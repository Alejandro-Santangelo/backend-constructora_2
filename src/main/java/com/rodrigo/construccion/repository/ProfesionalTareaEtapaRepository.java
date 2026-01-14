package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.ProfesionalTareaEtapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para ProfesionalTareaEtapa
 */
@Repository
public interface ProfesionalTareaEtapaRepository extends JpaRepository<ProfesionalTareaEtapa, Long> {

    /**
     * Buscar profesionales por tarea
     */
    List<ProfesionalTareaEtapa> findByTareaId(Long tareaId);

    /**
     * Eliminar profesionales por tarea
     */
    @Modifying
    void deleteByTareaId(Long tareaId);
}
