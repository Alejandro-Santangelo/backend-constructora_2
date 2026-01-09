package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Profesional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {

       /**
        * Obtener todos los profesionales.
        * Las relaciones se cargan de forma eficiente gracias a @BatchSize en la entidad,
        * evitando N+1 y MultipleBagFetchException.
        */
       @Query("SELECT DISTINCT p FROM Profesional p")
       List<Profesional> findAllWithRelations();

       /* Buscar profesionales activos */
       List<Profesional> findByActivoTrue();

       /* Buscar profesionales activos con paginación */
       Page<Profesional> findByActivoTrue(Pageable pageable);

       /* Buscar profesionales por tipo exacto */
       List<Profesional> findByTipoProfesionalAndActivoTrue(String tipoProfesional);

       /*
        * Busca profesionales activos por tipo de forma flexible, incluyendo
        * variaciones de género (ej: 'arquitecto'/'arquitecta').
        */
       @Query("SELECT DISTINCT p FROM Profesional p LEFT JOIN FETCH p.obrasAsignadas po " +
                     "WHERE p.activo = true AND (" +
                     "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :terminoBase, '%')) OR " +
                     "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :terminoAlterno, '%')))")
       List<Profesional> buscarActivosPorTipoFlexible(@Param("terminoBase") String terminoBase,
                     @Param("terminoAlterno") String terminoAlterno);

       /*
        * Buscar profesional por tipo y nombre exactos (para validación de asignación)
        */
       Optional<Profesional> findByTipoProfesionalAndNombreAndActivoTrue(String tipoProfesional, String nombre);

       /* Buscar profesionales por tipo (sin filtro de activo) */
       List<Profesional> findByTipoProfesional(String tipoProfesional);

       /**
        * Buscar profesionales por tipo con búsqueda flexible (ignora
        * mayúsculas/minúsculas)
        */
       List<Profesional> findByTipoProfesionalIgnoreCase(String tipoProfesional);

       /* MÉTODOS PARA BÚSQUEDA FLEXIBLE - NUEVOS */

       /* Buscar profesionales por tipo ignorando mayúsculas y solo activos */
       List<Profesional> findByTipoProfesionalIgnoreCaseAndActivoTrue(String tipoProfesional);

       /**
        * Buscar profesionales por tipo que contenga el texto (ignora mayúsculas) y
        * solo activos
        */
       List<Profesional> findByTipoProfesionalContainingIgnoreCaseAndActivoTrue(String tipoProfesional);

       /* Buscar profesionales por especialidad */
       List<Profesional> findByEspecialidad(String especialidad);

       /* Buscar profesionales por nombre (contiene) */
       @Query("SELECT p FROM Profesional p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
       List<Profesional> findByNombreContaining(@Param("nombre") String nombre);

       /* Buscar profesionales por rango de valor hora */
       List<Profesional> findByValorHoraDefaultBetween(java.math.BigDecimal valorMinimo,
                     java.math.BigDecimal valorMaximo);

       /* Contar profesionales activos */
       long countByActivoTrue();

       /* Contar profesionales por tipo */
       long countByTipoProfesional(String tipoProfesional);

       /* Obtener valor hora promedio */
       @Query("SELECT AVG(p.valorHoraDefault) FROM Profesional p WHERE p.activo = true")
       java.math.BigDecimal getValorHoraPromedio();

       /* Buscar profesionales por nombre */
       @Query("SELECT p FROM Profesional p WHERE p.activo = true AND LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
       Page<Profesional> findByNombreContainingIgnoreCase(@Param("nombre") String nombre, Pageable pageable);

       /**
        * CONSULTAS DE DISPONIBILIDAD
        */

       /* Profesionales disponibles (no asignados actualmente a ninguna obra) */
       @Query("SELECT p FROM Profesional p WHERE p.activo = true AND p.id NOT IN " +
                     "(SELECT po.profesional.id FROM ProfesionalObra po WHERE LOWER(po.estado) = 'activo')")
       List<Profesional> findProfesionalesDisponibles();

       /* Profesionales ocupados (asignados a obras activas) */
       @Query("SELECT DISTINCT p FROM Profesional p " +
                     "JOIN ProfesionalObra po ON p.id = po.profesional.id " +
                     "WHERE p.activo = true AND LOWER(po.estado) = 'activo'")
       List<Profesional> findProfesionalesOcupados();

       /* Alias para findProfesionalesOcupados (mismo método con nombre diferente) */
       @Query("SELECT DISTINCT p FROM Profesional p " +
                     "JOIN ProfesionalObra po ON p.id = po.profesional.id " +
                     "WHERE p.activo = true AND LOWER(po.estado) = 'activo'")
       List<Profesional> findProfesionalesEnObrasActivas();


       /* ESTADÍSTICAS */

       /* Contar profesionales por tipo */
       @Query("SELECT p.tipoProfesional, COUNT(p) FROM Profesional p WHERE p.activo = true GROUP BY p.tipoProfesional")
       List<Object[]> countProfesionalesPorTipo();


       /* Buscar profesionales por tipo con texto (like) */
       @Query("SELECT p FROM Profesional p WHERE p.activo = true AND LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :tipo, '%'))")
       Page<Profesional> findByTipoProfesionalContainingIgnoreCase(@Param("tipo") String tipo, Pageable pageable);

       /**
        * Buscar profesionales por tipo flexible - maneja variaciones de género y
        * capitalización
        * Busca profesionales donde el tipo coincida de manera flexible:
        * - Ignora mayúsculas/minúsculas
        * - Maneja variaciones de género (Arquitecto/Arquitecta, Ingeniero/Ingeniera,
        * etc.)
        */
       @Query("SELECT p FROM Profesional p WHERE " +
                     "LOWER(p.tipoProfesional) LIKE LOWER(:tipo) OR " +
                     "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT(:tipo, 'a')) OR " +
                     "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT(SUBSTRING(:tipo, 1, LENGTH(:tipo) - 1), 'a')) OR " +
                     "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT(SUBSTRING(:tipo, 1, LENGTH(:tipo) - 1), 'o'))")
       List<Profesional> buscarPorTipoFlexible(@Param("tipo") String tipo);

       /* Obtener tipos de profesionales únicos que existen en la base de datos */
       @Query("SELECT DISTINCT p.tipoProfesional FROM Profesional p WHERE p.tipoProfesional IS NOT NULL ORDER BY p.tipoProfesional")
       List<String> findDistinctTipoProfesional();

       @Query(value = "SELECT DISTINCT h.profesional FROM Honorario h " +
                      "JOIN h.obra.cliente.empresas e " +
                      "WHERE e.id = :empresaId",
              countQuery = "SELECT COUNT(DISTINCT h.profesional) FROM Honorario h " +
                           "JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId")
       Page<Profesional> findProfesionalesWithHonorarios(Pageable pageable, @Param("empresaId") Long empresaId);
}