package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.StockGastoGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockGastoGeneralRepository extends JpaRepository<StockGastoGeneral, Long> {
    
    Optional<StockGastoGeneral> findByGastoGeneralIdAndEmpresaId(Long gastoGeneralId, Long empresaId);
    
    List<StockGastoGeneral> findByEmpresaIdOrderByGastoGeneralId(Long empresaId);
    
    @Query("SELECT sgg FROM StockGastoGeneral sgg " +
           "JOIN sgg.gastoGeneral gg " +
           "WHERE sgg.empresaId = :empresaId " +
           "ORDER BY gg.nombre")
    List<StockGastoGeneral> findByEmpresaIdWithGastoGeneralOrderByNombre(@Param("empresaId") Long empresaId);
    
    @Query("SELECT sgg FROM StockGastoGeneral sgg " +
           "WHERE sgg.empresaId = :empresaId AND sgg.cantidadDisponible <= sgg.cantidadMinima")
    List<StockGastoGeneral> findStockBajoByEmpresaId(@Param("empresaId") Long empresaId);
}