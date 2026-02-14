package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoAdicionalProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para profesionales de trabajos adicionales
 */
@Repository
public interface TrabajoAdicionalProfesionalRepository extends JpaRepository<TrabajoAdicionalProfesional, Long> {

    /**
     * Buscar todos los profesionales de un trabajo adicional
     */
    @Query("SELECT tap FROM TrabajoAdicionalProfesional tap WHERE tap.trabajoAdicional.id = :trabajoAdicionalId")
    List<TrabajoAdicionalProfesional> findByTrabajoAdicionalId(@Param("trabajoAdicionalId") Long trabajoAdicionalId);

    /**
     * Eliminar todos los profesionales de un trabajo adicional
     */
    @Modifying
    @Query("DELETE FROM TrabajoAdicionalProfesional tap WHERE tap.trabajoAdicional.id = :trabajoAdicionalId")
    void deleteByTrabajoAdicionalId(@Param("trabajoAdicionalId") Long trabajoAdicionalId);

    /**
     * Buscar profesionales registrados de un trabajo adicional
     */
    @Query("SELECT tap FROM TrabajoAdicionalProfesional tap " +
           "WHERE tap.trabajoAdicional.id = :trabajoAdicionalId AND tap.esRegistrado = true")
    List<TrabajoAdicionalProfesional> findProfesionalesRegistradosByTrabajoAdicionalId(
            @Param("trabajoAdicionalId") Long trabajoAdicionalId);

    /**
     * Buscar profesionales ad-hoc de un trabajo adicional
     */
    @Query("SELECT tap FROM TrabajoAdicionalProfesional tap " +
           "WHERE tap.trabajoAdicional.id = :trabajoAdicionalId AND tap.esRegistrado = false")
    List<TrabajoAdicionalProfesional> findProfesionalesAdHocByTrabajoAdicionalId(
            @Param("trabajoAdicionalId") Long trabajoAdicionalId);
}
