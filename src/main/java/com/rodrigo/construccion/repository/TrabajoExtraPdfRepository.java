package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtraPdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrabajoExtraPdfRepository extends JpaRepository<TrabajoExtraPdf, Long> {
    
    /**
     * Buscar PDFs por trabajo extra ID ordenados por fecha de generación descendente
     */
    List<TrabajoExtraPdf> findByTrabajoExtraIdOrderByFechaGeneracionDesc(Long trabajoExtraId);
    
    /**
     * Buscar PDFs por trabajo extra ID y empresa ID
     */
    List<TrabajoExtraPdf> findByTrabajoExtraIdAndEmpresaIdOrderByFechaGeneracionDesc(Long trabajoExtraId, Long empresaId);
    
    /**
     * Buscar PDF por ID y empresa ID
     */
    Optional<TrabajoExtraPdf> findByIdAndEmpresaId(Long id, Long empresaId);
    
    /**
     * Verificar existencia por trabajo extra ID y empresa ID
     */
    boolean existsByTrabajoExtraIdAndEmpresaId(Long trabajoExtraId, Long empresaId);
    
    /**
     * Eliminar por trabajo extra ID
     */
    void deleteByTrabajoExtraId(Long trabajoExtraId);
}
