package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.StockMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockMaterialRepository extends JpaRepository<StockMaterial, Long> {

    /* Buscar stock por empresa  */
    @Query("SELECT sm FROM StockMaterial sm WHERE sm.empresa.id = :empresaId")
    List<StockMaterial> findByEmpresaId(@Param("empresaId") Long empresaId);

    /* Buscar stock por material y empresa  */
    @Query("SELECT sm FROM StockMaterial sm WHERE sm.material.id = :materialId AND sm.empresa.id = :empresaId")
    List<StockMaterial> findByMaterialIdAndEmpresaId(@Param("materialId") Long materialId, @Param("empresaId") Long empresaId);
    
    /* Materiales con stock bajo por empresa */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND sm.cantidadActual < COALESCE(sm.cantidadMinima, 10) " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.cantidadActual ASC")
    List<StockMaterial> findStockBajoByEmpresaId(@Param("empresaId") Long empresaId);

    /* Materiales con stock menor a una cantidad específica */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND sm.cantidadActual < :cantidadMinima " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.cantidadActual ASC")
    List<StockMaterial> findStockMenorA(@Param("empresaId") Long empresaId, @Param("cantidadMinima") Double cantidadMinima);

    /* Materiales próximos a vencer */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND sm.fechaVencimiento IS NOT NULL " +
           "AND sm.fechaVencimiento <= :fechaLimite " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.fechaVencimiento ASC")
    List<StockMaterial> findStockProximoAVencer(@Param("empresaId") Long empresaId, @Param("fechaLimite") java.time.LocalDate fechaLimite);

    /* Stock por ubicación específica */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.empresa.id = :empresaId " +
           "AND LOWER(sm.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%')) " +
           "AND sm.estado = 'ACTIVO' " +
           "ORDER BY sm.material.nombre")
    List<StockMaterial> findByEmpresaIdAndUbicacionContaining(@Param("empresaId") Long empresaId, 
                                                              @Param("ubicacion") String ubicacion);

    /* Buscar stock por ID y empresa */
    @Query("SELECT sm FROM StockMaterial sm WHERE sm.id = :id AND sm.empresa.id = :empresaId")
    Optional<StockMaterial> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /* Verificar si existe stock para un material en una ubicación */
    @Query("SELECT sm FROM StockMaterial sm " +
           "WHERE sm.material.id = :materialId " +
           "AND sm.empresa.id = :empresaId " +
           "AND LOWER(sm.ubicacion) = LOWER(:ubicacion) " +
           "AND sm.estado = 'ACTIVO'")
    Optional<StockMaterial> findByMaterialIdAndEmpresaIdAndUbicacion(@Param("materialId") Long materialId,
                                                                     @Param("empresaId") Long empresaId,
                                                                     @Param("ubicacion") String ubicacion);
}