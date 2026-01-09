package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.enums.TipoPagoConsolidado;
import com.rodrigo.construccion.model.entity.PagoConsolidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para pagos consolidados (materiales, gastos generales, otros).
 */
@Repository
public interface PagoConsolidadoRepository extends JpaRepository<PagoConsolidado, Long> {

    /**
     * Buscar pagos por presupuesto.
     */
    List<PagoConsolidado> findByPresupuestoNoClienteIdAndEmpresaIdOrderByFechaPagoDesc(
        Long presupuestoNoClienteId, Long empresaId);

    /**
     * Buscar pagos por item de calculadora.
     */
    List<PagoConsolidado> findByItemCalculadoraIdAndEmpresaIdOrderByFechaPagoDesc(
        Long itemCalculadoraId, Long empresaId);

    /**
     * Buscar pagos por presupuesto y tipo.
     */
    List<PagoConsolidado> findByPresupuestoNoClienteIdAndTipoPagoAndEmpresaIdOrderByFechaPagoDesc(
        Long presupuestoNoClienteId, TipoPagoConsolidado tipoPago, Long empresaId);

    /**
     * Buscar pagos por presupuesto y estado.
     */
    List<PagoConsolidado> findByPresupuestoNoClienteIdAndEstadoAndEmpresaIdOrderByFechaPagoDesc(
        Long presupuestoNoClienteId, EstadoPago estado, Long empresaId);

    /**
     * Buscar pagos por rango de fechas.
     */
    @Query("SELECT p FROM PagoConsolidado p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.fechaPago BETWEEN :fechaDesde AND :fechaHasta " +
           "ORDER BY p.fechaPago DESC")
    List<PagoConsolidado> findByFechaRange(
        @Param("empresaId") Long empresaId,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta);

    /**
     * Verificar si existe un pago duplicado.
     * Se considera duplicado si coincide: presupuesto + item + concepto + fecha + monto (±0.01)
     */
    @Query("SELECT p FROM PagoConsolidado p " +
           "WHERE p.presupuestoNoCliente.id = :presupuestoId " +
           "AND p.itemCalculadora.id = :itemId " +
           "AND p.concepto = :concepto " +
           "AND p.fechaPago = :fecha " +
           "AND ABS(p.monto - :monto) < 0.01 " +
           "AND p.estado != 'ANULADO' " +
           "AND p.empresaId = :empresaId")
    Optional<PagoConsolidado> findDuplicado(
        @Param("presupuestoId") Long presupuestoId,
        @Param("itemId") Long itemId,
        @Param("concepto") String concepto,
        @Param("fecha") LocalDate fecha,
        @Param("monto") BigDecimal monto,
        @Param("empresaId") Long empresaId);

    /**
     * Calcular totales por tipo de pago.
     */
    @Query("SELECT p.tipoPago, SUM(p.monto) FROM PagoConsolidado p " +
           "WHERE p.presupuestoNoCliente.id = :presupuestoId " +
           "AND p.estado = 'PAGADO' " +
           "AND p.empresaId = :empresaId " +
           "GROUP BY p.tipoPago")
    List<Object[]> calcularTotalesPorTipo(
        @Param("presupuestoId") Long presupuestoId,
        @Param("empresaId") Long empresaId);

    /**
     * Calcular total general de pagos.
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoConsolidado p " +
           "WHERE p.presupuestoNoCliente.id = :presupuestoId " +
           "AND p.estado = 'PAGADO' " +
           "AND p.empresaId = :empresaId")
    BigDecimal calcularTotalGeneral(
        @Param("presupuestoId") Long presupuestoId,
        @Param("empresaId") Long empresaId);

    /**
     * Buscar pagos por dirección de obra.
     */
    @Query("SELECT p FROM PagoConsolidado p " +
           "JOIN p.presupuestoNoCliente pnc " +
           "WHERE pnc.direccionObraCalle = :calle " +
           "AND pnc.direccionObraAltura = :altura " +
           "AND p.empresaId = :empresaId " +
           "ORDER BY p.fechaPago DESC")
    List<PagoConsolidado> findByDireccionObra(
        @Param("calle") String calle,
        @Param("altura") String altura,
        @Param("empresaId") Long empresaId);

    /**
     * Contar pagos activos (no anulados).
     */
    @Query("SELECT COUNT(p) FROM PagoConsolidado p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.estado != 'ANULADO'")
    Long countPagosActivos(@Param("empresaId") Long empresaId);

    /**
     * Calcular total pagado de materiales y gastos generales por empresa.
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoConsolidado p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoByEmpresa(@Param("empresaId") Long empresaId);

    /**
     * Listar todos los pagos de materiales por empresa.
     */
    List<PagoConsolidado> findByEmpresaIdOrderByFechaPagoDesc(Long empresaId);
}
