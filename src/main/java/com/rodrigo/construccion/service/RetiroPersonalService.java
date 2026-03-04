package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.RetiroPersonalRequestDTO;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.model.entity.RetiroPersonal;
import com.rodrigo.construccion.repository.AsignacionCobroObraRepository;
import com.rodrigo.construccion.repository.CobroEmpresaRepository;
import com.rodrigo.construccion.repository.RetiroPersonalRepository;
import com.rodrigo.construccion.repository.PagoProfesionalObraRepository;
import com.rodrigo.construccion.repository.PagoConsolidadoRepository;
import com.rodrigo.construccion.repository.PagoTrabajoExtraObraRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RetiroPersonalService {

    private final RetiroPersonalRepository retiroPersonalRepository;
    private final CobroEmpresaRepository cobroEmpresaRepository;
    private final AsignacionCobroObraRepository asignacionCobroObraRepository;
    private final PagoProfesionalObraRepository pagoProfesionalObraRepository;
    private final PagoConsolidadoRepository pagoConsolidadoRepository;
    private final PagoTrabajoExtraObraRepository pagoTrabajoExtraObraRepository;

    /**
     * CRÍTICO: Calcular saldo disponible para retiros
     * Fórmula: totalCobrado - totalRetirado
     * NOTA: Los PAGOS a profesionales/materiales NO se restan del saldo de retiros personales.
     *       Solo se consideran: cobros del cliente menos retiros personales previos.
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoDisponible(Long empresaId) {
        // Total cobrado (de tabla cobros_empresa, sin filtrar por estado ANULADO)
        BigDecimal totalCobrado = cobroEmpresaRepository.calcularTotalCobradoByEmpresa(empresaId);
        if (totalCobrado == null) {
            totalCobrado = BigDecimal.ZERO;
        }

        // Total retirado (solo estado ACTIVO)
        BigDecimal totalRetirado = retiroPersonalRepository.sumMontoByEmpresaIdAndEstado(
            empresaId, "ACTIVO"
        );
        if (totalRetirado == null) {
            totalRetirado = BigDecimal.ZERO;
        }

        // Saldo disponible = solo cobrado menos retirado (sin restar pagos operativos)
        return totalCobrado.subtract(totalRetirado);
    }

    /**
     * Obtener saldo disponible con desglose completo
     */
    @Transactional(readOnly = true)
    public SaldoDisponibleResponseDTO obtenerSaldoDisponibleCompleto(Long empresaId) {
        // Totales
        BigDecimal totalCobrado = cobroEmpresaRepository.calcularTotalCobradoByEmpresa(empresaId);
        if (totalCobrado == null) totalCobrado = BigDecimal.ZERO;

        // Total pagado = honorarios + materiales/gastos + trabajos extra
        // NOTA: Este total se calcula para informar al frontend, pero NO afecta el saldo de retiros
        BigDecimal totalHonorarios = pagoProfesionalObraRepository.calcularTotalPagadoByEmpresa(empresaId);
        if (totalHonorarios == null) totalHonorarios = BigDecimal.ZERO;
        
        BigDecimal totalMaterialesGastos = pagoConsolidadoRepository.calcularTotalPagadoByEmpresa(empresaId);
        if (totalMaterialesGastos == null) totalMaterialesGastos = BigDecimal.ZERO;
        
        BigDecimal totalTrabajosExtra = pagoTrabajoExtraObraRepository.calcularTotalPagadoByEmpresa(empresaId);
        if (totalTrabajosExtra == null) totalTrabajosExtra = BigDecimal.ZERO;
        
        BigDecimal totalPagado = totalHonorarios.add(totalMaterialesGastos).add(totalTrabajosExtra);

        BigDecimal totalRetirado = retiroPersonalRepository.sumMontoByEmpresaIdAndEstado(
            empresaId, "ACTIVO"
        );
        if (totalRetirado == null) totalRetirado = BigDecimal.ZERO;

        // Saldo disponible = solo cobrado menos retirado (sin restar pagos operativos)
        BigDecimal saldoDisponible = totalCobrado.subtract(totalRetirado);

        // Desglose detallado
        DesgloseFinancieroDTO desglose = new DesgloseFinancieroDTO();
        
        // Resumen cobros
        ResumenCobrosDTO resumenCobros = new ResumenCobrosDTO();
        List<Object> allCobros = cobroEmpresaRepository.findByEmpresaId(empresaId).stream()
            .map(c -> (Object) c)
            .toList();
        resumenCobros.setCantidad((long) allCobros.size());
        resumenCobros.setMonto(totalCobrado);
        resumenCobros.setCobrados((long) allCobros.size()); // Todos los cobros no anulados
        resumenCobros.setPendientes(0L);
        resumenCobros.setAnulados(0L);
        desglose.setCobros(resumenCobros);

        // Resumen asignaciones
        ResumenAsignacionesDTO resumenAsignaciones = new ResumenAsignacionesDTO();
        Long countAsignaciones = (long) asignacionCobroObraRepository.findByEmpresaId(empresaId).size();
        resumenAsignaciones.setCantidad(countAsignaciones);
        resumenAsignaciones.setMonto(totalPagado);
        resumenAsignaciones.setActivas(asignacionCobroObraRepository.findByEmpresaIdAndEstado(
            empresaId, "ACTIVA"
        ).size() + 0L);
        resumenAsignaciones.setAnuladas(asignacionCobroObraRepository.findByEmpresaId(empresaId).stream()
            .filter(a -> "ANULADA".equals(a.getEstado()))
            .count());
        desglose.setAsignaciones(resumenAsignaciones);

        // Resumen retiros
        ResumenRetirosDTO resumenRetiros = new ResumenRetirosDTO();
        Long countRetiros = retiroPersonalRepository.countByEmpresaIdAndEstado(empresaId, "ACTIVO");
        resumenRetiros.setCantidad(countRetiros != null ? countRetiros : 0L);
        resumenRetiros.setMonto(totalRetirado);
        resumenRetiros.setActivos(retiroPersonalRepository.countByEmpresaIdAndEstado(
            empresaId, "ACTIVO"
        ));
        resumenRetiros.setAnulados(retiroPersonalRepository.countByEmpresaIdAndEstado(
            empresaId, "ANULADO"
        ));
        desglose.setRetiros(resumenRetiros);

        SaldoDisponibleResponseDTO response = new SaldoDisponibleResponseDTO();
        response.setEmpresaId(empresaId);
        response.setTotalCobrado(totalCobrado);
        response.setTotalPagado(totalPagado);
        response.setTotalRetirado(totalRetirado);
        response.setSaldoDisponible(saldoDisponible);
        response.setDesglose(desglose);

        return response;
    }

    /**
     * Registrar un nuevo retiro con validaciones
     */
    @Transactional
    public RetiroPersonalResponseDTO registrarRetiro(RetiroPersonalRequestDTO request) {
        // Validar fecha no futura
        if (request.getFechaRetiro().isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de retiro no puede ser futura");
        }

        // Validar saldo disponible (CRÍTICO)
        BigDecimal saldoDisponible = calcularSaldoDisponible(request.getEmpresaId());

        if (request.getMonto().compareTo(saldoDisponible) > 0) {
            BigDecimal faltante = request.getMonto().subtract(saldoDisponible);
            String mensaje = String.format(
                "No hay saldo suficiente para retirar.\n" +
                "Disponible: $%,.2f\n" +
                "Solicitado: $%,.2f\n" +
                "Faltante: $%,.2f",
                saldoDisponible,
                request.getMonto(),
                faltante
            );
            throw new RuntimeException(mensaje);
        }

        // Crear retiro
        RetiroPersonal retiro = new RetiroPersonal();
        retiro.setEmpresaId(request.getEmpresaId());
        retiro.setObraId(request.getObraId());
        retiro.setMonto(request.getMonto());
        retiro.setFechaRetiro(request.getFechaRetiro());
        retiro.setMotivo(request.getMotivo());
        retiro.setTipoRetiro(request.getTipoRetiro() != null ? request.getTipoRetiro() : "GANANCIA");
        retiro.setEstado(RetiroPersonal.ESTADO_ACTIVO);
        retiro.setObservaciones(request.getObservaciones());

        retiro = retiroPersonalRepository.save(retiro);

        return mapToResponse(retiro);
    }

    /**
     * Listar retiros con filtros opcionales
     */
    @Transactional(readOnly = true)
    public List<RetiroPersonalResponseDTO> listarRetiros(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String tipoRetiro,
            String estado) {
        
        List<RetiroPersonal> retiros = retiroPersonalRepository.findByFiltros(
            empresaId, fechaDesde, fechaHasta, tipoRetiro, estado
        );

        return retiros.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un retiro por ID
     */
    @Transactional(readOnly = true)
    public RetiroPersonalResponseDTO obtenerRetiro(Long id, Long empresaId) {
        RetiroPersonal retiro = retiroPersonalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retiro no encontrado con ID: " + id));

        if (!retiro.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El retiro no pertenece a esta empresa");
        }

        return mapToResponse(retiro);
    }

    /**
     * Anular un retiro (cambia estado a ANULADO, el monto vuelve a estar disponible)
     */
    @Transactional
    public RetiroPersonalResponseDTO anularRetiro(Long id, Long empresaId) {
        RetiroPersonal retiro = retiroPersonalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retiro no encontrado con ID: " + id));

        if (!retiro.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El retiro no pertenece a esta empresa");
        }

        if (retiro.estaAnulado()) {
            throw new RuntimeException("El retiro ya está anulado");
        }

        retiro.anular();
        retiro = retiroPersonalRepository.save(retiro);

        return mapToResponse(retiro);
    }

    /**
     * Eliminar físicamente un retiro (solo si está ACTIVO)
     */
    @Transactional
    public void eliminarRetiro(Long id, Long empresaId) {
        RetiroPersonal retiro = retiroPersonalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retiro no encontrado con ID: " + id));

        if (!retiro.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El retiro no pertenece a esta empresa");
        }

        if (!retiro.puedeSerEliminado()) {
            throw new RuntimeException("Solo se pueden eliminar retiros en estado ACTIVO. Estado actual: " + retiro.getEstado());
        }

        retiroPersonalRepository.delete(retiro);
    }

    /**
     * Obtener totales de retiros
     */
    @Transactional(readOnly = true)
    public TotalesRetirosResponseDTO obtenerTotales(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        BigDecimal totalRetiros = retiroPersonalRepository.sumMontoByEmpresaIdAndEstado(
            empresaId, "ACTIVO"
        );
        if (totalRetiros == null) totalRetiros = BigDecimal.ZERO;

        Long cantidadRetiros = retiroPersonalRepository.countByEmpresaIdAndEstado(
            empresaId, "ACTIVO"
        );
        if (cantidadRetiros == null) cantidadRetiros = 0L;

        // Totales por tipo
        Map<String, BigDecimal> retirosPorTipo = new HashMap<>();
        List<RetiroPersonalRepository.TipoRetiroTotal> totalesTipo = 
            retiroPersonalRepository.sumMontoByTipo(empresaId);
        
        for (RetiroPersonalRepository.TipoRetiroTotal total : totalesTipo) {
            retirosPorTipo.put(total.getTipo(), total.getTotal());
        }

        // Totales por mes
        List<RetiroMensualDTO> retirosPorMes = retiroPersonalRepository
            .sumMontoByMes(empresaId, fechaDesde, fechaHasta)
            .stream()
            .map(rm -> {
                RetiroMensualDTO dto = new RetiroMensualDTO();
                dto.setAnio(rm.getAnio());
                dto.setMes(String.format("%04d-%02d", rm.getAnio(), rm.getMes()));
                dto.setTotal(rm.getTotal());
                dto.setCantidad(rm.getCantidad());
                return dto;
            })
            .collect(Collectors.toList());

        TotalesRetirosResponseDTO response = new TotalesRetirosResponseDTO();
        response.setEmpresaId(empresaId);
        response.setTotalRetiros(totalRetiros);
        response.setCantidadRetiros(cantidadRetiros);
        response.setRetirosPorTipo(retirosPorTipo);
        response.setRetirosPorMes(retirosPorMes);

        return response;
    }

    /**
     * Mapper de entidad a DTO de respuesta
     */
    private RetiroPersonalResponseDTO mapToResponse(RetiroPersonal retiro) {
        RetiroPersonalResponseDTO response = new RetiroPersonalResponseDTO();
        response.setId(retiro.getId());
        response.setEmpresaId(retiro.getEmpresaId());
        response.setObraId(retiro.getObraId());
        response.setMonto(retiro.getMonto());
        response.setFechaRetiro(retiro.getFechaRetiro());
        response.setMotivo(retiro.getMotivo());
        response.setTipoRetiro(retiro.getTipoRetiro());
        response.setEstado(retiro.getEstado());
        response.setObservaciones(retiro.getObservaciones());
        response.setFechaCreacion(retiro.getFechaCreacion());
        response.setFechaModificacion(retiro.getFechaModificacion());
        response.setUsuarioCreacionId(retiro.getUsuarioCreacionId());
        response.setUsuarioModificacionId(retiro.getUsuarioModificacionId());
        return response;
    }
}
