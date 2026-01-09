package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.EtapaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad EtapaDiaria
 * Todas las consultas incluyen filtrado por empresa (Multi-Tenant)
 */
@Repository
public interface EtapaDiariaRepository extends JpaRepository<EtapaDiaria, Long> {

    /**
     * Buscar etapas diarias por obra y empresa
     */
    @Query("SELECT ed FROM EtapaDiaria ed WHERE ed.obraId = :obraId AND ed.empresaId = :empresaId ORDER BY ed.fecha DESC")
    List<EtapaDiaria> findByObraIdAndEmpresaId(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);

    /**
     * Buscar etapa diaria por ID y empresa (validación multi-tenant)
     */
    @Query("SELECT ed FROM EtapaDiaria ed WHERE ed.id = :id AND ed.empresaId = :empresaId")
    Optional<EtapaDiaria> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Buscar etapa diaria por obra, fecha y empresa
     */
    @Query("SELECT ed FROM EtapaDiaria ed WHERE ed.obraId = :obraId AND ed.fecha = :fecha AND ed.empresaId = :empresaId")
    Optional<EtapaDiaria> findByObraIdAndFechaAndEmpresaId(
        @Param("obraId") Long obraId, 
        @Param("fecha") LocalDate fecha, 
        @Param("empresaId") Long empresaId
    );

    /**
     * Verificar si existe una etapa para la obra y fecha
     */
    @Query("SELECT COUNT(ed) > 0 FROM EtapaDiaria ed WHERE ed.obraId = :obraId AND ed.fecha = :fecha AND ed.empresaId = :empresaId")
    boolean existsByObraIdAndFechaAndEmpresaId(
        @Param("obraId") Long obraId, 
        @Param("fecha") LocalDate fecha, 
        @Param("empresaId") Long empresaId
    );

    /**
     * Verificar si existe una etapa por ID y empresa
     */
    @Query("SELECT COUNT(ed) > 0 FROM EtapaDiaria ed WHERE ed.id = :id AND ed.empresaId = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Buscar etapas diarias por empresa
     */
    @Query("SELECT ed FROM EtapaDiaria ed WHERE ed.empresaId = :empresaId ORDER BY ed.fecha DESC")
    List<EtapaDiaria> findByEmpresaId(@Param("empresaId") Long empresaId);
}
