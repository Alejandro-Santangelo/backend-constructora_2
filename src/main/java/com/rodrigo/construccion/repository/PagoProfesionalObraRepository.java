package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.PagoProfesionalObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagoProfesionalObraRepository extends JpaRepository<PagoProfesionalObra, Long> {

    /**
     * Buscar todos los pagos de un profesional en una obra
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.profesionalObra.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<PagoProfesionalObra> findByProfesionalObraId(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Buscar pagos por profesional y tipo de pago
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago = :tipoPago " +
           "AND p.profesionalObra.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<PagoProfesionalObra> findByProfesionalObraIdAndTipoPago(
        @Param("profesionalObraId") Long profesionalObraId, 
        @Param("tipoPago") String tipoPago);

    /**
     * Buscar pagos por empresa
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.empresaId = :empresaId " +
           "AND (p.profesionalObra.obra IS NULL OR p.profesionalObra.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<PagoProfesionalObra> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Buscar adelantos de un profesional
     * EXCLUYE pagos de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago = 'ADELANTO' " +
           "AND p.estado = 'PAGADO' " +
           "AND p.profesionalObra.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<PagoProfesionalObra> findAdelantosByProfesional(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Calcular total de adelantos pendientes de descontar
     * (adelantos que aún no se han descontado completamente)
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago = 'ADELANTO' " +
           "AND p.estado = 'PAGADO' " +
           "AND p.id NOT IN (" +
           "    SELECT DISTINCT pago.id FROM PagoProfesionalObra pago " +
           "    WHERE pago.profesionalObra.id = :profesionalObraId " +
           "    AND pago.tipoPago = 'SEMANAL' " +
           "    AND pago.descuentoAdelantos > 0" +
           ")")
    BigDecimal calcularAdelantosPendientesDescuento(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Calcular total pagado a un profesional (todos los tipos de pago)
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoByProfesional(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Calcular total de pagos semanales de un profesional
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago = 'SEMANAL' " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalSemanalesByProfesional(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Buscar pagos por rango de fechas
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.fechaPago BETWEEN :desde AND :hasta")
    List<PagoProfesionalObra> findByFechaPagoBetween(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /**
     * Buscar pagos de un profesional por rango de fechas
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.fechaPago BETWEEN :desde AND :hasta")
    List<PagoProfesionalObra> findByProfesionalAndFechaBetween(
            @Param("profesionalObraId") Long profesionalObraId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /**
     * Calcular total pagado de honorarios por empresa (TODOS los tipos de pago)
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoProfesionalObra p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoByEmpresa(@Param("empresaId") Long empresaId);

    /**
     * Buscar pagos semanales de un profesional en un período específico
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago = 'SEMANAL' " +
           "AND p.periodoDesde = :periodoDesde " +
           "AND p.periodoHasta = :periodoHasta")
    List<PagoProfesionalObra> findPagoSemanalByPeriodo(
            @Param("profesionalObraId") Long profesionalObraId,
            @Param("periodoDesde") LocalDate periodoDesde,
            @Param("periodoHasta") LocalDate periodoHasta);

    /**
     * Verificar si ya existe un pago semanal para un período
     */
    @Query("SELECT COUNT(p) > 0 FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago = 'SEMANAL' " +
           "AND p.periodoDesde = :periodoDesde " +
           "AND p.periodoHasta = :periodoHasta " +
           "AND p.estado = 'PAGADO'")
    boolean existsPagoSemanalInPeriodo(
            @Param("profesionalObraId") Long profesionalObraId,
            @Param("periodoDesde") LocalDate periodoDesde,
            @Param("periodoHasta") LocalDate periodoHasta);

    /**
     * Obtener último pago de un profesional
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "ORDER BY p.fechaPago DESC, p.id DESC")
    List<PagoProfesionalObra> findUltimoPagoByProfesional(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Calcular total de premios y bonos de un profesional
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago IN ('PREMIO', 'BONO') " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPremiosBonosByProfesional(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Buscar pagos por método de pago
     */
    List<PagoProfesionalObra> findByMetodoPago(String metodoPago);

    /**
     * Contar pagos de un profesional por tipo
     */
    @Query("SELECT COUNT(p) FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago = :tipoPago " +
           "AND p.estado = 'PAGADO'")
    Long countPagosByTipo(
            @Param("profesionalObraId") Long profesionalObraId,
            @Param("tipoPago") String tipoPago);

    /**
     * Calcular promedio de presentismo de un profesional
     */
    @Query("SELECT AVG(p.porcentajePresentismo) FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.tipoPago = 'SEMANAL' " +
           "AND p.porcentajePresentismo IS NOT NULL " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularPromedioPresentismo(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Buscar pagos por empresa y tipo
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.tipoPago = :tipoPago")
    List<PagoProfesionalObra> findByEmpresaIdAndTipoPago(
        @Param("empresaId") Long empresaId,
        @Param("tipoPago") String tipoPago);

    /**
     * Buscar adelantos activos de un profesional con saldo pendiente.
     * Retorna adelantos ordenados por fecha (FIFO - First In, First Out).
     * 
     * @param profesionalObraId ID del profesional en obra
     * @return Lista de adelantos activos ordenados por fecha ascendente
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.profesionalObra.id = :profesionalObraId " +
           "AND p.esAdelanto = true " +
           "AND p.estadoAdelanto = 'ACTIVO' " +
           "AND p.saldoAdelantoPorDescontar > 0 " +
           "ORDER BY p.fechaPago ASC")
    List<PagoProfesionalObra> findAdelantosActivosByProfesionalObraId(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Calcular total pagado en una asignación específica (profesional-obra-rubro)
     * Suma todos los pagos donde asignacion_profesional_obra_id coincide
     * 
     * @param asignacionId ID de la asignación (profesional + obra + rubro)
     * @return Total pagado en esa asignación, 0 si no hay pagos
     */
    @Query("SELECT COALESCE(SUM(p.montoFinal), 0) FROM PagoProfesionalObra p " +
           "WHERE p.asignacion.id = :asignacionId " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoByAsignacion(@Param("asignacionId") Long asignacionId);

    /**
     * Obtener lista de pagos de una asignación específica (para historial)
     * 
     * @param asignacionId ID de la asignación
     * @return Lista de pagos ordenados por fecha descendente
     */
    @Query("SELECT p FROM PagoProfesionalObra p " +
           "WHERE p.asignacion.id = :asignacionId " +
           "AND p.estado = 'PAGADO' " +
           "ORDER BY p.fechaPago DESC")
    List<PagoProfesionalObra> findPagosByAsignacionId(@Param("asignacionId") Long asignacionId);

    /**
     * Contar pagos asociados a una asignación específica (SQL nativo para evitar filtros)
     * Útil para validar si se puede eliminar la asignación
     * 
     * @param asignacionId ID de la asignación
     * @return Cantidad de pagos asociados (incluye PAGADO, PENDIENTE, etc.)
     */
    @Query(value = "SELECT COUNT(*) FROM pagos_profesional_obra " +
                   "WHERE profesional_obra_id = :asignacionId", 
           nativeQuery = true)
    Long countByAsignacionId(@Param("asignacionId") Long asignacionId);
    
    /**
     * Eliminar todos los pagos asociados a una asignación (SQL nativo para evitar filtros)
     * Usado al eliminar una asignación para mantener integridad referencial
     * 
     * @param asignacionId ID de la asignación
     */
    @Modifying
    @Query(value = "DELETE FROM pagos_profesional_obra " +
                   "WHERE profesional_obra_id = :asignacionId", 
           nativeQuery = true)
    void deleteByAsignacionId(@Param("asignacionId") Long asignacionId);
}
