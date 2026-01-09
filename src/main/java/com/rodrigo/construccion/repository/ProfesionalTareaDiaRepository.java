package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.ProfesionalTareaDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para ProfesionalTareaDia
 */
@Repository
public interface ProfesionalTareaDiaRepository extends JpaRepository<ProfesionalTareaDia, Long> {

    /**
     * Buscar todos los profesionales asignados a una tarea
     */
    @Query("SELECT ptd FROM ProfesionalTareaDia ptd WHERE ptd.tarea.id = :tareaId")
    List<ProfesionalTareaDia> findByTareaId(@Param("tareaId") Long tareaId);

    /**
     * Buscar todas las tareas de un profesional en una fecha (a través de asignacion_dia_id)
     */
    @Query("SELECT ptd FROM ProfesionalTareaDia ptd " +
           "JOIN ptd.asignacionDia apd " +
           "WHERE apd.id = :asignacionDiaId")
    List<ProfesionalTareaDia> findByAsignacionDiaId(@Param("asignacionDiaId") Long asignacionDiaId);

    /**
     * Eliminar todas las asignaciones de una tarea
     */
    @Modifying
    @Query("DELETE FROM ProfesionalTareaDia ptd WHERE ptd.tarea.id = :tareaId")
    void deleteByTareaId(@Param("tareaId") Long tareaId);

    /**
     * Verificar si un profesional ya está asignado a una tarea
     */
    boolean existsByTareaIdAndAsignacionDiaId(Long tareaId, Long asignacionDiaId);

    /**
     * Contar profesionales asignados a una tarea
     */
    Long countByTareaId(Long tareaId);

    /**
     * Sumar horas asignadas a un profesional en un día específico
     */
    @Query("SELECT COALESCE(SUM(ptd.horasAsignadas), 0) FROM ProfesionalTareaDia ptd " +
           "WHERE ptd.asignacionDia.id = :asignacionDiaId")
    java.math.BigDecimal sumHorasByAsignacionDiaId(@Param("asignacionDiaId") Long asignacionDiaId);
}
