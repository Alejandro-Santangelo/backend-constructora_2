package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.CajaChicaMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository para gestionar movimientos de caja chica
 * Incluye queries optimizadas para consultas frecuentes
 */
@Repository
public interface CajaChicaMovimientoRepository extends JpaRepository<CajaChicaMovimiento, Long> {

    /**
     * Buscar todos los movimientos de un presupuesto
     * Ordenados por fecha descendente (más recientes primero)
     */
    List<CajaChicaMovimiento> findByPresupuestoIdAndEmpresaIdOrderByFechaDesc(Long presupuestoId, Long empresaId);

    /**
     * Buscar movimientos de un profesional específico en un presupuesto
     * Útil para ver el historial completo de un profesional
     */
    List<CajaChicaMovimiento> findByPresupuestoIdAndEmpresaIdAndProfesionalNombreAndProfesionalTipoOrderByFechaDesc(
        Long presupuestoId, 
        Long empresaId, 
        String profesionalNombre, 
        String profesionalTipo
    );

    /**
     * Calcular saldo actual de un profesional
     * Fórmula: SUMA(ASIGNACION) - SUMA(GASTO)
     * 
     * Retorna BigDecimal con el saldo actual:
     * - Positivo: tiene dinero disponible
     * - Cero: gastó todo
     * - Negativo: gastó de más (sobregiro)
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN m.tipo = 'ASIGNACION' THEN m.monto ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN m.tipo = 'GASTO' THEN m.monto ELSE 0 END), 0) " +
           "FROM CajaChicaMovimiento m " +
           "WHERE m.presupuestoId = :presupuestoId " +
           "AND m.empresaId = :empresaId " +
           "AND m.profesionalNombre = :profesionalNombre " +
           "AND m.profesionalTipo = :profesionalTipo")
    BigDecimal calcularSaldo(
        @Param("presupuestoId") Long presupuestoId,
        @Param("empresaId") Long empresaId,
        @Param("profesionalNombre") String profesionalNombre,
        @Param("profesionalTipo") String profesionalTipo
    );

    /**
     * Buscar movimientos por tipo (ASIGNACION o GASTO)
     * Útil para reportes segregados
     */
    List<CajaChicaMovimiento> findByPresupuestoIdAndEmpresaIdAndTipoOrderByFechaDesc(
        Long presupuestoId, 
        Long empresaId, 
        String tipo
    );

    /**
     * Calcular total de asignaciones de un profesional
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) " +
           "FROM CajaChicaMovimiento m " +
           "WHERE m.presupuestoId = :presupuestoId " +
           "AND m.empresaId = :empresaId " +
           "AND m.profesionalNombre = :profesionalNombre " +
           "AND m.profesionalTipo = :profesionalTipo " +
           "AND m.tipo = 'ASIGNACION'")
    BigDecimal calcularTotalAsignaciones(
        @Param("presupuestoId") Long presupuestoId,
        @Param("empresaId") Long empresaId,
        @Param("profesionalNombre") String profesionalNombre,
        @Param("profesionalTipo") String profesionalTipo
    );

    /**
     * Calcular total de gastos de un profesional
     */
    @Query("SELECT COALESCE(SUM(m.monto), 0) " +
           "FROM CajaChicaMovimiento m " +
           "WHERE m.presupuestoId = :presupuestoId " +
           "AND m.empresaId = :empresaId " +
           "AND m.profesionalNombre = :profesionalNombre " +
           "AND m.profesionalTipo = :profesionalTipo " +
           "AND m.tipo = 'GASTO'")
    BigDecimal calcularTotalGastos(
        @Param("presupuestoId") Long presupuestoId,
        @Param("empresaId") Long empresaId,
        @Param("profesionalNombre") String profesionalNombre,
        @Param("profesionalTipo") String profesionalTipo
    );
}
