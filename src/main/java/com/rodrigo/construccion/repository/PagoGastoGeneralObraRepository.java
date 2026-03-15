package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.enums.TipoPagoConsolidado;
import com.rodrigo.construccion.model.entity.PagoGastoGeneralObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para pagos de gastos generales / otros costos.
 * Tabla: pagos_gastos_generales_obra
 */
@Repository
public interface PagoGastoGeneralObraRepository extends JpaRepository<PagoGastoGeneralObra, Long> {

    /**
     * Eliminar todos los pagos asociados a un presupuesto.
     */
    void deleteByPresupuestoNoClienteId(Long presupuestoNoClienteId);

    /**
     * Buscar pagos por presupuesto.
     */
    List<PagoGastoGeneralObra> findByPresupuestoNoClienteIdAndEmpresaIdOrderByFechaPagoDesc(
        Long presupuestoNoClienteId, Long empresaId);

    /**
     * Buscar pagos por item de calculadora.
     */
    List<PagoGastoGeneralObra> findByItemCalculadoraIdAndEmpresaIdOrderByFechaPagoDesc(
        Long itemCalculadoraId, Long empresaId);

    /**
     * Buscar pagos por presupuesto y tipo.
     */
    List<PagoGastoGeneralObra> findByPresupuestoNoClienteIdAndTipoPagoAndEmpresaIdOrderByFechaPagoDesc(
        Long presupuestoNoClienteId, TipoPagoConsolidado tipoPago, Long empresaId);

    /**
     * Buscar pagos por presupuesto y estado.
     */
    List<PagoGastoGeneralObra> findByPresupuestoNoClienteIdAndEstadoAndEmpresaIdOrderByFechaPagoDesc(
        Long presupuestoNoClienteId, EstadoPago estado, Long empresaId);

    /**
     * Buscar todos los pagos de gastos generales de una empresa.
     */
    List<PagoGastoGeneralObra> findByEmpresaIdOrderByFechaPagoDesc(Long empresaId);

    /**
     * Buscar pagos por gasto general específico.
     */
    List<PagoGastoGeneralObra> findByGastoGeneralIdAndEmpresaIdOrderByFechaPagoDesc(
        Long gastoGeneralId, Long empresaId);

    /**
     * Buscar pagos por rango de fechas.
     */
    @Query("SELECT p FROM PagoGastoGeneralObra p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY p.fechaPago DESC")
    List<PagoGastoGeneralObra> findByEmpresaIdAndFechaPagoBetween(
        @Param("empresaId") Long empresaId,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin);

    /**
     * Calcular total pagado por presupuesto.
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoGastoGeneralObra p " +
           "WHERE p.presupuestoNoCliente.id = :presupuestoId " +
           "AND p.empresaId = :empresaId " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoPorPresupuesto(
        @Param("presupuestoId") Long presupuestoId,
        @Param("empresaId") Long empresaId);

    /**
     * Calcular total pagado por gasto general.
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoGastoGeneralObra p " +
           "WHERE p.gastoGeneralId = :gastoGeneralId " +
           "AND p.empresaId = :empresaId " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoPorGastoGeneral(
        @Param("gastoGeneralId") Long gastoGeneralId,
        @Param("empresaId") Long empresaId);

    /**
     * Calcular total pagado por item calculadora.
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoGastoGeneralObra p " +
           "WHERE p.itemCalculadora.id = :itemId " +
           "AND p.empresaId = :empresaId " +
           "AND p.estado = 'PAGADO'")
    BigDecimal calcularTotalPagadoPorItem(
        @Param("itemId") Long itemId,
        @Param("empresaId") Long empresaId);

    /**
     * Buscar pagos por observaciones (para filtrar por semana).
     */
    @Query("SELECT p FROM PagoGastoGeneralObra p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.presupuestoNoCliente.id = :presupuestoId " +
           "AND p.observaciones LIKE %:patron% " +
           "ORDER BY p.fechaPago DESC")
    List<PagoGastoGeneralObra> findByObservacionesContaining(
        @Param("empresaId") Long empresaId,
        @Param("presupuestoId") Long presupuestoId,
        @Param("patron") String patron);

    /**
     * Verificar si existe pago duplicado.
     */
    @Query("SELECT COUNT(p) > 0 FROM PagoGastoGeneralObra p " +
           "WHERE p.presupuestoNoCliente.id = :presupuestoId " +
           "AND p.itemCalculadora.id = :itemId " +
           "AND p.gastoGeneralId = :gastoGeneralId " +
           "AND p.fechaPago = :fechaPago " +
           "AND p.monto = :monto " +
           "AND p.empresaId = :empresaId " +
           "AND p.estado != 'ANULADO'")
    boolean existePagoDuplicado(
        @Param("presupuestoId") Long presupuestoId,
        @Param("itemId") Long itemId,
        @Param("gastoGeneralId") Long gastoGeneralId,
        @Param("fechaPago") LocalDate fechaPago,
        @Param("monto") BigDecimal monto,
        @Param("empresaId") Long empresaId);

    /**
     * Buscar pagos por dirección de obra (calle + altura).
     */
    @Query("SELECT p FROM PagoGastoGeneralObra p " +
           "WHERE p.presupuestoNoCliente.direccionObraCalle LIKE %:calle% " +
           "AND p.presupuestoNoCliente.direccionObraAltura = :altura " +
           "AND p.empresaId = :empresaId " +
           "ORDER BY p.fechaPago DESC")
    List<PagoGastoGeneralObra> findByDireccionObra(
        @Param("calle") String calle,
        @Param("altura") String altura,
        @Param("empresaId") Long empresaId);
}
