package com.rodrigo.construccion.service;

import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.enums.EstadoPagoTrabajoExtra;
import com.rodrigo.construccion.enums.TipoPagoTrabajoExtra;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service para gestión de pagos de trabajos extra
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PagoTrabajoExtraObraService {

    private final PagoTrabajoExtraObraRepository pagoRepository;
    private final TrabajoExtraRepository trabajoExtraRepository;
    private final TrabajoExtroProfesionalRepository profesionalRepository;
    private final TrabajoExtraTareaRepository tareaRepository;

    // ========== CRUD BÁSICO ==========

    /**
     * Crear un nuevo pago de trabajo extra
     */
    public PagoTrabajoExtraObra crearPago(PagoTrabajoExtraObra pago) {
        log.info("Creando nuevo pago de trabajo extra: {}", pago.getConcepto());
        
        // Validar
        pago.validar();
        
        // Guardar el pago
        PagoTrabajoExtraObra pagoGuardado = pagoRepository.save(pago);
        
        // Actualizar estados automáticamente
        actualizarEstadosPago(pago.getTrabajoExtra().getId());
        
        log.info("Pago creado exitosamente con ID: {}", pagoGuardado.getId());
        return pagoGuardado;
    }

    /**
     * Actualizar un pago existente
     */
    public PagoTrabajoExtraObra actualizarPago(Long id, PagoTrabajoExtraObra pagoActualizado) {
        log.info("Actualizando pago de trabajo extra ID: {}", id);
        
        PagoTrabajoExtraObra pagoExistente = pagoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
        
        // Actualizar campos
        pagoExistente.setConcepto(pagoActualizado.getConcepto());
        pagoExistente.setMontoBase(pagoActualizado.getMontoBase());
        pagoExistente.setDescuentos(pagoActualizado.getDescuentos());
        pagoExistente.setBonificaciones(pagoActualizado.getBonificaciones());
        pagoExistente.setMontoFinal(pagoActualizado.getMontoFinal());
        pagoExistente.setFechaPago(pagoActualizado.getFechaPago());
        pagoExistente.setMetodoPago(pagoActualizado.getMetodoPago());
        pagoExistente.setNumeroComprobante(pagoActualizado.getNumeroComprobante());
        pagoExistente.setComprobanteUrl(pagoActualizado.getComprobanteUrl());
        pagoExistente.setObservaciones(pagoActualizado.getObservaciones());
        pagoExistente.setEstado(pagoActualizado.getEstado());
        pagoExistente.setFechaModificacion(LocalDateTime.now());
        
        // Validar y guardar
        pagoExistente.validar();
        PagoTrabajoExtraObra pagoGuardado = pagoRepository.save(pagoExistente);
        
        // Actualizar estados
        actualizarEstadosPago(pagoExistente.getTrabajoExtra().getId());
        
        log.info("Pago actualizado exitosamente");
        return pagoGuardado;
    }

    /**
     * Eliminar un pago
     */
    public void eliminarPago(Long id) {
        log.info("Eliminando pago de trabajo extra ID: {}", id);
        
        PagoTrabajoExtraObra pago = pagoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
        
        Long trabajoExtraId = pago.getTrabajoExtra().getId();
        
        pagoRepository.deleteById(id);
        
        // Actualizar estados después de eliminar
        actualizarEstadosPago(trabajoExtraId);
        
        log.info("Pago eliminado exitosamente");
    }

    /**
     * Anular un pago (cambiar estado a ANULADO)
     */
    public PagoTrabajoExtraObra anularPago(Long id, String motivo) {
        log.info("Anulando pago de trabajo extra ID: {}", id);
        
        PagoTrabajoExtraObra pago = pagoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
        
        pago.setEstado(EstadoPago.ANULADO);
        pago.setMotivoAnulacion(motivo);
        pago.setFechaModificacion(LocalDateTime.now());
        
        PagoTrabajoExtraObra pagoGuardado = pagoRepository.save(pago);
        
        // Actualizar estados
        actualizarEstadosPago(pago.getTrabajoExtra().getId());
        
        log.info("Pago anulado exitosamente");
        return pagoGuardado;
    }

    /**
     * Obtener pago por ID
     */
    @Transactional(readOnly = true)
    public PagoTrabajoExtraObra obtenerPorId(Long id) {
        return pagoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    // ========== CONSULTAS ==========

    /**
     * Obtener todos los pagos de un trabajo extra
     */
    @Transactional(readOnly = true)
    public List<PagoTrabajoExtraObra> obtenerPagosPorTrabajoExtra(Long trabajoExtraId) {
        return pagoRepository.findByTrabajoExtraIdOrderByFechaPagoDesc(trabajoExtraId);
    }

    /**
     * Obtener todos los pagos de una obra
     */
    @Transactional(readOnly = true)
    public List<PagoTrabajoExtraObra> obtenerPagosPorObra(Long obraId) {
        return pagoRepository.findByObraId(obraId);
    }

    /**
     * Obtener todos los pagos de una empresa
     */
    @Transactional(readOnly = true)
    public List<PagoTrabajoExtraObra> obtenerPagosPorEmpresa(Long empresaId) {
        return pagoRepository.findByEmpresaId(empresaId);
    }

    /**
     * Obtener pagos de una empresa en un período
     */
    @Transactional(readOnly = true)
    public List<PagoTrabajoExtraObra> obtenerPagosPorEmpresaYPeriodo(
            Long empresaId, LocalDate fechaDesde, LocalDate fechaHasta) {
        return pagoRepository.findByEmpresaIdAndFechaPagoBetween(empresaId, fechaDesde, fechaHasta);
    }

    /**
     * Obtener pagos a un profesional específico
     */
    @Transactional(readOnly = true)
    public List<PagoTrabajoExtraObra> obtenerPagosPorProfesional(Long profesionalId) {
        return pagoRepository.findByTrabajoExtroProfesionalId(profesionalId);
    }

    /**
     * Obtener pagos de una tarea específica
     */
    @Transactional(readOnly = true)
    public List<PagoTrabajoExtraObra> obtenerPagosPorTarea(Long tareaId) {
        return pagoRepository.findByTrabajoExtraTareaId(tareaId);
    }

    // ========== CÁLCULOS ==========

    /**
     * Calcular total pagado de un trabajo extra
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPagado(Long trabajoExtraId) {
        return pagoRepository.calcularTotalPagadoTrabajoExtra(trabajoExtraId);
    }

    /**
     * Calcular total pagado a un profesional específico
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPagadoProfesional(Long profesionalId) {
        return pagoRepository.calcularTotalPagadoProfesional(profesionalId);
    }

    /**
     * Calcular total pagado de una tarea específica
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPagadoTarea(Long tareaId) {
        return pagoRepository.calcularTotalPagadoTarea(tareaId);
    }

    /**
     * Calcular total de pagos de trabajos extra de una obra
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPagadoObra(Long obraId) {
        return pagoRepository.calcularTotalPagadoObra(obraId);
    }

    // ========== ACTUALIZACIÓN DE ESTADOS ==========

    /**
     * Actualizar estados de pago de todos los elementos de un trabajo extra
     * Actualiza: TrabajoExtra, TrabajoExtroProfesional, TrabajoExtraTarea
     */
    public void actualizarEstadosPago(Long trabajoExtraId) {
        log.info("Actualizando estados de pago para trabajo extra ID: {}", trabajoExtraId);
        
        TrabajoExtra trabajoExtra = trabajoExtraRepository.findById(trabajoExtraId)
            .orElseThrow(() -> new RuntimeException("Trabajo extra no encontrado"));
        
        // Actualizar estado de cada profesional
        for (TrabajoExtroProfesional profesional : trabajoExtra.getProfesionales()) {
            actualizarEstadoProfesional(profesional);
        }
        
        // Actualizar estado de cada tarea
        for (TrabajoExtraTarea tarea : trabajoExtra.getTareas()) {
            actualizarEstadoTarea(tarea);
        }
        
        // Actualizar estado general del trabajo extra
        actualizarEstadoTrabajoExtra(trabajoExtra);
        
        log.info("Estados actualizados correctamente");
    }

    /**
     * Actualizar estado de pago de un profesional
     */
    private void actualizarEstadoProfesional(TrabajoExtroProfesional profesional) {
        BigDecimal totalPagado = calcularTotalPagadoProfesional(profesional.getId());
        BigDecimal importe = profesional.getImporte();
        
        EstadoPagoTrabajoExtra nuevoEstado = EstadoPagoTrabajoExtra.determinarEstado(importe, totalPagado);
        
        if (profesional.getEstadoPago() != nuevoEstado) {
            profesional.setEstadoPago(nuevoEstado);
            profesionalRepository.save(profesional);
            log.info("Estado de profesional {} actualizado a: {}", profesional.getId(), nuevoEstado);
        }
    }

    /**
     * Actualizar estado de pago de una tarea
     */
    private void actualizarEstadoTarea(TrabajoExtraTarea tarea) {
        BigDecimal totalPagado = calcularTotalPagadoTarea(tarea.getId());
        BigDecimal importe = tarea.getImporte() != null ? tarea.getImporte() : BigDecimal.ZERO;
        
        EstadoPagoTrabajoExtra nuevoEstado = EstadoPagoTrabajoExtra.determinarEstado(importe, totalPagado);
        
        if (tarea.getEstadoPago() != nuevoEstado) {
            tarea.setEstadoPago(nuevoEstado);
            tareaRepository.save(tarea);
            log.info("Estado de tarea {} actualizado a: {}", tarea.getId(), nuevoEstado);
        }
    }

    /**
     * Actualizar estado general del trabajo extra
     */
    private void actualizarEstadoTrabajoExtra(TrabajoExtra trabajoExtra) {
        BigDecimal totalImporte = calcularTotalImporteTrabajoExtra(trabajoExtra);
        BigDecimal totalPagado = calcularTotalPagado(trabajoExtra.getId());
        
        EstadoPagoTrabajoExtra nuevoEstado = EstadoPagoTrabajoExtra.determinarEstado(totalImporte, totalPagado);
        
        if (trabajoExtra.getEstadoPagoGeneral() != nuevoEstado) {
            trabajoExtra.setEstadoPagoGeneral(nuevoEstado);
            trabajoExtraRepository.save(trabajoExtra);
            log.info("Estado general del trabajo extra {} actualizado a: {}", trabajoExtra.getId(), nuevoEstado);
        }
    }

    /**
     * Calcular el importe total de un trabajo extra (profesionales + tareas)
     */
    private BigDecimal calcularTotalImporteTrabajoExtra(TrabajoExtra trabajoExtra) {
        BigDecimal totalProfesionales = trabajoExtra.getProfesionales().stream()
            .map(TrabajoExtroProfesional::getImporte)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalTareas = trabajoExtra.getTareas().stream()
            .map(t -> t.getImporte() != null ? t.getImporte() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalProfesionales.add(totalTareas);
    }

    // ========== PAGOS RÁPIDOS ==========

    /**
     * Crear pago general del trabajo extra completo
     */
    public PagoTrabajoExtraObra crearPagoGeneral(
            TrabajoExtra trabajoExtra, 
            BigDecimal monto,
            LocalDate fechaPago,
            String concepto) {
        
        PagoTrabajoExtraObra pago = new PagoTrabajoExtraObra();
        pago.setTrabajoExtra(trabajoExtra);
        pago.setObra(trabajoExtra.getObra());
        pago.setEmpresaId(trabajoExtra.getEmpresaId());
        pago.setTipoPago(TipoPagoTrabajoExtra.PAGO_GENERAL);
        pago.setConcepto(concepto);
        pago.setMontoBase(monto);
        pago.setMontoFinal(monto);
        pago.setFechaPago(fechaPago);
        pago.setEstado(EstadoPago.PAGADO);
        
        return crearPago(pago);
    }

    /**
     * Crear pago a un profesional específico
     */
    public PagoTrabajoExtraObra crearPagoProfesional(
            TrabajoExtroProfesional profesional,
            BigDecimal monto,
            LocalDate fechaPago,
            String concepto) {
        
        PagoTrabajoExtraObra pago = new PagoTrabajoExtraObra();
        pago.setTrabajoExtra(profesional.getTrabajoExtra());
        pago.setObra(profesional.getTrabajoExtra().getObra());
        pago.setEmpresaId(profesional.getTrabajoExtra().getEmpresaId());
        pago.setTrabajoExtroProfesional(profesional);
        pago.setTipoPago(TipoPagoTrabajoExtra.PAGO_PROFESIONAL);
        pago.setConcepto(concepto);
        pago.setMontoBase(monto);
        pago.setMontoFinal(monto);
        pago.setFechaPago(fechaPago);
        pago.setEstado(EstadoPago.PAGADO);
        
        return crearPago(pago);
    }

    /**
     * Crear pago por una tarea específica
     */
    public PagoTrabajoExtraObra crearPagoTarea(
            TrabajoExtraTarea tarea,
            BigDecimal monto,
            LocalDate fechaPago,
            String concepto) {
        
        PagoTrabajoExtraObra pago = new PagoTrabajoExtraObra();
        pago.setTrabajoExtra(tarea.getTrabajoExtra());
        pago.setObra(tarea.getTrabajoExtra().getObra());
        pago.setEmpresaId(tarea.getTrabajoExtra().getEmpresaId());
        pago.setTrabajoExtraTarea(tarea);
        pago.setTipoPago(TipoPagoTrabajoExtra.PAGO_TAREA);
        pago.setConcepto(concepto);
        pago.setMontoBase(monto);
        pago.setMontoFinal(monto);
        pago.setFechaPago(fechaPago);
        pago.setEstado(EstadoPago.PAGADO);
        
        return crearPago(pago);
    }
}
