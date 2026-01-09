package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.AsignacionProfesionalDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository para AsignacionProfesionalDia
 */
@Repository
public interface AsignacionProfesionalDiaRepository extends JpaRepository<AsignacionProfesionalDia, Long> {

    /**
     * Buscar todos los días de una asignación específica
     */
    @Query("SELECT apd FROM AsignacionProfesionalDia apd WHERE apd.asignacion.id = :asignacionId ORDER BY apd.fecha")
    List<AsignacionProfesionalDia> findByAsignacionIdOrderByFecha(@Param("asignacionId") Long asignacionId);

    /**
     * Buscar días por rango de fechas
     */
    @Query("SELECT apd FROM AsignacionProfesionalDia apd " +
           "WHERE apd.asignacion.id = :asignacionId " +
           "AND apd.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY apd.fecha")
    List<AsignacionProfesionalDia> findByAsignacionIdAndFechaBetweenOrderByFecha(
            @Param("asignacionId") Long asignacionId, 
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Buscar días por semana ISO
     */
    @Query("SELECT apd FROM AsignacionProfesionalDia apd " +
           "WHERE apd.asignacion.id = :asignacionId " +
           "AND apd.semanaIso = :semanaIso " +
           "ORDER BY apd.fecha")
    List<AsignacionProfesionalDia> findByAsignacionIdAndSemanaIsoOrderByFecha(
            @Param("asignacionId") Long asignacionId, 
            @Param("semanaIso") String semanaIso);

    /**
     * Buscar todos los días de asignaciones de una obra
     */
    @Query("SELECT apd FROM AsignacionProfesionalDia apd " +
           "JOIN apd.asignacion apo " +
           "WHERE apo.obra.id = :obraId " +
           "ORDER BY apd.fecha")
    List<AsignacionProfesionalDia> findByObraId(@Param("obraId") Long obraId);

    /**
     * Eliminar todos los días de una asignación
     */
    @Modifying
    @Query("DELETE FROM AsignacionProfesionalDia apd WHERE apd.asignacion.id = :asignacionId")
    void deleteByAsignacionId(@Param("asignacionId") Long asignacionId);

    /**
     * Sumar total de jornales de una asignación
     */
    @Query("SELECT COALESCE(SUM(apd.cantidad), 0) FROM AsignacionProfesionalDia apd " +
           "WHERE apd.asignacion.id = :asignacionId")
    Integer sumCantidadByAsignacionId(@Param("asignacionId") Long asignacionId);

    /**
     * Obtener profesionales disponibles en una obra y fecha específica
     */
    @Query(value = "SELECT " +
           "apd.id as asignacion_dia_id, " +
           "apo.profesional_id, " +
           "p.nombre as profesional_nombre, " +
           "tp.tipo as tipo_profesional, " +
           "apd.cantidad, " +
           "apd.semana_iso " +
           "FROM asignacion_profesional_dia apd " +
           "JOIN asignaciones_profesional_obra apo ON apo.id = apd.asignacion_id " +
           "LEFT JOIN profesionales p ON p.id_profesional = apo.profesional_id " +
           "LEFT JOIN tipos_profesionales tp ON tp.id = p.tipo_profesional_id " +
           "WHERE apo.obra_id = :obraId " +
           "AND apd.fecha = :fecha " +
           "AND apo.empresa_id = :empresaId " +
           "AND apo.estado = 'ACTIVO' " +
           "ORDER BY p.nombre", 
           nativeQuery = true)
    List<Object[]> findProfesionalesDisponiblesByObraAndFecha(
            @Param("obraId") Long obraId,
            @Param("fecha") LocalDate fecha,
            @Param("empresaId") Long empresaId);

    /**
     * Verifica si existe una asignación para una fecha específica
     * Usado en asignación semanal para evitar duplicados
     */
    boolean existsByAsignacion_IdAndFecha(Long asignacionId, LocalDate fecha);
}
