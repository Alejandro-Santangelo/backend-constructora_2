package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Profesional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {

    /* Buscar profesionales por tipo exacto */
    List<Profesional> findByTipoProfesionalAndActivoTrue(String tipoProfesional);

    /* Buscar profesional por tipo y nombre exactos (para validación de asignación) */
    Optional<Profesional> findByTipoProfesionalAndNombreAndActivoTrue(String tipoProfesional, String nombre);

    /* Buscar profesionales por tipo con búsqueda flexible (ignora mayúsculas/minúsculas) */
    List<Profesional> findByTipoProfesionalIgnoreCase(String tipoProfesional);

    /* Buscar profesionales por tipo ignorando mayúsculas y solo activos */
    List<Profesional> findByTipoProfesionalIgnoreCaseAndActivoTrue(String tipoProfesional);

    /* Buscar profesionales por nombre (contiene) */
    @Query("SELECT p FROM Profesional p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Profesional> findByNombreContaining(@Param("nombre") String nombre);

    /* Buscar profesionales por tipo flexible - maneja variaciones de género y  capitalización */
    @Query("SELECT p FROM Profesional p WHERE " +
            "LOWER(p.tipoProfesional) LIKE LOWER(:tipo) OR " +
            "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT(:tipo, 'a')) OR " +
            "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT(SUBSTRING(:tipo, 1, LENGTH(:tipo) - 1), 'a')) OR " +
            "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT(SUBSTRING(:tipo, 1, LENGTH(:tipo) - 1), 'o'))")
    List<Profesional> buscarPorTipoFlexible(@Param("tipo") String tipo);

    @Query(value = "SELECT DISTINCT h.profesional FROM Honorario h " +
            "JOIN h.obra.cliente.empresas e " +
            "WHERE e.id = :empresaId",
            countQuery = "SELECT COUNT(DISTINCT h.profesional) FROM Honorario h " +
                    "JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId")
    Page<Profesional> findProfesionalesWithHonorarios(Pageable pageable, @Param("empresaId") Long empresaId);

    /* Buscar profesionales por categoría */
    List<Profesional> findByCategoria(String categoria);

    /* Buscar profesionales por categoría y activos */
    List<Profesional> findByCategoriaAndActivoTrue(String categoria);

    /* Buscar profesionales por categoría, empresaId y activos */
    @Query("SELECT p FROM Profesional p WHERE " +
           "p.categoria = :categoria AND " +
           "p.empresa.id = :empresaId AND " +
           "p.activo = true")
    List<Profesional> findByCategoriaAndEmpresaIdAndActivoTrue(
        @Param("categoria") String categoria, 
        @Param("empresaId") Long empresaId
    );

    /* Buscar todos los profesionales activos de una empresa */
    @Query("SELECT p FROM Profesional p WHERE p.empresa.id = :empresaId AND p.activo = true ORDER BY p.nombre")
    List<Profesional> findByEmpresaIdAndActivoTrue(@Param("empresaId") Long empresaId);

}