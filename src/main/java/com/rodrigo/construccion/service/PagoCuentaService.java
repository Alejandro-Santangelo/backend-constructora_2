package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.PagoCuentaRequestDTO;
import com.rodrigo.construccion.dto.response.PagoCuentaResponseDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.PagoParcialRubroRepository;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de pagos a cuenta sobre items de rubros del presupuesto
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PagoCuentaService implements IPagoCuentaService {

    private final PagoParcialRubroRepository pagoParcialRepository;
    private final PresupuestoNoClienteRepository presupuestoRepository;
    private final IEmpresaService empresaService;

    @Override
    public PagoCuentaResponseDTO crearPagoCuenta(PagoCuentaRequestDTO request) {
        log.info("Creando pago a cuenta: Presupuesto={}, Rubro={}, Tipo={}, Monto={}", 
                request.getPresupuestoId(), request.getNombreRubro(), request.getTipoItem(), request.getMonto());

        // 1. Validar empresa
        empresaService.findEmpresaById(request.getEmpresaId());

        // 2. Validar presupuesto
        PresupuestoNoCliente presupuesto = presupuestoRepository.findById(request.getPresupuestoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con ID: " + request.getPresupuestoId()));

        if (!presupuesto.getEmpresa().getId().equals(request.getEmpresaId())) {
            throw new IllegalArgumentException("El presupuesto no pertenece a la empresa especificada");
        }

        // 3. Validar tipo de item
        if (!PagoParcialRubro.TipoItem.esValido(request.getTipoItem())) {
            throw new IllegalArgumentException("Tipo de item inválido: " + request.getTipoItem());
        }

        // 4. Obtener el item del presupuesto y validar el rubro
        ItemCalculadoraPresupuesto item = presupuesto.getItemsCalculadora().stream()
                .filter(i -> i.getTipoProfesional().equalsIgnoreCase(request.getNombreRubro()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rubro no encontrado en el presupuesto: " + request.getNombreRubro()));

        // 5. Calcular monto total del item según el tipo
        BigDecimal montoTotalItem = calcularMontoTotalItem(item, request.getTipoItem());
        
        if (montoTotalItem.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El item seleccionado no tiene monto presupuestado");
        }

        // 6. Validar que el pago no exceda el saldo pendiente
        BigDecimal totalPagado = pagoParcialRepository.calcularTotalPagadoItem(
                request.getPresupuestoId(),
                request.getEmpresaId(),
                request.getNombreRubro(),
                request.getTipoItem());

        BigDecimal saldoPendiente = montoTotalItem.subtract(totalPagado);

        if (request.getMonto().compareTo(saldoPendiente) > 0) {
            throw new IllegalArgumentException(
                    String.format("El monto del pago ($%,.2f) excede el saldo pendiente ($%,.2f)", 
                            request.getMonto(), saldoPendiente));
        }

        // 7. Crear el pago
        PagoParcialRubro pago = PagoParcialRubro.builder()
                .presupuestoId(request.getPresupuestoId())
                .empresaId(request.getEmpresaId())
                .nombreRubro(request.getNombreRubro())
                .tipoItem(request.getTipoItem().toUpperCase())
                .monto(request.getMonto())
                .metodoPago(request.getMetodoPago() != null ? request.getMetodoPago().toUpperCase() : "EFECTIVO")
                .observaciones(request.getObservaciones())
                .fechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDate.now())
                .usuarioRegistro(request.getUsuarioRegistro())
                .build();

        pago = pagoParcialRepository.save(pago);
        log.info("Pago a cuenta creado exitosamente con ID: {}", pago.getId());

        // 8. Construir respuesta con totales actualizados
        return construirResponseDTO(pago, montoTotalItem);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoCuentaResponseDTO obtenerPagoPorId(Long id, Long empresaId) {
        log.debug("Obteniendo pago ID: {}, Empresa: {}", id, empresaId);

        PagoParcialRubro pago = pagoParcialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));

        if (!pago.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El pago no pertenece a la empresa especificada");
        }

        // Obtener presupuesto para calcular montos totales
        PresupuestoNoCliente presupuesto = presupuestoRepository.findById(pago.getPresupuestoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con ID: " + pago.getPresupuestoId()));

        ItemCalculadoraPresupuesto item = presupuesto.getItemsCalculadora().stream()
                .filter(i -> i.getTipoProfesional().equalsIgnoreCase(pago.getNombreRubro()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rubro no encontrado: " + pago.getNombreRubro()));

        BigDecimal montoTotalItem = calcularMontoTotalItem(item, pago.getTipoItem());

        return construirResponseDTO(pago, montoTotalItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoCuentaResponseDTO> listarPagosPorPresupuesto(Long presupuestoId, Long empresaId) {
        log.debug("Listando pagos del presupuesto: {}, Empresa: {}", presupuestoId, empresaId);

        // Validar empresa
        empresaService.findEmpresaById(empresaId);

        // Validar presupuesto
        PresupuestoNoCliente presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con ID: " + presupuestoId));

        if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El presupuesto no pertenece a la empresa especificada");
        }

        List<PagoParcialRubro> pagos = pagoParcialRepository
                .findByPresupuestoIdAndEmpresaIdOrderByFechaRegistroDesc(presupuestoId, empresaId);

        return pagos.stream()
                .map(pago -> {
                    ItemCalculadoraPresupuesto item = presupuesto.getItemsCalculadora().stream()
                            .filter(i -> i.getTipoProfesional().equalsIgnoreCase(pago.getNombreRubro()))
                            .findFirst()
                            .orElse(null);

                    BigDecimal montoTotalItem = item != null 
                            ? calcularMontoTotalItem(item, pago.getTipoItem())
                            : BigDecimal.ZERO;

                    return construirResponseDTO(pago, montoTotalItem);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoCuentaResponseDTO> listarPagosPorRubro(Long presupuestoId, Long empresaId, String nombreRubro) {
        log.debug("Listando pagos del rubro: {}, Presupuesto: {}", nombreRubro, presupuestoId);

        empresaService.findEmpresaById(empresaId);

        List<PagoParcialRubro> pagos = pagoParcialRepository
                .findByPresupuestoIdAndEmpresaIdAndNombreRubroOrderByFechaRegistroDesc(
                        presupuestoId, empresaId, nombreRubro);

        PresupuestoNoCliente presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con ID: " + presupuestoId));

        return pagos.stream()
                .map(pago -> {
                    ItemCalculadoraPresupuesto item = presupuesto.getItemsCalculadora().stream()
                            .filter(i -> i.getTipoProfesional().equalsIgnoreCase(nombreRubro))
                            .findFirst()
                            .orElse(null);

                    BigDecimal montoTotalItem = item != null 
                            ? calcularMontoTotalItem(item, pago.getTipoItem())
                            : BigDecimal.ZERO;

                    return construirResponseDTO(pago, montoTotalItem);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoCuentaResponseDTO> listarPagosPorItem(Long presupuestoId, Long empresaId, 
                                                          String nombreRubro, String tipoItem) {
        log.debug("Listando pagos del item: {}-{}, Presupuesto: {}", nombreRubro, tipoItem, presupuestoId);

        empresaService.findEmpresaById(empresaId);

        List<PagoParcialRubro> pagos = pagoParcialRepository
                .findByPresupuestoIdAndEmpresaIdAndNombreRubroAndTipoItemOrderByFechaRegistroDesc(
                        presupuestoId, empresaId, nombreRubro, tipoItem);

        PresupuestoNoCliente presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con ID: " + presupuestoId));

        ItemCalculadoraPresupuesto item = presupuesto.getItemsCalculadora().stream()
                .filter(i -> i.getTipoProfesional().equalsIgnoreCase(nombreRubro))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rubro no encontrado: " + nombreRubro));

        BigDecimal montoTotalItem = calcularMontoTotalItem(item, tipoItem);

        return pagos.stream()
                .map(pago -> construirResponseDTO(pago, montoTotalItem))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<String, Map<String, BigDecimal>>> obtenerResumenPagos(Long presupuestoId, Long empresaId) {
        log.debug("Generando resumen de pagos para presupuesto: {}", presupuestoId);

        empresaService.findEmpresaById(empresaId);

        PresupuestoNoCliente presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con ID: " + presupuestoId));

        if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El presupuesto no pertenece a la empresa especificada");
        }

        Map<String, Map<String, Map<String, BigDecimal>>> resumen = new LinkedHashMap<>();

        // Iterar sobre todos los rubros del presupuesto
        for (ItemCalculadoraPresupuesto item : presupuesto.getItemsCalculadora()) {
            String nombreRubro = item.getTipoProfesional();
            Map<String, Map<String, BigDecimal>> resumenRubro = new LinkedHashMap<>();

            // Para cada tipo de item (JORNALES, MATERIALES, GASTOS_GENERALES)
            for (PagoParcialRubro.TipoItem tipo : PagoParcialRubro.TipoItem.values()) {
                String tipoStr = tipo.name();
                BigDecimal montoTotal = calcularMontoTotalItem(item, tipoStr);

                if (montoTotal.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal totalPagado = pagoParcialRepository.calcularTotalPagadoItem(
                            presupuestoId, empresaId, nombreRubro, tipoStr);

                    BigDecimal saldoPendiente = montoTotal.subtract(totalPagado);

                    Map<String, BigDecimal> totales = new LinkedHashMap<>();
                    totales.put("totalPresupuestado", montoTotal);
                    totales.put("totalPagado", totalPagado);
                    totales.put("saldoPendiente", saldoPendiente);

                    resumenRubro.put(tipoStr, totales);
                }
            }

            if (!resumenRubro.isEmpty()) {
                resumen.put(nombreRubro, resumenRubro);
            }
        }

        return resumen;
    }

    @Override
    public void eliminarPago(Long id, Long empresaId) {
        log.info("Eliminando pago ID: {}, Empresa: {}", id, empresaId);

        PagoParcialRubro pago = pagoParcialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));

        if (!pago.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El pago no pertenece a la empresa especificada");
        }

        pagoParcialRepository.delete(pago);
        log.info("Pago eliminado exitosamente");
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> calcularTotalesItem(Long presupuestoId, Long empresaId, 
                                                       String nombreRubro, String tipoItem) {
        log.debug("Calculando totales del item: {}-{}", nombreRubro, tipoItem);

        empresaService.findEmpresaById(empresaId);

        PresupuestoNoCliente presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con ID: " + presupuestoId));

        ItemCalculadoraPresupuesto item = presupuesto.getItemsCalculadora().stream()
                .filter(i -> i.getTipoProfesional().equalsIgnoreCase(nombreRubro))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rubro no encontrado: " + nombreRubro));

        BigDecimal montoTotal = calcularMontoTotalItem(item, tipoItem);
        BigDecimal totalPagado = pagoParcialRepository.calcularTotalPagadoItem(
                presupuestoId, empresaId, nombreRubro, tipoItem);
        BigDecimal saldoPendiente = montoTotal.subtract(totalPagado);

        Map<String, BigDecimal> totales = new LinkedHashMap<>();
        totales.put("montoTotal", montoTotal);
        totales.put("totalPagado", totalPagado);
        totales.put("saldoPendiente", saldoPendiente);
        totales.put("porcentajePagado", montoTotal.compareTo(BigDecimal.ZERO) > 0
                ? totalPagado.divide(montoTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO);

        return totales;
    }

    // ============================================================================
    // MÉTODOS AUXILIARES PRIVADOS
    // ============================================================================

    /**
     * Calcula el monto total de un item según su tipo
     */
    private BigDecimal calcularMontoTotalItem(ItemCalculadoraPresupuesto item, String tipoItem) {
        switch (tipoItem.toUpperCase()) {
            case "JORNALES":
                return item.getJornales().stream()
                        .map(j -> j.getSubtotal() != null ? j.getSubtotal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

            case "MATERIALES":
                return item.getMaterialesLista().stream()
                        .map(m -> m.getSubtotal() != null ? m.getSubtotal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

            case "GASTOS_GENERALES":
                return item.getGastosGenerales().stream()
                        .map(g -> g.getSubtotal() != null ? g.getSubtotal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

            default:
                throw new IllegalArgumentException("Tipo de item no válido: " + tipoItem);
        }
    }

    /**
     * Construye el DTO de respuesta con todos los totales calculados
     */
    private PagoCuentaResponseDTO construirResponseDTO(PagoParcialRubro pago, BigDecimal montoTotalItem) {
        BigDecimal totalPagado = pagoParcialRepository.calcularTotalPagadoItem(
                pago.getPresupuestoId(),
                pago.getEmpresaId(),
                pago.getNombreRubro(),
                pago.getTipoItem());

        BigDecimal saldoPendiente = montoTotalItem.subtract(totalPagado);

        BigDecimal porcentajePagado = montoTotalItem.compareTo(BigDecimal.ZERO) > 0
                ? totalPagado.divide(montoTotalItem, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return PagoCuentaResponseDTO.builder()
                .id(pago.getId())
                .presupuestoId(pago.getPresupuestoId())
                .empresaId(pago.getEmpresaId())
                .nombreRubro(pago.getNombreRubro())
                .tipoItem(pago.getTipoItem())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .observaciones(pago.getObservaciones())
                .fechaPago(pago.getFechaPago())
                .usuarioRegistro(pago.getUsuarioRegistro())
                .fechaRegistro(pago.getFechaRegistro())
                .montoTotalItem(montoTotalItem)
                .totalPagado(totalPagado)
                .saldoPendiente(saldoPendiente)
                .porcentajePagado(porcentajePagado)
                .build();
    }
}
