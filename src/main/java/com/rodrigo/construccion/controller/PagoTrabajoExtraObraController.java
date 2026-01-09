package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.PagoTrabajoExtraRequestDTO;
import com.rodrigo.construccion.dto.response.PagoTrabajoExtraResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenPagosTrabajoExtraDTO;
import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.enums.EstadoPagoTrabajoExtra;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import com.rodrigo.construccion.service.PagoTrabajoExtraObraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para gestión de pagos de trabajos extra
 */
@RestController
@RequestMapping("/api/pagos-trabajos-extra")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = "*")
public class PagoTrabajoExtraObraController {

    private final PagoTrabajoExtraObraService pagoService;
    private final PagoTrabajoExtraObraRepository pagoRepository;
    private final TrabajoExtraRepository trabajoExtraRepository;
    private final TrabajoExtroProfesionalRepository profesionalRepository;
    private final TrabajoExtraTareaRepository tareaRepository;
    private final ObraRepository obraRepository;
    private final PresupuestoNoClienteRepository presupuestoRepository;

    // ========== CRUD BÁSICO ==========

    /**
     * Crear un nuevo pago de trabajo extra
     * POST /api/pagos-trabajos-extra
     */
    @PostMapping
    public ResponseEntity<PagoTrabajoExtraResponseDTO> crearPago(
            @Valid @RequestBody PagoTrabajoExtraRequestDTO request) {
        log.info("POST /api/pagos-trabajos-extra - Crear nuevo pago");
        
        try {
            // Construir entidad
            PagoTrabajoExtraObra pago = convertirDTOAEntidad(request);
            
            // Crear pago
            PagoTrabajoExtraObra pagoCreado = pagoService.crearPago(pago);
            
            // Convertir a DTO de respuesta
            PagoTrabajoExtraResponseDTO response = convertirEntidadADTO(pagoCreado);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error al crear pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear pago: " + e.getMessage());
        }
    }

    /**
     * Obtener pago por ID
     * GET /api/pagos-trabajos-extra/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PagoTrabajoExtraResponseDTO> obtenerPagoPorId(@PathVariable Long id) {
        log.info("GET /api/pagos-trabajos-extra/{}", id);
        
        PagoTrabajoExtraObra pago = pagoService.obtenerPorId(id);
        PagoTrabajoExtraResponseDTO response = convertirEntidadADTO(pago);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un pago existente
     * PUT /api/pagos-trabajos-extra/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PagoTrabajoExtraResponseDTO> actualizarPago(
            @PathVariable Long id,
            @Valid @RequestBody PagoTrabajoExtraRequestDTO request) {
        log.info("PUT /api/pagos-trabajos-extra/{}", id);
        
        try {
            PagoTrabajoExtraObra pagoActualizado = convertirDTOAEntidad(request);
            PagoTrabajoExtraObra pagoGuardado = pagoService.actualizarPago(id, pagoActualizado);
            PagoTrabajoExtraResponseDTO response = convertirEntidadADTO(pagoGuardado);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al actualizar pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar pago: " + e.getMessage());
        }
    }

    /**
     * Eliminar un pago
     * DELETE /api/pagos-trabajos-extra/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPago(@PathVariable Long id) {
        log.info("DELETE /api/pagos-trabajos-extra/{}", id);
        
        pagoService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Anular un pago
     * PUT /api/pagos-trabajos-extra/{id}/anular
     */
    @PutMapping("/{id}/anular")
    public ResponseEntity<PagoTrabajoExtraResponseDTO> anularPago(
            @PathVariable Long id,
            @RequestParam String motivo) {
        log.info("PUT /api/pagos-trabajos-extra/{}/anular", id);
        
        PagoTrabajoExtraObra pagoAnulado = pagoService.anularPago(id, motivo);
        PagoTrabajoExtraResponseDTO response = convertirEntidadADTO(pagoAnulado);
        
        return ResponseEntity.ok(response);
    }

    // ========== CONSULTAS ==========

