package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.AsistenciaObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para AsistenciaObra
 */
@Repository
public interface AsistenciaObraRepository extends JpaRepository<AsistenciaObra, Long> {

    /**
     * Buscar asistencia por profesional y fecha
     * Solo puede haber una por día (constraint unique)
     */
    @Query("SELECT a FROM AsistenciaObra a " +
           "WHERE a.profesionalObra.id = :profesionalObraId " +
           "AND a.fecha = :fecha " +
           "AND a.empresaId = :empresaId")
    Optional<AsistenciaObra> findByProfesionalObraIdAndFecha(
        @Param("profesionalObraId") Long profesionalObraId,
        @Param("fecha") LocalDate fecha,
        @Param("empresaId") Long empresaId
    );

    /**
     * Buscar historial de asistencias de un profesional
     */
    @Query("SELECT a FROM AsistenciaObra a " +
           "WHERE a.profesionalObra.id = :profesionalObraId " +
           "AND a.empresaId = :empresaId " +
           "ORDER BY a.fecha DESC")
    List<AsistenciaObra> findByProfesionalObraId(
        @Param("profesionalObraId") Long profesionalObraId,
        @Param("empresaId") Long empresaId
    );

    /**
     * Buscar asistencias de una obra (por los 4 campos de dirección)
     */
    @Query("SELECT a FROM AsistenciaObra a " +
           "WHERE a.profesionalObra.obra.direccionObraCalle = :calle " +
           "AND a.profesionalObra.obra.direccionObraAltura = :altura " +
           "AND (COALESCE(a.profesionalObra.obra.direccionObraPiso, '') = COALESCE(:piso, '')) " +
           "AND (COALESCE(a.profesionalObra.obra.direccionObraDepartamento, '') = COALESCE(:depto, '')) " +
           "AND a.empresaId = :empresaId " +
           "ORDER BY a.fecha DESC, a.profesionalObra.id")
    List<AsistenciaObra> findByDireccionObra(
        @Param("calle") String direccionObraCalle,
        @Param("altura") String direccionObraAltura,
        @Param("piso") String direccionObraPiso,
        @Param("depto") String direccionObraDepartamento,
        @Param("empresaId") Long empresaId
    );

    /**
     * Buscar asistencia por ID con validación de empresa
     */
    @Query("SELECT a FROM AsistenciaObra a " +
           "WHERE a.id = :id AND a.empresaId = :empresaId")
    Optional<AsistenciaObra> findByIdAndEmpresaId(
        @Param("id") Long id,
        @Param("empresaId") Long empresaId
    );

    /**
     * Buscar asistencia de hoy para un profesional
     */
    @Query("SELECT a FROM AsistenciaObra a " +
           "WHERE a.profesionalObra.id = :profesionalObraId " +
           "AND a.fecha = CURRENT_DATE " +
           "AND a.empresaId = :empresaId")
    Optional<AsistenciaObra> findHoyByProfesionalObraId(
        @Param("profesionalObraId") Long profesionalObraId,
        @Param("empresaId") Long empresaId
    );

    /**
     * Verificar si existe asistencia para profesional en una fecha
     */
    @Query("SELECT COUNT(a) > 0 FROM AsistenciaObra a " +
           "WHERE a.profesionalObra.id = :profesionalObraId " +
           "AND a.fecha = :fecha " +
           "AND a.empresaId = :empresaId")
    boolean existsByProfesionalObraIdAndFecha(
        @Param("profesionalObraId") Long profesionalObraId,
        @Param("fecha") LocalDate fecha,
        @Param("empresaId") Long empresaId
    );
}
