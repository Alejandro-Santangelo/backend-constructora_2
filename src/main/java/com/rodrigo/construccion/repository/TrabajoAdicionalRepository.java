package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoAdicional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para trabajos adicionales
 */
@Repository
public interface TrabajoAdicionalRepository extends JpaRepository<TrabajoAdicional, Long> {

    /**
     * Buscar todos los trabajos adicionales de una empresa
     */
    List<TrabajoAdicional> findByEmpresaId(Long empresaId);

    /**
     * Buscar trabajos adicionales de una empresa filtrados por obra
     */
    List<TrabajoAdicional> findByEmpresaIdAndObraId(Long empresaId, Long obraId);

    /**
     * Buscar trabajos adicionales de una empresa filtrados por trabajo extra
     */
    List<TrabajoAdicional> findByEmpresaIdAndTrabajoExtraId(Long empresaId, Long trabajoExtraId);

    /**
     * Buscar trabajos adicionales por estado
     */
    List<TrabajoAdicional> findByEmpresaIdAndEstado(Long empresaId, String estado);

    /**
     * Buscar un trabajo adicional por ID y empresa (para validación de permisos)
     */
    Optional<TrabajoAdicional> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Verificar si existe un trabajo adicional por ID y empresa
     */
    boolean existsByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Buscar trabajos adicionales con profesionales (eager loading)
     */
    @Query("SELECT DISTINCT ta FROM TrabajoAdicional ta " +
           "LEFT JOIN FETCH ta.profesionales " +
           "WHERE ta.id = :id")
    Optional<TrabajoAdicional> findByIdWithProfesionales(@Param("id") Long id);

    /**
     * Buscar todos los trabajos adicionales con profesionales de una empresa
     */
    @Query("SELECT DISTINCT ta FROM TrabajoAdicional ta " +
           "LEFT JOIN FETCH ta.profesionales " +
           "WHERE ta.empresaId = :empresaId")
    List<TrabajoAdicional> findAllByEmpresaIdWithProfesionales(@Param("empresaId") Long empresaId);

    /**
     * Buscar trabajos adicionales de una obra con profesionales
     */
    @Query("SELECT DISTINCT ta FROM TrabajoAdicional ta " +
           "LEFT JOIN FETCH ta.profesionales " +
           "WHERE ta.empresaId = :empresaId AND ta.obraId = :obraId")
    List<TrabajoAdicional> findByEmpresaIdAndObraIdWithProfesionales(
            @Param("empresaId") Long empresaId, 
            @Param("obraId") Long obraId);

    /**
     * Buscar trabajos adicionales de un trabajo extra con profesionales
     */
    @Query("SELECT DISTINCT ta FROM TrabajoAdicional ta " +
           "LEFT JOIN FETCH ta.profesionales " +
           "WHERE ta.empresaId = :empresaId AND ta.trabajoExtraId = :trabajoExtraId")
    List<TrabajoAdicional> findByEmpresaIdAndTrabajoExtraIdWithProfesionales(
            @Param("empresaId") Long empresaId, 
            @Param("trabajoExtraId") Long trabajoExtraId);
}
