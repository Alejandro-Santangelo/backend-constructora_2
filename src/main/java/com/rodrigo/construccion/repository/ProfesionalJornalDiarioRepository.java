package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.ProfesionalJornalDiario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesionalJornalDiarioRepository extends JpaRepository<ProfesionalJornalDiario, Long> {

    /**
     * Buscar todos los jornales de un profesional en una obra específica
     */
    List<ProfesionalJornalDiario> findByProfesionalIdAndObraIdOrderByFechaDesc(Long profesionalId, Long obraId);

    /**
     * Buscar todos los jornales de un profesional ordenados por fecha
     */
    List<ProfesionalJornalDiario> findByProfesionalIdOrderByFechaDesc(Long profesionalId);

    /**
     * Buscar todos los jornales de una obra ordenados por fecha
     */
    List<ProfesionalJornalDiario> findByObraIdOrderByFechaDesc(Long obraId);

    /**
     * Buscar jornales de un profesional en una fecha específica
     */
    List<ProfesionalJornalDiario> findByProfesionalIdAndFecha(Long profesionalId, LocalDate fecha);

    /**
     * Buscar jornales de una obra en una fecha específica
     */
    List<ProfesionalJornalDiario> findByObraIdAndFecha(Long obraId, LocalDate fecha);

    /**
     * Buscar jornal específico (profesional + obra + fecha)
     */
    Optional<ProfesionalJornalDiario> findByProfesionalIdAndObraIdAndFecha(Long profesionalId, Long obraId, LocalDate fecha);

    /**
     * Buscar jornal específico (profesional + obra + rubro + fecha)
     */
    Optional<ProfesionalJornalDiario> findByProfesionalIdAndObraIdAndRubroIdAndFecha(
            Long profesionalId, Long obraId, Long rubroId, LocalDate fecha);

    /**
     * Verificar si existe un jornal (profesional + obra + rubro + fecha) - incluye eliminación lógica
     */
    boolean existsByProfesionalIdAndObraIdAndRubroIdAndFecha(
            Long profesionalId, Long obraId, Long rubroId, LocalDate fecha);

    /**
     * Buscar jornales por rango de fechas para un profesional
     */
    List<ProfesionalJornalDiario> findByProfesionalIdAndFechaBetweenOrderByFechaDesc(
            Long profesionalId, 
            LocalDate fechaDesde, 
            LocalDate fechaHasta
    );

    /**
     * Buscar jornales por rango de fechas para una obra
     */
    List<ProfesionalJornalDiario> findByObraIdAndFechaBetweenOrderByFechaDesc(
            Long obraId, 
            LocalDate fechaDesde, 
            LocalDate fechaHasta
    );

    /**
     * Buscar jornales por rango de fechas y empresa (sin filtro de profesional u obra)
     * Con JOIN FETCH para cargar Profesional y Obra
     */
    @Query("SELECT j FROM ProfesionalJornalDiario j " +
           "JOIN FETCH j.profesional " +
           "JOIN FETCH j.obra " +
           "WHERE j.fecha BETWEEN :fechaDesde AND :fechaHasta " +
           "AND j.empresaId = :empresaId " +
           "ORDER BY j.fecha DESC")
    List<ProfesionalJornalDiario> findByFechaBetweenAndEmpresaIdOrderByFechaDesc(
            @Param("fechaDesde") LocalDate fechaDesde, 
            @Param("fechaHasta") LocalDate fechaHasta,
            @Param("empresaId") Long empresaId
    );

    /**
     * Buscar jornales por rango de fechas para profesional y obra
     */
    List<ProfesionalJornalDiario> findByProfesionalIdAndObraIdAndFechaBetweenOrderByFechaDesc(
            Long profesionalId,
            Long obraId,
            LocalDate fechaDesde, 
            LocalDate fechaHasta
    );

    /**
     * Calcular total de horas trabajadas por profesional en una obra
     */
    @Query("SELECT COALESCE(SUM(j.horasTrabajadasDecimal), 0) FROM ProfesionalJornalDiario j " +
           "WHERE j.profesional.id = :profesionalId AND j.obra.id = :obraId")
    BigDecimal calcularTotalHorasPorProfesionalEnObra(
            @Param("profesionalId") Long profesionalId,
            @Param("obraId") Long obraId
    );

    /**
     * Calcular total cobrado por profesional en una obra
     */
    @Query("SELECT COALESCE(SUM(j.montoCobrado), 0) FROM ProfesionalJornalDiario j " +
           "WHERE j.profesional.id = :profesionalId AND j.obra.id = :obraId")
    BigDecimal calcularTotalCobradoPorProfesionalEnObra(
            @Param("profesionalId") Long profesionalId,
            @Param("obraId") Long obraId
    );

    /**
     * Calcular total cobrado en una obra (todos los profesionales)
     */
    @Query("SELECT COALESCE(SUM(j.montoCobrado), 0) FROM ProfesionalJornalDiario j " +
           "WHERE j.obra.id = :obraId")
    BigDecimal calcularTotalCobradoEnObra(@Param("obraId") Long obraId);

    /**
     * Calcular total cobrado por un profesional (todas las obras)
     */
    @Query("SELECT COALESCE(SUM(j.montoCobrado), 0) FROM ProfesionalJornalDiario j " +
           "WHERE j.profesional.id = :profesionalId")
    BigDecimal calcularTotalCobradoPorProfesional(@Param("profesionalId") Long profesionalId);

    /**
     * Calcular total de horas trabajadas por un profesional (todas las obras)
     */
    @Query("SELECT COALESCE(SUM(j.horasTrabajadasDecimal), 0) FROM ProfesionalJornalDiario j " +
           "WHERE j.profesional.id = :profesionalId")
    BigDecimal calcularTotalHorasPorProfesional(@Param("profesionalId") Long profesionalId);

    /**
     * Obtener resumen de jornales por profesional en una obra (agrupado)
     */
    @Query("SELECT j.profesional.id, j.profesional.nombre, " +
           "COUNT(j), COALESCE(SUM(j.horasTrabajadasDecimal), 0), COALESCE(SUM(j.montoCobrado), 0) " +
           "FROM ProfesionalJornalDiario j " +
           "WHERE j.obra.id = :obraId " +
           "GROUP BY j.profesional.id, j.profesional.nombre " +
           "ORDER BY j.profesional.nombre")
    List<Object[]> obtenerResumenProfesionalesPorObra(@Param("obraId") Long obraId);

    /**
     * Obtener resumen de jornales por obra para un profesional (agrupado)
     */
    @Query("SELECT j.obra.id, j.obra.nombre, " +
           "COUNT(j), COALESCE(SUM(j.horasTrabajadasDecimal), 0), COALESCE(SUM(j.montoCobrado), 0) " +
           "FROM ProfesionalJornalDiario j " +
           "WHERE j.profesional.id = :profesionalId " +
           "GROUP BY j.obra.id, j.obra.nombre " +
           "ORDER BY j.obra.nombre")
    List<Object[]> obtenerResumenObrasPorProfesional(@Param("profesionalId") Long profesionalId);

    /**
     * Buscar jornales por empresa (multi-tenancy)
     */
    List<ProfesionalJornalDiario> findByEmpresaIdOrderByFechaDesc(Long empresaId);

    /**
     * Buscar jornales por empresa paginado
     */
    Page<ProfesionalJornalDiario> findByEmpresaIdOrderByFechaDesc(Long empresaId, Pageable pageable);

    /**
     * Buscar jornales de un profesional en un rango de fechas con paginación
     */
    Page<ProfesionalJornalDiario> findByProfesionalIdAndFechaBetweenOrderByFechaDesc(
            Long profesionalId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Pageable pageable
    );

    /**
     * Buscar jornales de una obra en un rango de fechas con paginación
     */
    Page<ProfesionalJornalDiario> findByObraIdAndFechaBetweenOrderByFechaDesc(
            Long obraId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Pageable pageable
    );

    /**
     * Verificar si existe un jornal para profesional + obra + fecha 
     * (para evitar duplicados antes de crear)
     */
    boolean existsByProfesionalIdAndObraIdAndFecha(Long profesionalId, Long obraId, LocalDate fecha);

    /**
     * Contar cantidad de jornales de un profesional en una obra
     */
    long countByProfesionalIdAndObraId(Long profesionalId, Long obraId);

    /**
     * Obtener último jornal registrado de un profesional
     */
    Optional<ProfesionalJornalDiario> findFirstByProfesionalIdOrderByFechaDescFechaCreacionDesc(Long profesionalId);

    /**
     * Obtener último jornal registrado en una obra
     */
    Optional<ProfesionalJornalDiario> findFirstByObraIdOrderByFechaDescFechaCreacionDesc(Long obraId);
}
