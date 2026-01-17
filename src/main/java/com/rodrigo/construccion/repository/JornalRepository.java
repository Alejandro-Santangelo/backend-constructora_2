package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Jornal;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JornalRepository extends JpaRepository<Jornal, Long> {

    /**
     * Verifica si existe un jornal para una asignación y fecha específica
     * Útil para evitar duplicados al crear jornales automáticamente desde asistencias
     */
    @Query("SELECT COUNT(j) > 0 FROM Jornal j WHERE j.asignacion.id = :asignacionId AND j.fecha = :fecha")
    boolean existsByAsignacionIdAndFecha(@Param("asignacionId") Long asignacionId, @Param("fecha") LocalDate fecha);

    /**
     * Busca jornales por empresa navegando por la relación asignación
     * Nota: ProfesionalObra tiene empresaId directamente, simplificando la consulta
     */
    @Query("SELECT j FROM Jornal j WHERE j.asignacion.empresaId = :empresaId")
    List<Jornal> findByEmpresaId(@Param("empresaId") Long empresaId, Sort sort);

    /* Busca jornales por empresa y rango de fechas */
    @Query("SELECT j FROM Jornal j WHERE j.asignacion.empresaId = :empresaId AND j.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Jornal> findByEmpresaIdAndFechaBetween(
            @Param("empresaId") Long empresaId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            Sort sort
    );

    /* Busca jornales por empresa con valor por hora mayor o igual al especificado */
    @Query("SELECT j FROM Jornal j WHERE j.asignacion.empresaId = :empresaId AND j.valorHora >= :valorMinimo")
    List<Jornal> findByEmpresaIdAndValorHoraGreaterThanEqual(
            @Param("empresaId") Long empresaId,
            @Param("valorMinimo") java.math.BigDecimal valorMinimo,
            Sort sort
    );

    /* Cuenta el total de jornales por empresa */
    @Query("SELECT COUNT(j) FROM Jornal j WHERE j.asignacion.empresaId = :empresaId")
    long countByEmpresaId(@Param("empresaId") Long empresaId);

    /* Cuenta jornales por empresa en un rango de fechas */
    @Query("SELECT COUNT(j) FROM Jornal j WHERE j.asignacion.empresaId = :empresaId AND j.fecha BETWEEN :fechaInicio AND :fechaFin")
    long countByFechaBetweenAndEmpresaId(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("empresaId") Long empresaId
    );

    /* Suma el monto total (horas * valorHora) por empresa en un rango de fechas */
    @Query("SELECT COALESCE(SUM(j.horasTrabajadas * j.valorHora), 0.0) FROM Jornal j WHERE j.asignacion.empresaId = :empresaId AND j.fecha BETWEEN :fechaInicio AND :fechaFin")
    java.math.BigDecimal sumMontoTotalByFechaBetweenAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /* Suma las horas trabajadas por empresa en un rango de fechas */
    @Query("SELECT COALESCE(SUM(j.horasTrabajadas), 0.0) FROM Jornal j WHERE j.asignacion.empresaId = :empresaId AND j.fecha BETWEEN :fechaInicio AND :fechaFin")
    java.math.BigDecimal sumHorasTrabajadasByFechaBetweenAndEmpresaId(
            @Param("empresaId") Long empresaId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

}
