package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.RetiroPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RetiroPersonalRepository extends JpaRepository<RetiroPersonal, Long> {

    /**
     * Buscar todos los retiros de una empresa ordenados por fecha descendente
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT r FROM RetiroPersonal r LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO')) " +
           "ORDER BY r.fechaRetiro DESC")
    List<RetiroPersonal> findByEmpresaIdOrderByFechaRetiroDesc(@Param("empresaId") Long empresaId);

    /**
     * Buscar retiros por empresa y estado
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT r FROM RetiroPersonal r LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId AND r.estado = :estado " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<RetiroPersonal> findByEmpresaIdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") String estado);

    /**
     * Buscar retiros por empresa y tipo
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT r FROM RetiroPersonal r LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId " +
           "AND r.tipoRetiro = :tipoRetiro " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO')) " +
           "ORDER BY r.fechaRetiro DESC")
    List<RetiroPersonal> findByEmpresaIdAndTipoRetiro(
        @Param("empresaId") Long empresaId,
        @Param("tipoRetiro") String tipoRetiro
    );

    /**
     * Buscar retiros por rango de fechas
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT r FROM RetiroPersonal r LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId " +
           "AND r.fechaRetiro BETWEEN :desde AND :hasta " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO')) " +
           "ORDER BY r.fechaRetiro DESC")
    List<RetiroPersonal> findByEmpresaIdAndFechaRange(
        @Param("empresaId") Long empresaId,
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
    );

    /**
     * Buscar retiros con múltiples filtros
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT r FROM RetiroPersonal r LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId " +
           "AND (:fechaDesde IS NULL OR r.fechaRetiro >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR r.fechaRetiro <= :fechaHasta) " +
           "AND (:tipoRetiro IS NULL OR r.tipoRetiro = :tipoRetiro) " +
           "AND (:estado IS NULL OR r.estado = :estado) " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO')) " +
           "ORDER BY r.fechaRetiro DESC")
    List<RetiroPersonal> findByFiltros(
        @Param("empresaId") Long empresaId,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta,
        @Param("tipoRetiro") String tipoRetiro,
        @Param("estado") String estado
    );

    /**
     * Calcular suma total de retiros por empresa y estado
     * CRÍTICO: Usar en cálculo de saldo disponible
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT COALESCE(SUM(r.monto), 0) FROM RetiroPersonal r " +
           "LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId AND r.estado = :estado " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    BigDecimal sumMontoByEmpresaIdAndEstado(
        @Param("empresaId") Long empresaId,
        @Param("estado") String estado
    );

    /**
     * Calcular suma total de retiros por empresa (todos los estados)
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT COALESCE(SUM(r.monto), 0) FROM RetiroPersonal r " +
           "LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    BigDecimal sumMontoByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Calcular totales agrupados por tipo de retiro
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT r.tipoRetiro as tipo, SUM(r.monto) as total " +
           "FROM RetiroPersonal r LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId AND r.estado = 'ACTIVO' " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO')) " +
           "GROUP BY r.tipoRetiro")
    List<TipoRetiroTotal> sumMontoByTipo(@Param("empresaId") Long empresaId);

    /**
     * Calcular totales agrupados por mes
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT YEAR(r.fechaRetiro) as anio, MONTH(r.fechaRetiro) as mes, " +
           "SUM(r.monto) as total, COUNT(r) as cantidad " +
           "FROM RetiroPersonal r LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId AND r.estado = 'ACTIVO' " +
           "AND (:fechaDesde IS NULL OR r.fechaRetiro >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR r.fechaRetiro <= :fechaHasta) " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO')) " +
           "GROUP BY YEAR(r.fechaRetiro), MONTH(r.fechaRetiro) " +
           "ORDER BY YEAR(r.fechaRetiro) DESC, MONTH(r.fechaRetiro) DESC")
    List<RetiroMensual> sumMontoByMes(
        @Param("empresaId") Long empresaId,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    /**
     * Contar retiros por estado
     * EXCLUYE retiros de obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT COUNT(r) FROM RetiroPersonal r " +
           "LEFT JOIN Obra o ON r.obraId = o.id " +
           "WHERE r.empresaId = :empresaId AND r.estado = :estado " +
           "AND (r.obraId IS NULL OR o.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    Long countByEmpresaIdAndEstado(
        @Param("empresaId") Long empresaId,
        @Param("estado") String estado
    );

    // ========== INTERFACES DE PROYECCIÓN ==========

    interface TipoRetiroTotal {
        String getTipo();
        BigDecimal getTotal();
    }

    interface RetiroMensual {
        Integer getAnio();
        Integer getMes();
        BigDecimal getTotal();
        Long getCantidad();
    }
}
