package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtraTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de tareas de trabajos extra
 */
@Repository
public interface TrabajoExtraTareaRepository extends JpaRepository<TrabajoExtraTarea, Long> {
    
    /**
     * Obtiene todas las tareas asociadas a un trabajo extra
     */
    List<TrabajoExtraTarea> findByTrabajoExtraId(Long trabajoExtraId);
    
    /**
     * Elimina todas las tareas asociadas a un trabajo extra
     */
    void deleteByTrabajoExtraId(Long trabajoExtraId);
}
