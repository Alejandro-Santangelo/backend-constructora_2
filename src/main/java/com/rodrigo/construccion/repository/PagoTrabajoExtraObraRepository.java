package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.enums.TipoPagoTrabajoExtra;
import com.rodrigo.construccion.model.entity.PagoTrabajoExtraObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository para PagoTrabajoExtraObra
 * Tabla: pagos_trabajos_extra_obra
 */
@Repository
public interface PagoTrabajoExtraObraRepository extends JpaRepository<PagoTrabajoExtraObra, Long> {

    // ========== BÚSQUEDAS POR TRABAJO EXTRA ==========
    
    /**
     * Obtener todos los pagos de un trabajo extra
     */
    List<PagoTrabajoExtraObra> findByTrabajoExtraId(Long trabajoExtraId);
    
    /**
     * Obtener todos los pagos de un trabajo extra por estado
     */
    List<PagoTrabajoExtraObra> findByTrabajoExtraIdAndEstado(Long trabajoExtraId, EstadoPago estado);
    
    /**
     * Obtener todos los pagos de un trabajo extra ordenados por fecha
     */
    List<PagoTrabajoExtraObra> findByTrabajoExtraIdOrderByFechaPagoDesc(Long trabajoExtraId);

    // ========== BÚSQUEDAS POR OBRA ==========
    
    /**
     * Obtener todos los pagos de trabajos extra de una obra
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoTrabajoExtraObra p WHERE p.obra.id = :obraId " +
           "AND p.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<PagoTrabajoExtraObra> findByObraId(@Param("obraId") Long obraId);
    
    /**
     * Obtener todos los pagos de trabajos extra de una obra por estado
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoTrabajoExtraObra p WHERE p.obra.id = :obraId " +
           "AND p.estado = :estado " +
           "AND p.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<PagoTrabajoExtraObra> findByObraIdAndEstado(@Param("obraId") Long obraId, @Param("estado") EstadoPago estado);

    // ========== BÚSQUEDAS POR EMPRESA ==========
    
    /**
     * Obtener todos los pagos de una empresa
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoTrabajoExtraObra p WHERE p.empresaId = :empresaId " +
           "AND (p.obra IS NULL OR p.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<PagoTrabajoExtraObra> findByEmpresaId(@Param("empresaId") Long empresaId);
    
    /**
     * Obtener todos los pagos de una empresa por estado
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoTrabajoExtraObra p WHERE p.empresaId = :empresaId " +
           "AND p.estado = :estado " +
           "AND (p.obra IS NULL OR p.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<PagoTrabajoExtraObra> findByEmpresaIdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") EstadoPago estado);
    
    /**
     * Obtener todos los pagos de una empresa en un rango de fechas
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoTrabajoExtraObra p WHERE p.empresaId = :empresaId " +
           "AND p.fechaPago BETWEEN :fechaDesde AND :fechaHasta " +
           "AND (p.obra IS NULL OR p.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<PagoTrabajoExtraObra> findByEmpresaIdAndFechaPagoBetween(
        @Param("empresaId") Long empresaId, 
        @Param("fechaDesde") LocalDate fechaDesde, 
        @Param("fechaHasta") LocalDate fechaHasta);

    // ========== BÚSQUEDAS POR PROFESIONAL ==========
    
    /**
     * Obtener todos los pagos a un profesional específico de un trabajo extra
     */
    List<PagoTrabajoExtraObra> findByTrabajoExtroProfesionalId(Long profesionalId);
    
    /**
     * Obtener todos los pagos a un profesional específico por estado
     */
    List<PagoTrabajoExtraObra> findByTrabajoExtroProfesionalIdAndEstado(Long profesionalId, EstadoPago estado);

    // ========== BÚSQUEDAS POR TAREA ==========
    
    /**
     * Obtener todos los pagos de una tarea específica
     */
    List<PagoTrabajoExtraObra> findByTrabajoExtraTareaId(Long tareaId);
    
    /**
     * Obtener todos los pagos de una tarea específica por estado
     */
    List<PagoTrabajoExtraObra> findByTrabajoExtraTareaIdAndEstado(Long tareaId, EstadoPago estado);

