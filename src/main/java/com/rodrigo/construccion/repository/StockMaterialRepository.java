package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.StockMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad StockMaterial
 * 
 * Implementa las consultas de stock equivalentes a las vistas SQL originales.
 * Especialmente para v_stock_actual y consultas de inventario.
 */
@Repository
public interface StockMaterialRepository extends JpaRepository<StockMaterial, Long> {

    /**
     * CONSULTAS MULTI-TENANT
     */
    
    /**
     * Buscar stock por empresa
     */
    @Query("SELECT sm FROM StockMaterial sm WHERE sm.empresa.id = :empresaId")
    List<StockMaterial> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Buscar stock por material
     */
    @Query("SELECT sm FROM StockMaterial sm WHERE sm.material.id = :materialId")
    List<StockMaterial> findByMaterialId(@Param("materialId") Long materialId);

    /**
     * Buscar stock por material y empresa
     */
    @Query("SELECT sm FROM StockMaterial sm WHERE sm.material.id = :materialId AND sm.empresa.id = :empresaId")
    List<StockMaterial> findByMaterialIdAndEmpresaId(@Param("materialId") Long materialId, @Param("empresaId") Long empresaId);

    /**
     * CONSULTAS DE STOCK ACTUAL (equivalente a v_stock_actual - consulta 25 del SQL original)
     */
    
    /**
     * Stock actual en todas las obras por empresa
     */
    @Query("SELECT sm.id, m.nombre as materialNombre, sm.ubicacion, sm.cantidadActual as cantidadDisponible, " +
           "m.unidadMedida, sm.precioUnitarioPromedio, " +
           "(sm.cantidadActual * sm.precioUnitarioPromedio) as valorTotal " +
           "FROM StockMaterial sm " +
           "JOIN sm.material m " +
           "WHERE sm.empresa.id = :empresaId AND sm.estado = 'ACTIVO' " +
           "ORDER BY m.nombre, sm.ubicacion")
    List<Object[]> findStockActualByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * CONSULTAS DE STOCK BAJO (equivalente a consulta 26 del SQL original)
     */
    
    /**
     * Materiales con stock bajo por empresa
     */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND sm.cantidadActual < COALESCE(sm.cantidadMinima, 10) " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.cantidadActual ASC")
    List<StockMaterial> findStockBajoByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Materiales con stock menor a una cantidad específica
     */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND sm.cantidadActual < :cantidadMinima " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.cantidadActual ASC")
    List<StockMaterial> findStockMenorA(@Param("empresaId") Long empresaId, @Param("cantidadMinima") Double cantidadMinima);

    /**
     * CONSULTAS DE VALOR DE INVENTARIO (equivalente a consulta 29 del SQL original)
     */
    
    /**
     * Valor total del inventario por obra y empresa
     */
    @Query("SELECT sm.ubicacion as obra, SUM(sm.cantidadActual * sm.precioUnitarioPromedio) as valorTotal " +
           "FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId AND sm.estado = 'ACTIVO' " +
           "GROUP BY sm.ubicacion " +
           "ORDER BY valorTotal DESC")
    List<Object[]> findValorInventarioPorObra(@Param("empresaId") Long empresaId);

    /**
     * Valor total del inventario por empresa
     */
    @Query("SELECT COALESCE(SUM(sm.cantidadActual * sm.precioUnitarioPromedio), 0) " +
           "FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId AND sm.estado = 'ACTIVO'")
    Double getValorTotalInventario(@Param("empresaId") Long empresaId);

    /**
     * CONSULTAS POR UBICACIÓN
     */
    
    /**
     * Stock por ubicación específica
     */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND LOWER(sm.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%')) " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.material.nombre")
    List<StockMaterial> findByEmpresaIdAndUbicacionContaining(@Param("empresaId") Long empresaId, 
                                                              @Param("ubicacion") String ubicacion);

    /**
     * CONSULTAS DE MATERIALES ESPECÍFICOS
     */
    
    /**
     * Stock de un material específico en todas las ubicaciones
     */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.material.id = :materialId " +
           "AND sm.empresa.id = :empresaId " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.cantidadActual DESC")
    List<StockMaterial> findStockByMaterialIdAndEmpresaId(@Param("materialId") Long materialId, 
                                                          @Param("empresaId") Long empresaId);

    /**
     * VALIDACIONES Y UTILIDADES
     */
    
    /**
     * Buscar stock por ID y empresa
     */
    @Query("SELECT sm FROM StockMaterial sm WHERE sm.id = :id AND sm.empresa.id = :empresaId")
    Optional<StockMaterial> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Verificar si existe stock para un material en una ubicación
     */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.material.id = :materialId " +
           "AND sm.empresa.id = :empresaId " +
           "AND LOWER(sm.ubicacion) = LOWER(:ubicacion) " +
           "AND sm.estado = 'ACTIVO'")
    Optional<StockMaterial> findByMaterialIdAndEmpresaIdAndUbicacion(@Param("materialId") Long materialId,
                                                                     @Param("empresaId") Long empresaId,
                                                                     @Param("ubicacion") String ubicacion);

    /**
     * CONSULTAS PARA DASHBOARD
     */
    
    /**
     * Contar materiales con stock bajo
     */
    @Query("SELECT COUNT(sm) FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND sm.cantidadActual < COALESCE(sm.cantidadMinima, 5) " +
           "AND sm.estado = 'ACTIVO'")
    long countMaterialesStockBajo(@Param("empresaId") Long empresaId);

    /**
     * Resumen de stock por empresa
     */
    @Query("SELECT " +
           "COUNT(DISTINCT sm.id) as totalItems, " +
           "COUNT(DISTINCT sm.material.id) as totalMateriales, " +
           "COALESCE(SUM(sm.cantidadActual * sm.precioUnitarioPromedio), 0) as valorTotal, " +
           "COUNT(CASE WHEN sm.cantidadActual < COALESCE(sm.cantidadMinima, 5) THEN 1 END) as stockBajo " +
           "FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId AND sm.estado = 'ACTIVO'")
    Object[] getResumenStockByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * CONSULTAS DE BÚSQUEDA
     */
    
    /**
     * Buscar stock por nombre de material
     */
    @Query("SELECT sm FROM StockMaterial sm " +
           "JOIN sm.material m " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombreMaterial, '%')) " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY m.nombre, sm.ubicacion")
    Page<StockMaterial> findByEmpresaIdAndMaterialNombreContaining(@Param("empresaId") Long empresaId,
                                                                   @Param("nombreMaterial") String nombreMaterial,
                                                                   Pageable pageable);

    /**
     * CONSULTAS DE MATERIALES AGOTADOS
     */
    
    /**
     * Materiales agotados o sin stock
     */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND (sm.cantidadActual IS NULL OR sm.cantidadActual <= 0) " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.material.nombre")
    List<StockMaterial> findMaterialesAgotados(@Param("empresaId") Long empresaId);

    /**
     * CONSULTAS DE STOCKS POR PAGINACIÓN
     */
    
    /**
     * Stock por empresa con paginación
     */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.material.nombre, sm.ubicacion")
    Page<StockMaterial> findByEmpresaIdWithPagination(@Param("empresaId") Long empresaId, Pageable pageable);
}