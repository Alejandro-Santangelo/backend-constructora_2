package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.MovimientoMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad MovimientoMaterial
 * 
 * Maneja las consultas de movimientos de materiales por empresa y obra.
 * Implementa las consultas equivalentes a las del SQL original.
 */
@Repository
public interface MovimientoMaterialRepository extends JpaRepository<MovimientoMaterial, Long> {

    /**
     * CONSULTAS MULTI-TENANT
     */
    
    /**
     * Buscar movimientos por empresa
     */
    @Query("SELECT mm FROM MovimientoMaterial mm WHERE mm.empresa.id = :empresaId")
    List<MovimientoMaterial> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Buscar movimientos por obra
     */
    @Query("SELECT mm FROM MovimientoMaterial mm WHERE mm.obraId = :obraId")
    List<MovimientoMaterial> findByObraId(@Param("obraId") Long obraId);

    /**
     * Buscar movimientos por obra y empresa
     */
    @Query("SELECT mm FROM MovimientoMaterial mm WHERE mm.obraId = :obraId AND mm.empresa.id = :empresaId")
    List<MovimientoMaterial> findByObraIdAndEmpresaId(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);

    /**
     * CONSULTAS POR TIPO DE MOVIMIENTO
     */
    
    /**
     * Movimientos de entrada por obra (equivalente a las consultas SQL de entrada de materiales)
     */
    @Query("SELECT mm FROM MovimientoMaterial mm WHERE mm.obraId = :obraId AND mm.tipoMovimiento = 'ENTRADA' " +
           "ORDER BY mm.fechaMovimiento DESC")
    List<MovimientoMaterial> findEntradaByObraId(@Param("obraId") Long obraId);

    /**
     * Movimientos de salida por obra (equivalente a las consultas SQL de salida de materiales)
     */
    @Query("SELECT mm FROM MovimientoMaterial mm WHERE mm.obraId = :obraId AND mm.tipoMovimiento = 'SALIDA' " +
           "ORDER BY mm.fechaMovimiento DESC")
    List<MovimientoMaterial> findSalidaByObraId(@Param("obraId") Long obraId);

    /**
     * CONSULTAS DE RANGO DE FECHAS (consulta 27 del SQL original)
     */
    
    /**
     * Movimientos por rango de fechas y empresa
     */
    @Query("SELECT mm FROM MovimientoMaterial mm " +
           "WHERE mm.empresa.id = :empresaId " +
           "AND mm.fechaMovimiento BETWEEN :fechaDesde AND :fechaHasta " +
           "ORDER BY mm.fechaMovimiento DESC")
    List<MovimientoMaterial> findByEmpresaIdAndFechaBetween(@Param("empresaId") Long empresaId,
                                                            @Param("fechaDesde") LocalDateTime fechaDesde,
                                                            @Param("fechaHasta") LocalDateTime fechaHasta);

    /**
     * CONSULTAS DE MATERIALES MÁS UTILIZADOS (consulta 28 del SQL original)
     */
    
    /**
     * Materiales más utilizados por empresa
     */
    @Query("SELECT m.nombre, SUM(mm.cantidad) as totalUtilizado " +
           "FROM MovimientoMaterial mm " +
           "JOIN mm.material m " +
           "WHERE mm.empresa.id = :empresaId AND mm.tipoMovimiento = 'SALIDA' " +
           "GROUP BY m.nombre " +
           "ORDER BY totalUtilizado DESC")
    List<Object[]> findMaterialesMasUtilizados(@Param("empresaId") Long empresaId);

    /**
     * Top 10 materiales más utilizados por empresa
     */
    @Query("SELECT m.nombre, SUM(mm.cantidad) as totalUtilizado " +
           "FROM MovimientoMaterial mm " +
           "JOIN mm.material m " +
           "WHERE mm.empresa.id = :empresaId AND mm.tipoMovimiento = 'SALIDA' " +
           "GROUP BY m.nombre " +
           "ORDER BY totalUtilizado DESC " +
           "LIMIT 10")
    List<Object[]> findTop10MaterialesMasUtilizados(@Param("empresaId") Long empresaId);

    /**
     * CONSULTAS FINANCIERAS
     */
    
    /**
     * Valor total de materiales utilizados por obra
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN mm.tipoMovimiento = 'SALIDA' THEN mm.cantidad * mm.precioUnitario ELSE 0 END), 0) " +
           "FROM MovimientoMaterial mm WHERE mm.obraId = :obraId")
    Double getTotalMaterialesUtilizadosPorObra(@Param("obraId") Long obraId);

    /**
     * CONSULTAS DE INVENTARIO
     */
    
