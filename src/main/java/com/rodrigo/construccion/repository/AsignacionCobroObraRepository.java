package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.AsignacionCobroObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AsignacionCobroObraRepository extends JpaRepository<AsignacionCobroObra, Long> {

    /**
     * Buscar todas las asignaciones de un cobro específico
     */
    @Query("SELECT a FROM AsignacionCobroObra a WHERE a.cobroObra.id = :cobroId")
    List<AsignacionCobroObra> findByCobroObraId(@Param("cobroId") Long cobroId);

    /**
     * Buscar todas las asignaciones de una obra específica
     */
    @Query("SELECT a FROM AsignacionCobroObra a WHERE a.obra.id = :obraId")
    List<AsignacionCobroObra> findByObraId(@Param("obraId") Long obraId);

    /**
     * Buscar asignaciones activas de un cobro
     */
    @Query("SELECT a FROM AsignacionCobroObra a WHERE a.cobroObra.id = :cobroId AND a.estado = 'ACTIVA'")
    List<AsignacionCobroObra> findActivasByCobroObraId(@Param("cobroId") Long cobroId);

    /**
     * Buscar asignaciones activas de una obra
     */
    @Query("SELECT a FROM AsignacionCobroObra a WHERE a.obra.id = :obraId AND a.estado = 'ACTIVA'")
    List<AsignacionCobroObra> findActivasByObraId(@Param("obraId") Long obraId);

    /**
     * Calcular total asignado de un cobro
     */
    @Query("SELECT COALESCE(SUM(a.montoAsignado), 0) FROM AsignacionCobroObra a " +
           "WHERE a.cobroObra.id = :cobroId AND a.estado = 'ACTIVA'")
    BigDecimal calcularTotalAsignadoByCobro(@Param("cobroId") Long cobroId);

    /**
     * Calcular total recibido por una obra desde todos los cobros
     */
    @Query("SELECT COALESCE(SUM(a.montoAsignado), 0) FROM AsignacionCobroObra a " +
           "WHERE a.obra.id = :obraId AND a.estado = 'ACTIVA'")
    BigDecimal calcularTotalRecibidoByObra(@Param("obraId") Long obraId);

    /**
     * Buscar asignaciones por empresa
     */
    List<AsignacionCobroObra> findByEmpresaId(Long empresaId);

    /**
     * Buscar asignaciones por empresa y estado
     */
    List<AsignacionCobroObra> findByEmpresaIdAndEstado(Long empresaId, String estado);

    /**
     * Buscar asignaciones por presupuesto
     */
    @Query("SELECT a FROM AsignacionCobroObra a WHERE a.presupuestoNoCliente.id = :presupuestoId")
    List<AsignacionCobroObra> findByPresupuestoNoClienteId(@Param("presupuestoId") Long presupuestoId);

    /**
     * Calcular totales por item de una obra (suma de todas las asignaciones)
     */
    @Query("SELECT COALESCE(SUM(a.montoProfesionales), 0) FROM AsignacionCobroObra a " +
           "WHERE a.obra.id = :obraId AND a.estado = 'ACTIVA'")
    BigDecimal calcularTotalProfesionalesByObra(@Param("obraId") Long obraId);

    @Query("SELECT COALESCE(SUM(a.montoMateriales), 0) FROM AsignacionCobroObra a " +
           "WHERE a.obra.id = :obraId AND a.estado = 'ACTIVA'")
    BigDecimal calcularTotalMaterialesByObra(@Param("obraId") Long obraId);

    @Query("SELECT COALESCE(SUM(a.montoGastosGenerales), 0) FROM AsignacionCobroObra a " +
           "WHERE a.obra.id = :obraId AND a.estado = 'ACTIVA'")
    BigDecimal calcularTotalGastosGeneralesByObra(@Param("obraId") Long obraId);

    /**
     * NUEVO: Calcular suma total de asignaciones por empresa y estado
     * CRÍTICO para cálculo de saldo disponible en retiros personales
     */
    @Query("SELECT COALESCE(SUM(a.montoAsignado), 0) FROM AsignacionCobroObra a " +
           "WHERE a.empresaId = :empresaId AND a.estado = :estado")
    BigDecimal sumMontoAsignadoByEmpresaIdAndEstado(
        @Param("empresaId") Long empresaId,
        @Param("estado") String estado
    );

    /**
     * Contar asignaciones de un cobro obra
     */
    @Query("SELECT COUNT(a) FROM AsignacionCobroObra a WHERE a.cobroObra.id = :cobroId")
    Long countByCobroObraId(@Param("cobroId") Long cobroId);
}
