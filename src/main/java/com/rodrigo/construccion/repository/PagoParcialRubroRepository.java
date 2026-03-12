package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.PagoParcialRubro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para pagos parciales sobre items de rubros
 */
@Repository
public interface PagoParcialRubroRepository extends JpaRepository<PagoParcialRubro, Long> {

    /**
     * Obtener todos los pagos de un presupuesto específico
     */
    List<PagoParcialRubro> findByPresupuestoIdAndEmpresaIdOrderByFechaRegistroDesc(Long presupuestoId, Long empresaId);

    /**
     * Obtener pagos de un rubro específico
     */
    List<PagoParcialRubro> findByPresupuestoIdAndEmpresaIdAndNombreRubroOrderByFechaRegistroDesc(
            Long presupuestoId, Long empresaId, String nombreRubro);

    /**
     * Obtener pagos de un item específico (rubro + tipo)
     */
    List<PagoParcialRubro> findByPresupuestoIdAndEmpresaIdAndNombreRubroAndTipoItemOrderByFechaRegistroDesc(
            Long presupuestoId, Long empresaId, String nombreRubro, String tipoItem);

    /**
     * Calcular total pagado de un item específico
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoParcialRubro p " +
           "WHERE p.presupuestoId = :presupuestoId " +
           "AND p.empresaId = :empresaId " +
           "AND p.nombreRubro = :nombreRubro " +
           "AND p.tipoItem = :tipoItem")
    BigDecimal calcularTotalPagadoItem(
            @Param("presupuestoId") Long presupuestoId,
            @Param("empresaId") Long empresaId,
            @Param("nombreRubro") String nombreRubro,
            @Param("tipoItem") String tipoItem);

    /**
     * Calcular total pagado de un rubro completo (todos sus items)
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoParcialRubro p " +
           "WHERE p.presupuestoId = :presupuestoId " +
           "AND p.empresaId = :empresaId " +
           "AND p.nombreRubro = :nombreRubro")
    BigDecimal calcularTotalPagadoRubro(
            @Param("presupuestoId") Long presupuestoId,
            @Param("empresaId") Long empresaId,
            @Param("nombreRubro") String nombreRubro);

    /**
     * Calcular total pagado del presupuesto completo
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoParcialRubro p " +
           "WHERE p.presupuestoId = :presupuestoId " +
           "AND p.empresaId = :empresaId")
    BigDecimal calcularTotalPagadoPresupuesto(
            @Param("presupuestoId") Long presupuestoId,
            @Param("empresaId") Long empresaId);

    /**
     * Obtener todos los pagos de la empresa
     */
    List<PagoParcialRubro> findByEmpresaIdOrderByFechaRegistroDesc(Long empresaId);

    /**
     * Verificar si existe al menos un pago para un item
     */
    boolean existsByPresupuestoIdAndEmpresaIdAndNombreRubroAndTipoItem(
            Long presupuestoId, Long empresaId, String nombreRubro, String tipoItem);

    /**
     * Contar pagos de un item específico
     */
    long countByPresupuestoIdAndEmpresaIdAndNombreRubroAndTipoItem(
            Long presupuestoId, Long empresaId, String nombreRubro, String tipoItem);
}
