package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.enums.EstadoCobroEmpresa;
import com.rodrigo.construccion.model.entity.CobroEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad CobroEmpresa
 */
@Repository
public interface CobroEmpresaRepository extends JpaRepository<CobroEmpresa, Long> {

    /**
     * Buscar cobros por empresa
     */
    List<CobroEmpresa> findByEmpresaId(Long empresaId);

    /**
     * Buscar cobros por empresa y estado
     */
    List<CobroEmpresa> findByEmpresaIdAndEstado(Long empresaId, EstadoCobroEmpresa estado);

    /**
     * Buscar cobro por ID y empresa
     */
    Optional<CobroEmpresa> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Buscar cobros con saldo disponible
     */
    @Query("SELECT c FROM CobroEmpresa c WHERE c.empresaId = :empresaId " +
           "AND c.montoDisponible > 0 " +
           "AND c.estado != 'ANULADO' " +
           "ORDER BY c.fechaCobro DESC")
    List<CobroEmpresa> findCobrosDisponiblesByEmpresa(@Param("empresaId") Long empresaId);

    /**
     * Calcular total cobrado por empresa
     */
    @Query("SELECT COALESCE(SUM(c.montoTotal), 0) FROM CobroEmpresa c " +
           "WHERE c.empresaId = :empresaId " +
           "AND c.estado != 'ANULADO'")
    BigDecimal calcularTotalCobradoByEmpresa(@Param("empresaId") Long empresaId);

    /**
     * Calcular total asignado por empresa
     */
    @Query("SELECT COALESCE(SUM(c.montoAsignado), 0) FROM CobroEmpresa c " +
           "WHERE c.empresaId = :empresaId " +
           "AND c.estado != 'ANULADO'")
    BigDecimal calcularTotalAsignadoByEmpresa(@Param("empresaId") Long empresaId);

    /**
     * Calcular total disponible por empresa
     */
    @Query("SELECT COALESCE(SUM(c.montoDisponible), 0) FROM CobroEmpresa c " +
           "WHERE c.empresaId = :empresaId " +
           "AND c.estado != 'ANULADO'")
    BigDecimal calcularTotalDisponibleByEmpresa(@Param("empresaId") Long empresaId);

    /**
     * Contar cobros por estado
     */
    @Query("SELECT COUNT(c) FROM CobroEmpresa c " +
           "WHERE c.empresaId = :empresaId " +
           "AND c.estado = :estado")
    Long contarCobrosPorEstado(@Param("empresaId") Long empresaId, @Param("estado") EstadoCobroEmpresa estado);

    /**
     * Contar cobros disponibles (con saldo > 0)
     */
    @Query("SELECT COUNT(c) FROM CobroEmpresa c " +
           "WHERE c.empresaId = :empresaId " +
           "AND c.montoDisponible > 0 " +
           "AND c.estado != 'ANULADO'")
    Long contarCobrosDisponibles(@Param("empresaId") Long empresaId);

    /**
     * Buscar cobros ordenados por fecha descendente
     */
    List<CobroEmpresa> findByEmpresaIdOrderByFechaCobroDesc(Long empresaId);

    /**
     * Buscar cobros por empresa y estado ordenados por fecha
     */
    List<CobroEmpresa> findByEmpresaIdAndEstadoOrderByFechaCobroDesc(Long empresaId, EstadoCobroEmpresa estado);
}
