package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TareaEtapaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para TareaEtapaDiaria
 */
@Repository
public interface TareaEtapaDiariaRepository extends JpaRepository<TareaEtapaDiaria, Long> {

    /**
     * Buscar tareas por etapa diaria
     */
    List<TareaEtapaDiaria> findByEtapaDiariaId(Long etapaDiariaId);

    /**
     * Eliminar tareas por etapa diaria
     */
    void deleteByEtapaDiariaId(Long etapaDiariaId);
}