    /**
     * Obtener todos los pagos de un trabajo extra
     * GET /api/pagos-trabajos-extra/trabajo-extra/{trabajoExtraId}
     */
    @GetMapping("/trabajo-extra/{trabajoExtraId}")
    public ResponseEntity<List<PagoTrabajoExtraResponseDTO>> obtenerPagosPorTrabajoExtra(
            @PathVariable Long trabajoExtraId) {
        log.info("GET /api/pagos-trabajos-extra/trabajo-extra/{}", trabajoExtraId);
        
        List<PagoTrabajoExtraObra> pagos = pagoService.obtenerPagosPorTrabajoExtra(trabajoExtraId);
        List<PagoTrabajoExtraResponseDTO> response = pagos.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todos los pagos de una obra
     * GET /api/pagos-trabajos-extra/obra/{obraId}
     */
    @GetMapping("/obra/{obraId}")
    public ResponseEntity<List<PagoTrabajoExtraResponseDTO>> obtenerPagosPorObra(
            @PathVariable Long obraId) {
        log.info("GET /api/pagos-trabajos-extra/obra/{}", obraId);
        
        List<PagoTrabajoExtraObra> pagos = pagoService.obtenerPagosPorObra(obraId);
        List<PagoTrabajoExtraResponseDTO> response = pagos.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener todos los pagos de una empresa
     * GET /api/pagos-trabajos-extra/empresa/{empresaId}
     */
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<PagoTrabajoExtraResponseDTO>> obtenerPagosPorEmpresa(
            @PathVariable Long empresaId) {
        log.info("GET /api/pagos-trabajos-extra/empresa/{}", empresaId);
        
        List<PagoTrabajoExtraObra> pagos = pagoService.obtenerPagosPorEmpresa(empresaId);
        List<PagoTrabajoExtraResponseDTO> response = pagos.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener pagos de una empresa en un período
     * GET /api/pagos-trabajos-extra/empresa/{empresaId}/periodo?fechaDesde=2025-01-01&fechaHasta=2025-12-31
     */
    @GetMapping("/empresa/{empresaId}/periodo")
    public ResponseEntity<List<PagoTrabajoExtraResponseDTO>> obtenerPagosPorEmpresaYPeriodo(
            @PathVariable Long empresaId,
            @RequestParam LocalDate fechaDesde,
            @RequestParam LocalDate fechaHasta) {
        log.info("GET /api/pagos-trabajos-extra/empresa/{}/periodo", empresaId);
        
        List<PagoTrabajoExtraObra> pagos = pagoService.obtenerPagosPorEmpresaYPeriodo(
            empresaId, fechaDesde, fechaHasta);
        List<PagoTrabajoExtraResponseDTO> response = pagos.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener pagos a un profesional específico
     * GET /api/pagos-trabajos-extra/profesional/{profesionalId}
     */
    @GetMapping("/profesional/{profesionalId}")
    public ResponseEntity<List<PagoTrabajoExtraResponseDTO>> obtenerPagosPorProfesional(
            @PathVariable Long profesionalId) {
        log.info("GET /api/pagos-trabajos-extra/profesional/{}", profesionalId);
        
        List<PagoTrabajoExtraObra> pagos = pagoService.obtenerPagosPorProfesional(profesionalId);
        List<PagoTrabajoExtraResponseDTO> response = pagos.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener pagos de una tarea específica
     * GET /api/pagos-trabajos-extra/tarea/{tareaId}
     */
    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<List<PagoTrabajoExtraResponseDTO>> obtenerPagosPorTarea(
            @PathVariable Long tareaId) {
        log.info("GET /api/pagos-trabajos-extra/tarea/{}", tareaId);
        
        List<PagoTrabajoExtraObra> pagos = pagoService.obtenerPagosPorTarea(tareaId);
        List<PagoTrabajoExtraResponseDTO> response = pagos.stream()
            .map(this::convertirEntidadADTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    // ========== RESÚMENES ==========

    /**
     * Obtener resumen de pagos de un trabajo extra
     * GET /api/pagos-trabajos-extra/trabajo-extra/{trabajoExtraId}/resumen
     */
    @GetMapping("/trabajo-extra/{trabajoExtraId}/resumen")
    public ResponseEntity<ResumenPagosTrabajoExtraDTO> obtenerResumenPagos(
            @PathVariable Long trabajoExtraId) {
        log.info("GET /api/pagos-trabajos-extra/trabajo-extra/{}/resumen", trabajoExtraId);
        
        TrabajoExtra trabajoExtra = trabajoExtraRepository.findById(trabajoExtraId)
            .orElseThrow(() -> new RuntimeException("Trabajo extra no encontrado"));
        
        ResumenPagosTrabajoExtraDTO resumen = generarResumen(trabajoExtra);
        
        return ResponseEntity.ok(resumen);
    }

    /**
     * Calcular total pagado de un trabajo extra
     * GET /api/pagos-trabajos-extra/trabajo-extra/{trabajoExtraId}/total-pagado
     */
    @GetMapping("/trabajo-extra/{trabajoExtraId}/total-pagado")
    public ResponseEntity<BigDecimal> calcularTotalPagado(@PathVariable Long trabajoExtraId) {
        log.info("GET /api/pagos-trabajos-extra/trabajo-extra/{}/total-pagado", trabajoExtraId);
        
        BigDecimal total = pagoService.calcularTotalPagado(trabajoExtraId);
        return ResponseEntity.ok(total);
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Convertir DTO de request a entidad
     */
    private PagoTrabajoExtraObra convertirDTOAEntidad(PagoTrabajoExtraRequestDTO dto) {
        PagoTrabajoExtraObra pago = new PagoTrabajoExtraObra();
        
        // Relaciones
        TrabajoExtra trabajoExtra = trabajoExtraRepository.findById(dto.getTrabajoExtraId())
            .orElseThrow(() -> new RuntimeException("Trabajo extra no encontrado"));
        pago.setTrabajoExtra(trabajoExtra);
        
        Obra obra = obraRepository.findById(dto.getObraId())
            .orElseThrow(() -> new RuntimeException("Obra no encontrada"));
        pago.setObra(obra);
        
        pago.setEmpresaId(trabajoExtra.getEmpresaId());
        
        if (dto.getPresupuestoNoClienteId() != null) {
            PresupuestoNoCliente presupuesto = presupuestoRepository.findById(dto.getPresupuestoNoClienteId())
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));
            pago.setPresupuestoNoCliente(presupuesto);
        }
        
        // Referencias específicas
        if (dto.getTrabajoExtroProfesionalId() != null) {
            TrabajoExtroProfesional profesional = profesionalRepository.findById(dto.getTrabajoExtroProfesionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));
            pago.setTrabajoExtroProfesional(profesional);
        }
        
        if (dto.getTrabajoExtraTareaId() != null) {
            TrabajoExtraTarea tarea = tareaRepository.findById(dto.getTrabajoExtraTareaId())
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
            pago.setTrabajoExtraTarea(tarea);
        }
        
        // Datos del pago
        pago.setTipoPago(dto.getTipoPago());
        pago.setConcepto(dto.getConcepto());
        pago.setMontoBase(dto.getMontoBase());
        pago.setDescuentos(dto.getDescuentos());
        pago.setBonificaciones(dto.getBonificaciones());
        pago.setMontoFinal(dto.getMontoFinal());
        pago.setFechaPago(dto.getFechaPago());
        pago.setFechaEmision(dto.getFechaEmision());
        pago.setMetodoPago(dto.getMetodoPago());
        pago.setNumeroComprobante(dto.getNumeroComprobante());
        pago.setComprobanteUrl(dto.getComprobanteUrl());
        pago.setObservaciones(dto.getObservaciones());
        pago.setEstado(EstadoPago.PAGADO);
        
        return pago;
    }

    /**
     * Convertir entidad a DTO de respuesta
     */
    private PagoTrabajoExtraResponseDTO convertirEntidadADTO(PagoTrabajoExtraObra pago) {
        return PagoTrabajoExtraResponseDTO.builder()
            .id(pago.getId())
            .trabajoExtraId(pago.getTrabajoExtra().getId())
            .trabajoExtraNombre(pago.getTrabajoExtra().getNombre())
            .obraId(pago.getObra().getId())
            .obraNombre(pago.getObra().getNombre())
            .presupuestoNoClienteId(pago.getPresupuestoNoCliente() != null ? 
                pago.getPresupuestoNoCliente().getId() : null)
            .empresaId(pago.getEmpresaId())
            .tipoPago(pago.getTipoPago())
            .tipoPagoDisplay(pago.getTipoPago().getDisplayName())
            .trabajoExtroProfesionalId(pago.getTrabajoExtroProfesional() != null ? 
                pago.getTrabajoExtroProfesional().getId() : null)
            .profesionalNombre(pago.getTrabajoExtroProfesional() != null ? 
                pago.getTrabajoExtroProfesional().getNombre() : null)
            .trabajoExtraTareaId(pago.getTrabajoExtraTarea() != null ? 
                pago.getTrabajoExtraTarea().getId() : null)
            .tareaDescripcion(pago.getTrabajoExtraTarea() != null ? 
                pago.getTrabajoExtraTarea().getDescripcion() : null)
            .concepto(pago.getConcepto())
            .montoBase(pago.getMontoBase())
            .descuentos(pago.getDescuentos())
            .bonificaciones(pago.getBonificaciones())
            .montoFinal(pago.getMontoFinal())
            .fechaPago(pago.getFechaPago())
            .fechaEmision(pago.getFechaEmision())
            .estado(pago.getEstado())
            .estadoDisplay(pago.getEstado().name())
            .metodoPago(pago.getMetodoPago())
            .metodoPagoDisplay(pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null)
            .numeroComprobante(pago.getNumeroComprobante())
            .comprobanteUrl(pago.getComprobanteUrl())
            .observaciones(pago.getObservaciones())
            .motivoAnulacion(pago.getMotivoAnulacion())
            .fechaCreacion(pago.getFechaCreacion())
            .fechaModificacion(pago.getFechaModificacion())
            .usuarioCreacionId(pago.getUsuarioCreacionId())
            .usuarioModificacionId(pago.getUsuarioModificacionId())
            .build();
    }

    /**
     * Generar resumen completo de pagos de un trabajo extra
     */
    private ResumenPagosTrabajoExtraDTO generarResumen(TrabajoExtra trabajoExtra) {
        // Calcular importes
        BigDecimal importeProfesionales = trabajoExtra.getProfesionales().stream()
            .map(TrabajoExtroProfesional::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal importeTareas = trabajoExtra.getTareas().stream()
            .map(t -> t.getImporte() != null ? t.getImporte() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal importeTotal = importeProfesionales.add(importeTareas);
        
        // Calcular montos pagados
        BigDecimal montoPagadoTotal = pagoService.calcularTotalPagado(trabajoExtra.getId());
        
        BigDecimal montoPagadoProfesionales = trabajoExtra.getProfesionales().stream()
            .map(p -> pagoService.calcularTotalPagadoProfesional(p.getId()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal montoPagadoTareas = trabajoExtra.getTareas().stream()
            .map(t -> pagoService.calcularTotalPagadoTarea(t.getId()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal montoPendiente = importeTotal.subtract(montoPagadoTotal);
        
        // Calcular porcentaje pagado
        BigDecimal porcentajePagado = BigDecimal.ZERO;
        if (importeTotal.compareTo(BigDecimal.ZERO) > 0) {
            porcentajePagado = montoPagadoTotal
                .multiply(new BigDecimal("100"))
                .divide(importeTotal, 2, RoundingMode.HALF_UP);
        }
        
        // Contar pagos
        Long totalPagos = pagoRepository.countByTrabajoExtraId(trabajoExtra.getId());
        Long totalPagosProfesionales = trabajoExtra.getProfesionales().stream()
            .map(p -> pagoRepository.existsByTrabajoExtroProfesionalId(p.getId()) ? 1L : 0L)
            .reduce(0L, Long::sum);
        Long totalPagosTareas = trabajoExtra.getTareas().stream()
            .map(t -> pagoRepository.existsByTrabajoExtraTareaId(t.getId()) ? 1L : 0L)
            .reduce(0L, Long::sum);
        
        // Contar estados de profesionales
        Long profesionalesPendientes = trabajoExtra.getProfesionales().stream()
            .filter(p -> p.getEstadoPago() == EstadoPagoTrabajoExtra.PENDIENTE)
            .count();
        Long profesionalesPagadosParcial = trabajoExtra.getProfesionales().stream()
            .filter(p -> p.getEstadoPago() == EstadoPagoTrabajoExtra.PAGADO_PARCIAL)
            .count();
        Long profesionalesPagadosTotal = trabajoExtra.getProfesionales().stream()
            .filter(p -> p.getEstadoPago() == EstadoPagoTrabajoExtra.PAGADO_TOTAL)
            .count();
        
        // Contar estados de tareas
        Long tareasPendientes = trabajoExtra.getTareas().stream()
            .filter(t -> t.getEstadoPago() == EstadoPagoTrabajoExtra.PENDIENTE)
            .count();
        Long tareasPagadasParcial = trabajoExtra.getTareas().stream()
            .filter(t -> t.getEstadoPago() == EstadoPagoTrabajoExtra.PAGADO_PARCIAL)
            .count();
        Long tareasPagadasTotal = trabajoExtra.getTareas().stream()
            .filter(t -> t.getEstadoPago() == EstadoPagoTrabajoExtra.PAGADO_TOTAL)
            .count();
        
        return ResumenPagosTrabajoExtraDTO.builder()
            .trabajoExtraId(trabajoExtra.getId())
            .trabajoExtraNombre(trabajoExtra.getNombre())
            .importeTotalProfesionales(importeProfesionales)
            .importeTotalTareas(importeTareas)
            .importeTotal(importeTotal)
            .montoPagadoProfesionales(montoPagadoProfesionales)
            .montoPagadoTareas(montoPagadoTareas)
            .montoPagadoTotal(montoPagadoTotal)
            .montoPendiente(montoPendiente)
            .estadoPagoGeneral(trabajoExtra.getEstadoPagoGeneral())
            .estadoPagoGeneralDisplay(trabajoExtra.getEstadoPagoGeneral().getDisplayName())
            .totalPagos(totalPagos)
            .totalPagosProfesionales(totalPagosProfesionales)
            .totalPagosTareas(totalPagosTareas)
            .totalProfesionales((long) trabajoExtra.getProfesionales().size())
            .profesionalesPendientes(profesionalesPendientes)
            .profesionalesPagadosParcial(profesionalesPagadosParcial)
            .profesionalesPagadosTotal(profesionalesPagadosTotal)
            .totalTareas((long) trabajoExtra.getTareas().size())
            .tareasPendientes(tareasPendientes)
            .tareasPagadasParcial(tareasPagadasParcial)
            .tareasPagadasTotal(tareasPagadasTotal)
            .porcentajePagado(porcentajePagado)
            .build();
    }
}
