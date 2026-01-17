package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Jornal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface JornalRepository extends JpaRepository<Jornal, Long> {

    /**
     * Verifica si existe un jornal para una asignación y fecha específica
     * Útil para evitar duplicados al crear jornales automáticamente desde asistencias
     */
    @Query("SELECT COUNT(j) > 0 FROM Jornal j WHERE j.asignacion.id = :asignacionId AND j.fecha = :fecha")
    boolean existsByAsignacionIdAndFecha(@Param("asignacionId") Long asignacionId, @Param("fecha") LocalDate fecha);

}
