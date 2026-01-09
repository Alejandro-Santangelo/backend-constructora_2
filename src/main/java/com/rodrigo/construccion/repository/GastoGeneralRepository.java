package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.GastoGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GastoGeneralRepository extends JpaRepository<GastoGeneral, Long> {
    
    List<GastoGeneral> findByEmpresaIdOrderByNombre(Long empresaId);
    
    Optional<GastoGeneral> findByIdAndEmpresaId(Long id, Long empresaId);
    
    @Query("SELECT gg FROM GastoGeneral gg WHERE gg.empresaId = :empresaId AND gg.categoria = :categoria ORDER BY gg.nombre")
    List<GastoGeneral> findByEmpresaIdAndCategoriaOrderByNombre(@Param("empresaId") Long empresaId, @Param("categoria") String categoria);
}