package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtroProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de profesionales de trabajos extra
 */
@Repository
public interface TrabajoExtroProfesionalRepository extends JpaRepository<TrabajoExtroProfesional, Long> {
    
    /**
     * Obtiene todos los profesionales asociados a un trabajo extra
     */
    List<TrabajoExtroProfesional> findByTrabajoExtraId(Long trabajoExtraId);
    
    /**
     * Elimina todos los profesionales asociados a un trabajo extra
     */
    void deleteByTrabajoExtraId(Long trabajoExtraId);
}
