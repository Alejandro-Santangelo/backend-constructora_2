package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AdelantoRequestDTO;
import com.rodrigo.construccion.dto.response.AdelantoResponseDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.PagoProfesionalObra;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.repository.PagoProfesionalObraRepository;
import com.rodrigo.construccion.repository.ProfesionalObraRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de adelantos a profesionales en obras
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdelantoService implements IAdelantoService {

    private final PagoProfesionalObraRepository pagoRepository;
    private final ProfesionalObraRepository profesionalObraRepository;
    private final IEmpresaService empresaService;

    // Constantes
    private static final String TIPO_PAGO_ADELANTO = "ADELANTO";
    private static final String ESTADO_ADELANTO_ACTIVO = "ACTIVO";
    private static final String ESTADO_ADELANTO_COMPLETADO = "COMPLETADO";
    private static final String ESTADO_ADELANTO_CANCELADO = "CANCELADO";
    private static final String ESTADO_PAGO_PAGADO = "PAGADO";
    private static final BigDecimal PORCENTAJE_MAXIMO_ADELANTO = new BigDecimal("50"); // 50% del total asignado
    private static final String METODO_PAGO_DEFAULT = "efectivo";

    @Override
    public AdelantoResponseDTO crearAdelanto(AdelantoRequestDTO request) {
        log.info("Creando adelanto para profesionalObra {} por monto {}", request.getProfesionalObraId(), request.getMonto());

        // 1. Validar empresa
        empresaService.findEmpresaById(request.getEmpresaId());

        // 2. Obtener y validar profesional-obra
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(request.getProfesionalObraId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesional-obra no encontrado con ID: " + request.getProfesionalObraId()));

        // 3. Validar que pertenece a la empresa
        if (!profesionalObra.getEmpresaId().equals(request.getEmpresaId())) {
            throw new IllegalArgumentException("El profesional-obra no pertenece a la empresa especificada");
        }

        // 4. Validar que el profesional está activo
        if (!"activo".equalsIgnoreCase(profesionalObra.getEstado())) {
            throw new IllegalArgumentException("No se puede otorgar adelanto a un profesional inactivo");
        }

        // 5. Validar saldo disponible (sin límite del 50%)
        validarSaldoDisponible(request.getProfesionalObraId(), request.getMonto());

        // 6. Crear el adelanto
        PagoProfesionalObra adelanto = new PagoProfesionalObra();
        adelanto.setProfesionalObra(profesionalObra);
        adelanto.setEmpresaId(request.getEmpresaId());
        adelanto.setTipoPago(TIPO_PAGO_ADELANTO);
        adelanto.setFechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDate.now());
        
        // Montos
        adelanto.setMontoBase(request.getMonto());
        adelanto.setMontoFinal(request.getMonto());
        adelanto.setMontoNeto(request.getMonto());
        adelanto.setMontoBruto(request.getMonto());
        
        // Campos específicos de adelantos
        adelanto.setEsAdelanto(true);
        adelanto.setPeriodoAdelanto(request.getPeriodoAdelanto());
        adelanto.setEstadoAdelanto(ESTADO_ADELANTO_ACTIVO);
        adelanto.setSaldoAdelantoPorDescontar(request.getMonto());
        adelanto.setMontoOriginalAdelanto(request.getMonto());
        
        // Estado y otros campos
        adelanto.setEstado(ESTADO_PAGO_PAGADO);
        adelanto.setMotivo(request.getMotivo());
        adelanto.setObservaciones(request.getObservaciones());
        adelanto.setMetodoPago(request.getMetodoPago() != null ? request.getMetodoPago() : METODO_PAGO_DEFAULT);
        adelanto.setNumeroComprobante(request.getNumeroComprobante());
        adelanto.setAprobadoPor(request.getAprobadoPor());
        adelanto.setPresupuestoNoClienteId(request.getPresupuestoNoClienteId());
        
        // Inicializar descuentos en cero
        adelanto.setDescuentoAdelantos(BigDecimal.ZERO);
        adelanto.setDescuentoPresentismo(BigDecimal.ZERO);
        adelanto.setAjustes(BigDecimal.ZERO);
        
        // Guardar
        PagoProfesionalObra adelantoGuardado = pagoRepository.save(adelanto);
        log.info("Adelanto creado exitosamente con ID: {}", adelantoGuardado.getId());

        // Mapear a DTO y agregar advertencias si corresponde
        AdelantoResponseDTO response = mapearADTO(adelantoGuardado);
        agregarAdvertenciasSiCorresponde(response, profesionalObra, request.getMonto());
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AdelantoResponseDTO obtenerAdelantoPorId(Long id, Long empresaId) {
        log.debug("Buscando adelanto ID: {} para empresa: {}", id, empresaId);

        PagoProfesionalObra adelanto = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adelanto no encontrado con ID: " + id));

        // Validar que es un adelanto
        if (!Boolean.TRUE.equals(adelanto.getEsAdelanto())) {
            throw new IllegalArgumentException("El pago con ID " + id + " no es un adelanto");
        }

        // Validar empresa
        if (!adelanto.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El adelanto no pertenece a la empresa especificada");
        }

        return mapearADTO(adelanto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdelantoResponseDTO> listarAdelantosPorEmpresa(Long empresaId) {
        log.debug("Listando adelantos para empresa: {}", empresaId);
        
        empresaService.findEmpresaById(empresaId);
        
        List<PagoProfesionalObra> adelantos = pagoRepository.findByEmpresaIdAndTipoPago(empresaId, TIPO_PAGO_ADELANTO);
        
        return adelantos.stream()
                .filter(pago -> Boolean.TRUE.equals(pago.getEsAdelanto()))
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdelantoResponseDTO> listarAdelantosPorProfesional(Long profesionalObraId, Long empresaId) {
        log.debug("Listando adelantos para profesional-obra: {}", profesionalObraId);

        // Validar profesional-obra
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(profesionalObraId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesional-obra no encontrado con ID: " + profesionalObraId));

        if (!profesionalObra.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El profesional-obra no pertenece a la empresa especificada");
        }

        List<PagoProfesionalObra> adelantos = pagoRepository.findByProfesionalObraIdAndTipoPago(
                profesionalObraId, TIPO_PAGO_ADELANTO);

        return adelantos.stream()
                .filter(pago -> Boolean.TRUE.equals(pago.getEsAdelanto()))
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdelantoResponseDTO> listarAdelantosPendientes(Long profesionalObraId, Long empresaId) {
        log.debug("Listando adelantos pendientes para profesional-obra: {}", profesionalObraId);

        // Validar profesional-obra
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(profesionalObraId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesional-obra no encontrado con ID: " + profesionalObraId));

        if (!profesionalObra.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El profesional-obra no pertenece a la empresa especificada");
        }

        List<PagoProfesionalObra> adelantos = pagoRepository.findAdelantosActivosByProfesionalObraId(profesionalObraId);

        return adelantos.stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdelantoResponseDTO> listarAdelantosPorObra(Long obraId, Long empresaId) {
        log.debug("Listando adelantos para obra: {}", obraId);

        empresaService.findEmpresaById(empresaId);

        // Obtener todos los adelantos de la empresa y filtrar por obra
        List<PagoProfesionalObra> todosAdelantos = pagoRepository.findByEmpresaIdAndTipoPago(empresaId, TIPO_PAGO_ADELANTO);

        return todosAdelantos.stream()
                .filter(pago -> Boolean.TRUE.equals(pago.getEsAdelanto()))
                .filter(pago -> pago.getProfesionalObra() != null && 
                               pago.getProfesionalObra().getIdObra() != null &&
                               pago.getProfesionalObra().getIdObra().equals(obraId))
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    public AdelantoResponseDTO actualizarAdelanto(Long id, AdelantoRequestDTO request, Long empresaId) {
        log.info("Actualizando adelanto ID: {}", id);

        PagoProfesionalObra adelanto = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adelanto no encontrado con ID: " + id));

        // Validar que es un adelanto
        if (!Boolean.TRUE.equals(adelanto.getEsAdelanto())) {
            throw new IllegalArgumentException("El pago con ID " + id + " no es un adelanto");
        }

        // Validar empresa
        if (!adelanto.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El adelanto no pertenece a la empresa especificada");
        }

        // Validar que no está completado o cancelado
        if (ESTADO_ADELANTO_COMPLETADO.equals(adelanto.getEstadoAdelanto()) ||
            ESTADO_ADELANTO_CANCELADO.equals(adelanto.getEstadoAdelanto())) {
            throw new IllegalStateException("No se puede modificar un adelanto " + adelanto.getEstadoAdelanto().toLowerCase());
        }

        // Actualizar solo campos permitidos (no el monto)
        if (request.getObservaciones() != null) {
            adelanto.setObservaciones(request.getObservaciones());
        }
        if (request.getMotivo() != null) {
            adelanto.setMotivo(request.getMotivo());
        }
        if (request.getMetodoPago() != null) {
            adelanto.setMetodoPago(request.getMetodoPago());
        }
        if (request.getNumeroComprobante() != null) {
            adelanto.setNumeroComprobante(request.getNumeroComprobante());
        }
        if (request.getAprobadoPor() != null) {
            adelanto.setAprobadoPor(request.getAprobadoPor());
        }

        adelanto.setFechaModificacion(LocalDateTime.now());

        PagoProfesionalObra adelantoActualizado = pagoRepository.save(adelanto);
        log.info("Adelanto actualizado exitosamente");

        return mapearADTO(adelantoActualizado);
    }

    @Override
    public AdelantoResponseDTO anularAdelanto(Long id, Long empresaId, String motivo) {
        log.info("Anulando adelanto ID: {}", id);

        PagoProfesionalObra adelanto = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adelanto no encontrado con ID: " + id));

        // Validar que es un adelanto
        if (!Boolean.TRUE.equals(adelanto.getEsAdelanto())) {
            throw new IllegalArgumentException("El pago con ID " + id + " no es un adelanto");
        }

        // Validar empresa
        if (!adelanto.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El adelanto no pertenece a la empresa especificada");
        }

        // Validar que no está ya cancelado
        if (ESTADO_ADELANTO_CANCELADO.equals(adelanto.getEstadoAdelanto())) {
            throw new IllegalStateException("El adelanto ya está cancelado");
        }

        // Validar que no tiene descuentos aplicados
        BigDecimal montoDescontado = adelanto.getMontoOriginalAdelanto().subtract(adelanto.getSaldoAdelantoPorDescontar());
        if (montoDescontado.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "No se puede anular un adelanto que ya tiene descuentos aplicados. Monto descontado: $" + montoDescontado);
        }

        // Anular
        adelanto.setEstadoAdelanto(ESTADO_ADELANTO_CANCELADO);
        adelanto.setSaldoAdelantoPorDescontar(BigDecimal.ZERO);
        adelanto.setObservaciones((adelanto.getObservaciones() != null ? adelanto.getObservaciones() + "\n" : "") +
                "ANULADO: " + (motivo != null ? motivo : "Sin motivo especificado") +
                " - Fecha: " + LocalDate.now());
        adelanto.setFechaModificacion(LocalDateTime.now());

        PagoProfesionalObra adelantoAnulado = pagoRepository.save(adelanto);
        log.info("Adelanto anulado exitosamente");

        return mapearADTO(adelantoAnulado);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalAdelantosPendientes(Long profesionalObraId) {
        List<PagoProfesionalObra> adelantosPendientes = 
                pagoRepository.findAdelantosActivosByProfesionalObraId(profesionalObraId);

        return adelantosPendientes.stream()
                .map(PagoProfesionalObra::getSaldoAdelantoPorDescontar)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Valida solo saldo disponible (sin límite del 50%)
     */
    private void validarSaldoDisponible(Long profesionalObraId, BigDecimal montoSolicitado) {
        log.debug("Validando saldo disponible para adelanto de ${} - profesional-obra: {}", 
                montoSolicitado, profesionalObraId);

        ProfesionalObra profesionalObra = profesionalObraRepository.findById(profesionalObraId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesional-obra no encontrado con ID: " + profesionalObraId));

        BigDecimal montoTotalAsignado = BigDecimal.ZERO;
        if (profesionalObra.getCantidadJornales() != null && profesionalObra.getImporteJornal() != null) {
            montoTotalAsignado = profesionalObra.getImporteJornal()
                    .multiply(profesionalObra.getCantidadJornales());
        }

        if (montoTotalAsignado.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "El profesional no tiene monto total asignado. Configure cantidad de jornales e importe por jornal.");
        }

        BigDecimal totalPagado = pagoRepository.calcularTotalPagadoByProfesional(profesionalObraId);
        BigDecimal saldoDisponible = montoTotalAsignado.subtract(totalPagado);

        if (montoSolicitado.compareTo(saldoDisponible) > 0) {
            throw new IllegalArgumentException(
                    String.format("El adelanto solicitado ($%,.2f) excede el saldo disponible ($%,.2f). " +
                                "Total asignado: $%,.2f, Total pagado: $%,.2f",
                            montoSolicitado, saldoDisponible, montoTotalAsignado, totalPagado));
        }

        log.info("Validación exitosa. Saldo disponible: ${}", saldoDisponible);
    }
    
    /**
     * Agrega advertencias si el adelanto excede el 50% recomendado (sin bloquear)
     */
    private void agregarAdvertenciasSiCorresponde(AdelantoResponseDTO response, 
                                                   ProfesionalObra profesionalObra, 
                                                   BigDecimal montoSolicitado) {
        BigDecimal montoTotalAsignado = BigDecimal.ZERO;
        if (profesionalObra.getCantidadJornales() != null && profesionalObra.getImporteJornal() != null) {
            montoTotalAsignado = profesionalObra.getImporteJornal()
                    .multiply(profesionalObra.getCantidadJornales());
        }
        
        if (montoTotalAsignado.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        
        BigDecimal adelantosPendientes = calcularTotalAdelantosPendientes(profesionalObra.getId());
        BigDecimal totalAdelantos = adelantosPendientes.add(montoSolicitado);
        BigDecimal limiteRecomendado = montoTotalAsignado
                .multiply(PORCENTAJE_MAXIMO_ADELANTO)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        if (totalAdelantos.compareTo(limiteRecomendado) > 0) {
            response.setExcedeLimiteRecomendado(true);
            BigDecimal exceso = totalAdelantos.subtract(limiteRecomendado);
            BigDecimal porcentajeReal = totalAdelantos
                    .multiply(new BigDecimal("100"))
                    .divide(montoTotalAsignado, 2, RoundingMode.HALF_UP);
            
            response.setAdvertencia(
                String.format("⚠️ ADVERTENCIA: El total de adelantos activos ($%,.2f) excede el límite recomendado " +
                            "del 50%% ($%,.2f). Exceso: $%,.2f (%.2f%% del total asignado).",
                        totalAdelantos, limiteRecomendado, exceso, porcentajeReal)
            );
            
            log.warn("Adelanto excede límite recomendado: Total adelantos: ${}, Límite 50%: ${}, Exceso: ${}",
                    totalAdelantos, limiteRecomendado, exceso);
        } else {
            response.setExcedeLimiteRecomendado(false);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validarAdelantoDisponible(Long profesionalObraId, BigDecimal montoSolicitado) {
        // Solo valida saldo disponible, no lanza excepción por límite del 50%
        validarSaldoDisponible(profesionalObraId, montoSolicitado);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdelantoResponseDTO> obtenerHistorialAdelantos(Long profesionalObraId, Long empresaId) {
        return listarAdelantosPorProfesional(profesionalObraId, empresaId).stream()
                .sorted((a1, a2) -> a2.getFechaPago().compareTo(a1.getFechaPago())) // Más reciente primero
                .collect(Collectors.toList());
    }

    /**
     * Mapear PagoProfesionalObra a AdelantoResponseDTO
     */
    private AdelantoResponseDTO mapearADTO(PagoProfesionalObra pago) {
        AdelantoResponseDTO dto = new AdelantoResponseDTO();

        dto.setId(pago.getId());
        dto.setEmpresaId(pago.getEmpresaId());
        dto.setPresupuestoNoClienteId(pago.getPresupuestoNoClienteId());

        // Datos del profesional-obra
        ProfesionalObra profesionalObra = pago.getProfesionalObra();
        if (profesionalObra != null) {
            dto.setProfesionalObraId(profesionalObra.getId());
            dto.setObraId(profesionalObra.getIdObra());

            // Datos del profesional
            Profesional profesional = profesionalObra.getProfesional();
            if (profesional != null) {
                dto.setProfesionalId(profesional.getId());
                dto.setNombreProfesional(profesional.getNombre());
                dto.setTipoProfesional(profesional.getTipoProfesional());
                dto.setEmailProfesional(profesional.getEmail());
                dto.setTelefonoProfesional(profesional.getTelefono());
                dto.setCuitProfesional(profesional.getCuit());
            }

            // Datos de la obra
            Obra obra = profesionalObra.getObra();
            if (obra != null) {
                dto.setNombreObra(obra.getNombre());
            }
        }

        // Datos del adelanto
        dto.setMontoOriginal(pago.getMontoOriginalAdelanto() != null ? 
                pago.getMontoOriginalAdelanto() : pago.getMontoFinal());
        dto.setSaldoPendiente(pago.getSaldoAdelantoPorDescontar() != null ? 
                pago.getSaldoAdelantoPorDescontar() : BigDecimal.ZERO);

        BigDecimal montoOriginal = dto.getMontoOriginal();
        BigDecimal saldoPendiente = dto.getSaldoPendiente();
        BigDecimal montoDescontado = montoOriginal.subtract(saldoPendiente);
        dto.setMontoDescontado(montoDescontado);

        // Calcular porcentaje descontado
        if (montoOriginal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal porcentaje = montoDescontado
                    .multiply(new BigDecimal("100"))
                    .divide(montoOriginal, 2, RoundingMode.HALF_UP);
            dto.setPorcentajeDescontado(porcentaje);
        } else {
            dto.setPorcentajeDescontado(BigDecimal.ZERO);
        }

        dto.setCompletamenteDescontado(saldoPendiente.compareTo(BigDecimal.ZERO) == 0 && 
                                       montoOriginal.compareTo(BigDecimal.ZERO) > 0);

        dto.setFechaPago(pago.getFechaPago());
        dto.setPeriodoAdelanto(pago.getPeriodoAdelanto());
        dto.setEstado(pago.getEstadoAdelanto() != null ? pago.getEstadoAdelanto() : pago.getEstado());
        dto.setMotivo(pago.getMotivo());
        dto.setObservaciones(pago.getObservaciones());
        dto.setMetodoPago(pago.getMetodoPago());
        dto.setNumeroComprobante(pago.getNumeroComprobante());
        dto.setAprobadoPor(pago.getAprobadoPor());
        dto.setFechaCreacion(pago.getFechaCreacion());
        dto.setFechaModificacion(pago.getFechaModificacion());

        return dto;
    }
}