    /**
     * Últimos movimientos por material y obra
     */
    @Query("SELECT mm FROM MovimientoMaterial mm " +
           "WHERE mm.material.id = :materialId AND mm.obraId = :obraId " +
           "ORDER BY mm.fechaMovimiento DESC")
    List<MovimientoMaterial> findByMaterialIdAndObraIdOrderByFechaDesc(@Param("materialId") Long materialId, 
                                                                       @Param("obraId") Long obraId);

    /**
     * VALIDACIONES Y UTILIDADES
     */
    
    /**
     * Contar movimientos por empresa
     */
    @Query("SELECT COUNT(mm) FROM MovimientoMaterial mm WHERE mm.empresa.id = :empresaId")
    long countByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Buscar movimiento por ID y empresa
     */
    @Query("SELECT mm FROM MovimientoMaterial mm WHERE mm.id = :id AND mm.empresa.id = :empresaId")
    Optional<MovimientoMaterial> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Buscar movimientos recientes por empresa
     */
    @Query("SELECT mm FROM MovimientoMaterial mm " +
           "WHERE mm.empresa.id = :empresaId " +
           "ORDER BY mm.fechaMovimiento DESC")
    Page<MovimientoMaterial> findRecentesByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    /**
     * CONSULTAS ESPECÍFICAS PARA DASHBOARD
     */
    
    /**
     * Actividad reciente de materiales por empresa (para consulta 70 del SQL original)
     */
    @Query("SELECT 'Material' as tipo, m.nombre, mm.fechaMovimiento as fecha, mm.observaciones " +
           "FROM MovimientoMaterial mm " +
           "JOIN mm.material m " +
           "WHERE mm.empresa.id = :empresaId " +
           "ORDER BY mm.fechaMovimiento DESC")
    List<Object[]> findActividadRecienteMateriales(@Param("empresaId") Long empresaId, Pageable pageable);

    /**
     * CONSULTAS POR ESTADO
     */
    
    /**
     * Movimientos por estado
     */
    @Query("SELECT mm FROM MovimientoMaterial mm " +
           "WHERE mm.empresa.id = :empresaId AND mm.estado = :estado " +
           "ORDER BY mm.fechaMovimiento DESC")
    List<MovimientoMaterial> findByEmpresaIdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") String estado);

    /**
     * MÉTODOS PARA STOCK CALCULATION
     */
    
    /**
     * Calcular stock actual por material y obra
     */
    @Query("SELECT " +
           "COALESCE(SUM(CASE WHEN mm.tipoMovimiento = 'ENTRADA' THEN mm.cantidad ELSE 0 END), 0) - " +
           "COALESCE(SUM(CASE WHEN mm.tipoMovimiento = 'SALIDA' THEN mm.cantidad ELSE 0 END), 0) as stockActual " +
           "FROM MovimientoMaterial mm " +
           "WHERE mm.material.id = :materialId AND mm.obraId = :obraId")
    Double getStockActualPorMaterialYObra(@Param("materialId") Long materialId, @Param("obraId") Long obraId);

    /**
     * CONSULTAS DE BÚSQUEDA
     */
    
    /**
     * Buscar movimientos por observaciones
     */
    @Query("SELECT mm FROM MovimientoMaterial mm " +
           "WHERE mm.empresa.id = :empresaId " +
           "AND (LOWER(mm.observaciones) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "OR LOWER(mm.motivo) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "ORDER BY mm.fechaMovimiento DESC")
    Page<MovimientoMaterial> findByEmpresaIdAndTextoContaining(@Param("empresaId") Long empresaId, 
                                                               @Param("texto") String texto, 
                                                               Pageable pageable);

    /**
     * Calcular cantidad total utilizada (salidas) por material catálogo y obra
     */
    @Query("SELECT COALESCE(SUM(mm.cantidad), 0) " +
           "FROM MovimientoMaterial mm " +
           "WHERE mm.material.id = :materialCatalogoId " +
           "AND mm.obraId = :obraId " +
           "AND mm.tipoMovimiento = 'SALIDA' " +
           "AND mm.estado != 'CANCELADO'")
    Double getCantidadUtilizadaPorMaterialCatalogoYObra(@Param("materialCatalogoId") Long materialCatalogoId, 
                                                         @Param("obraId") Long obraId);
}