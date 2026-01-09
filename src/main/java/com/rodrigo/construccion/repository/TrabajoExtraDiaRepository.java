package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtraDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de días de trabajos extra
 */
@Repository
public interface TrabajoExtraDiaRepository extends JpaRepository<TrabajoExtraDia, Long> {
    
    /**
     * Obtiene todos los días asociados a un trabajo extra
     */
    List<TrabajoExtraDia> findByTrabajoExtraId(Long trabajoExtraId);
    
    /**
     * Elimina todos los días asociados a un trabajo extra
     */
    void deleteByTrabajoExtraId(Long trabajoExtraId);
}
