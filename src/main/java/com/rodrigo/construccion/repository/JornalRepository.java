package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Jornal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/* Repositorio para la entidad Jornal */
@Repository
public interface JornalRepository extends JpaRepository<Jornal, Long> {

    /* TODO: ADAPTAR - Todos estos métodos usan j.asignacion.obra que ya no existe
     * Necesitan adaptarse para usar los 4 campos de dirección en ProfesionalObra
     
    @Query("SELECT j FROM Jornal j JOIN j.asignacion.obra.cliente.empresas e WHERE e.id = :empresaId")
    List<Jornal> findByEmpresaId(@Param("empresaId") Long empresaId, Sort sort);

    @Query("SELECT j FROM Jornal j JOIN j.asignacion.obra.cliente.empresas e WHERE e.id = :empresaId AND j.fecha BETWEEN :fechaDesde AND :fechaHasta")
    List<Jornal> findByEmpresaIdAndFechaBetween(@Param("empresaId") Long empresaId,
            @Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta, Sort sort);

    @Query("SELECT j FROM Jornal j JOIN j.asignacion.obra.cliente.empresas e WHERE e.id = :empresaId AND j.valorHora >= :valorMinimo")
    List<Jornal> findByEmpresaIdAndValorHoraGreaterThanEqual(@Param("empresaId") Long empresaId,
            @Param("valorMinimo") BigDecimal valorMinimo, Sort sort);

    @Query("SELECT j FROM Jornal j WHERE j.fecha = :fecha AND :empresaId MEMBER OF j.asignacion.obra.cliente.empresas")
    List<Jornal> findByFechaAndEmpresaId(@Param("fecha") LocalDate fecha, @Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(j) FROM Jornal j JOIN j.asignacion.obra.cliente.empresas e WHERE e.id = :empresaId")
    long countByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(j) FROM Jornal j JOIN j.asignacion.obra.cliente.empresas e WHERE e.id = :empresaId AND j.fecha BETWEEN :fechaDesde AND :fechaHasta")
    long countByFechaBetweenAndEmpresaId(@Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(j.horasTrabajadas * j.valorHora), 0.0) FROM Jornal j JOIN j.asignacion.obra.cliente.empresas e WHERE e.id = :empresaId AND j.fecha BETWEEN :fechaDesde AND :fechaHasta")
    BigDecimal sumMontoTotalByFechaBetweenAndEmpresaId(@Param("empresaId") Long empresaId,
            @Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta);

    @Query("SELECT COALESCE(SUM(j.horasTrabajadas), 0.0) FROM Jornal j JOIN j.asignacion.obra.cliente.empresas e WHERE e.id = :empresaId AND j.fecha BETWEEN :fechaDesde AND :fechaHasta")
    BigDecimal sumHorasTrabajadasByFechaBetweenAndEmpresaId(@Param("empresaId") Long empresaId,
            @Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta);
    */
}