    // ========== BÚSQUEDAS POR TIPO DE PAGO ==========
    
    /**
     * Obtener todos los pagos de un tipo específico
     */
    List<PagoTrabajoExtraObra> findByTipoPago(TipoPagoTrabajoExtra tipoPago);
    
    /**
     * Obtener todos los pagos de un trabajo extra por tipo
     */
    List<PagoTrabajoExtraObra> findByTrabajoExtraIdAndTipoPago(Long trabajoExtraId, TipoPagoTrabajoExtra tipoPago);

    // ========== CONSULTAS AGREGADAS ==========
    
    /**
     * Calcular el total pagado de un trabajo extra
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoTrabajoExtraObra p " +
           "WHERE p.trabajoExtra.id = :trabajoExtraId AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoTrabajoExtra(@Param("trabajoExtraId") Long trabajoExtraId);
    
    /**
     * Calcular el total pagado a un profesional específico de un trabajo extra
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoTrabajoExtraObra p " +
           "WHERE p.trabajoExtroProfesional.id = :profesionalId AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoProfesional(@Param("profesionalId") Long profesionalId);
    
    /**
     * Calcular el total pagado de una tarea específica
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoTrabajoExtraObra p " +
           "WHERE p.trabajoExtraTarea.id = :tareaId AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoTarea(@Param("tareaId") Long tareaId);
    
    /**
     * Calcular el total de pagos de trabajos extra de una obra
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoTrabajoExtraObra p " +
           "WHERE p.obra.id = :obraId AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoObra(@Param("obraId") Long obraId);
    
    /**
     * Calcular total pagado de trabajos extra por empresa en un período
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoTrabajoExtraObra p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.estado = 'PAGADO' " +
           "AND p.fechaPago BETWEEN :fechaDesde AND :fechaHasta")
    BigDecimal calcularTotalPagadoEmpresaPeriodo(
        @Param("empresaId") Long empresaId,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta);
    
    /**
     * Calcular total pagado de trabajos extra por empresa (TODOS los pagos)
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoTrabajoExtraObra p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoByEmpresa(@Param("empresaId") Long empresaId);

    // ========== CONSULTAS DE CONTEO ==========
    
    /**
     * Contar pagos de un trabajo extra
     */
    Long countByTrabajoExtraId(Long trabajoExtraId);
    
    /**
     * Contar pagos pendientes de un trabajo extra
     */
    Long countByTrabajoExtraIdAndEstado(Long trabajoExtraId, EstadoPago estado);
    
    /**
     * Contar pagos de una obra
     */
    Long countByObraId(Long obraId);
    
    /**
     * Verificar si existe al menos un pago para un profesional específico
     */
    boolean existsByTrabajoExtroProfesionalId(Long profesionalId);
    
    /**
     * Verificar si existe al menos un pago para una tarea específica
     */
    boolean existsByTrabajoExtraTareaId(Long tareaId);

    // ========== BÚSQUEDAS COMPLEJAS ==========
    
    /**
     * Obtener pagos de un trabajo extra con filtros múltiples
     */
    @Query("SELECT p FROM PagoTrabajoExtraObra p " +
           "WHERE p.trabajoExtra.id = :trabajoExtraId " +
           "AND (:tipoPago IS NULL OR p.tipoPago = :tipoPago) " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "ORDER BY p.fechaPago DESC")
    List<PagoTrabajoExtraObra> buscarConFiltros(
        @Param("trabajoExtraId") Long trabajoExtraId,
        @Param("tipoPago") TipoPagoTrabajoExtra tipoPago,
        @Param("estado") EstadoPago estado);
    
    /**
     * Obtener pagos de una obra en un rango de fechas con filtros
     */
    @Query("SELECT p FROM PagoTrabajoExtraObra p " +
           "WHERE p.obra.id = :obraId " +
           "AND p.fechaPago BETWEEN :fechaDesde AND :fechaHasta " +
           "AND (:estado IS NULL OR p.estado = :estado) " +
           "ORDER BY p.fechaPago DESC")
    List<PagoTrabajoExtraObra> buscarPorObraYPeriodo(
        @Param("obraId") Long obraId,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta,
        @Param("estado") EstadoPago estado);
}
