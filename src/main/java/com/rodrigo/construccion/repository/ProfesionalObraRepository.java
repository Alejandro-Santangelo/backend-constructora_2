package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.ProfesionalObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad ProfesionalObra
 * 
 * Maneja las asignaciones de profesionales a obras.
 */
@Repository
public interface ProfesionalObraRepository extends JpaRepository<ProfesionalObra, Long> {

    /* Buscar asignaciones activas de un profesional */
    @Query("SELECT po FROM ProfesionalObra po WHERE po.profesional.id = :profesionalId AND LOWER(po.estado) = 'activo'")
    List<ProfesionalObra> findByProfesional_IdAndActivoTrue(@Param("profesionalId") Long profesionalId);

    /* TODO: ADAPTAR - Ya no existe relación FK con Obra, usar métodos por dirección
    // Buscar asignación activa específica de profesional a obra
    Optional<ProfesionalObra> findByProfesional_IdAndObra_IdAndActivoTrue(Long profesionalId, Long obraId);

    // Buscar asignación por profesional y obra, sin importar el estado 'activo'
    Optional<ProfesionalObra> findByProfesional_IdAndObra_Id(Long profesionalId, Long obraId);

    // Busca si existe otra asignación con la misma combinación de profesional y obra
    @Query("SELECT po FROM ProfesionalObra po WHERE po.profesional.id = :profesionalId AND po.obra.id = :obraId AND po.id <> :excludeAsignacionId")
    Optional<ProfesionalObra> findDuplicate(@Param("profesionalId") Long profesionalId, @Param("obraId") Long obraId, @Param("excludeAsignacionId") Long excludeAsignacionId);

    // Verificar si existe asignación activa
    boolean existsByProfesional_IdAndObra_IdAndActivoTrue(Long profesionalId, Long obraId);
    */

    /* Verificar si un profesional está asignado a alguna obra activa */
    @Query("SELECT CASE WHEN COUNT(po) > 0 THEN true ELSE false END FROM ProfesionalObra po WHERE po.profesional.id = :profesionalId AND LOWER(po.estado) = 'activo'")
    boolean existsByProfesional_IdAndActivoTrue(@Param("profesionalId") Long profesionalId);

    /* TODO: ADAPTAR - Ya no existe relación FK con Obra, usar métodos por dirección
    // Buscar asignaciones activas de una obra
    List<ProfesionalObra> findByObra_IdAndActivoTrue(Long obraId);

    // Buscar todas las asignaciones de un profesional
    List<ProfesionalObra> findByProfesional_Id(Long profesionalId);

    // Buscar todas las asignaciones de una obra
    List<ProfesionalObra> findByObra_Id(Long obraId);
    */

    /* TODO: ADAPTAR - Ya no existe relación FK con Obra, usar métodos por dirección
    // Buscar asignaciones por tipo de profesional
    @Query("SELECT po FROM ProfesionalObra po JOIN po.profesional p WHERE LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :tipoProfesional, '%'))")
    List<ProfesionalObra> findByTipoProfesional(@Param("tipoProfesional") String tipoProfesional);

    // Buscar asignaciones activas por tipo de profesional
    @Query("SELECT po FROM ProfesionalObra po JOIN po.profesional p WHERE LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :tipoProfesional, '%')) AND po.activo = true")
    List<ProfesionalObra> findByTipoProfesionalAndActivoTrue(@Param("tipoProfesional") String tipoProfesional);
    */

    /* TODO: ADAPTAR - Ya no existe relación FK con Obra, usar métodos por dirección
    // Buscar asignaciones por tipo de profesional (case-insensitive, contiene)
    @Query("SELECT po FROM ProfesionalObra po JOIN po.profesional p WHERE LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :tipoProfesional, '%'))")
    List<ProfesionalObra> findByProfesionalTipoProfesionalContainingIgnoreCase(
            @Param("tipoProfesional") String tipoProfesional);

    // Busca asignaciones por tipo de profesional de forma flexible, incluyendo
    // variaciones de género (ej: 'arquitecto'/'arquitecta').
    @Query("SELECT DISTINCT po FROM ProfesionalObra po JOIN po.profesional p WHERE " +
            "(EXISTS (SELECT e FROM po.obra.cliente.empresas e WHERE e.id = :empresaId)) AND " +
            "po.activo = true AND " +
            "(LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :terminoBase, '%')) OR " + 
            "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :terminoAlterno, '%')))")
    List<ProfesionalObra> buscarPorTipoFlexible(@Param("terminoBase") String terminoBase,
            @Param("terminoAlterno") String terminoAlterno, @Param("empresaId") Long empresaId);

    // Buscar asignaciones por obra y empresa
    @Query("SELECT po FROM ProfesionalObra po WHERE po.obra.id = :obraId AND :empresaId IN (SELECT e.id FROM po.obra.cliente.empresas e)")
    List<ProfesionalObra> findByEmpresaIdAndObraId(@Param("empresaId") Long empresaId, @Param("obraId") Long obraId);
    */

    /* Contar obras activas de un profesional */
    @Query("SELECT COUNT(po) FROM ProfesionalObra po WHERE po.profesional.id = :profesionalId AND LOWER(po.estado) = 'activo'")
    Long countByProfesional_IdAndActivoTrue(@Param("profesionalId") Long profesionalId);

    /**
     * ============================================
     * MÉTODOS PARA BÚSQUEDA POR DIRECCIÓN DE OBRA (4 CAMPOS)
     * Reemplazan las búsquedas por obraId
     * ============================================
     */

    /**
     * Buscar asignaciones por dirección de obra (4 campos)
     * Comparación exacta de calle y altura, considera null como vacío para piso/depto
     */
    @Query("SELECT po FROM ProfesionalObra po " +
           "WHERE po.obra.direccionObraCalle = :calle " +
           "AND po.obra.direccionObraAltura = :altura " +
           "AND (COALESCE(po.obra.direccionObraPiso, '') = COALESCE(:piso, '')) " +
           "AND (COALESCE(po.obra.direccionObraDepartamento, '') = COALESCE(:depto, '')) " +
           "AND po.empresaId = :empresaId")
    List<ProfesionalObra> findByDireccionObra(
        @Param("calle") String direccionObraCalle,
        @Param("altura") String direccionObraAltura,
        @Param("piso") String direccionObraPiso,
        @Param("depto") String direccionObraDepartamento,
        @Param("empresaId") Long empresaId
    );

    /**
     * Buscar asignaciones activas por dirección de obra
     */
    @Query("SELECT po FROM ProfesionalObra po " +
           "WHERE po.obra.direccionObraCalle = :calle " +
           "AND po.obra.direccionObraAltura = :altura " +
           "AND (COALESCE(po.obra.direccionObraPiso, '') = COALESCE(:piso, '')) " +
           "AND (COALESCE(po.obra.direccionObraDepartamento, '') = COALESCE(:depto, '')) " +
           "AND po.empresaId = :empresaId " +
           "AND LOWER(po.estado) = 'activo'")
    List<ProfesionalObra> findActivasByDireccionObra(
        @Param("calle") String direccionObraCalle,
        @Param("altura") String direccionObraAltura,
        @Param("piso") String direccionObraPiso,
        @Param("depto") String direccionObraDepartamento,
        @Param("empresaId") Long empresaId
    );

    /**
     * Buscar asignación específica de profesional en dirección de obra
     */
    @Query("SELECT po FROM ProfesionalObra po " +
           "WHERE po.profesional.id = :profesionalId " +
           "AND po.obra.direccionObraCalle = :calle " +
           "AND po.obra.direccionObraAltura = :altura " +
           "AND (COALESCE(po.obra.direccionObraPiso, '') = COALESCE(:piso, '')) " +
           "AND (COALESCE(po.obra.direccionObraDepartamento, '') = COALESCE(:depto, '')) " +
           "AND po.empresaId = :empresaId " +
           "AND LOWER(po.estado) = 'activo'")
    Optional<ProfesionalObra> findByProfesionalAndDireccionObra(
        @Param("profesionalId") Long profesionalId,
        @Param("calle") String direccionObraCalle,
        @Param("altura") String direccionObraAltura,
        @Param("piso") String direccionObraPiso,
        @Param("depto") String direccionObraDepartamento,
        @Param("empresaId") Long empresaId
    );
    
    /**
     * Buscar todas las asignaciones de una empresa (multi-tenant) - CON EAGER LOADING
     * Utiliza LEFT JOIN FETCH para cargar Profesional y Obra eagerly
     * Necesario para que el mapper pueda acceder a obra.id, obra.nombre, etc.
     */
    @Query("SELECT DISTINCT po FROM ProfesionalObra po " +
           "LEFT JOIN FETCH po.profesional " +
           "LEFT JOIN FETCH po.obra " +
           "WHERE po.empresaId = :empresaId")
    List<ProfesionalObra> findByEmpresaId(@Param("empresaId") Long empresaId);
    
    /**
     * Obtener TODAS las asignaciones con relaciones eager loaded
     * Necesario para que el mapper pueda acceder a obra.id, obra.nombre, etc.
     */
    @Query("SELECT DISTINCT po FROM ProfesionalObra po " +
           "LEFT JOIN FETCH po.profesional " +
           "LEFT JOIN FETCH po.obra " +
           "ORDER BY po.id DESC")
    List<ProfesionalObra> findAllWithRelations();
    
    /**
     * Buscar asignaciones por tipo de profesional con eager loading de relaciones
     * Incluye variaciones de género de forma flexible
     */
    @Query("SELECT DISTINCT po FROM ProfesionalObra po " +
           "LEFT JOIN FETCH po.profesional p " +
           "LEFT JOIN FETCH po.obra " +
           "WHERE po.empresaId = :empresaId " +
           "AND LOWER(po.estado) = 'activo' " +
           "AND (LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :terminoBase, '%')) OR " +
           "LOWER(p.tipoProfesional) LIKE LOWER(CONCAT('%', :terminoAlterno, '%')))")
    List<ProfesionalObra> buscarPorTipoFlexibleWithRelations(
            @Param("terminoBase") String terminoBase,
            @Param("terminoAlterno") String terminoAlterno,
            @Param("empresaId") Long empresaId);
    
    /**
     * Buscar relación profesional-obra por IDs (para pagos semanales)
     */
    @Query("SELECT po FROM ProfesionalObra po WHERE po.profesional.id = :profesionalId AND po.obra.id = :obraId")
    Optional<ProfesionalObra> findByProfesionalIdAndObraId(@Param("profesionalId") Long profesionalId, @Param("obraId") Long obraId);

    /**
     * Buscar profesionales asignados a una obra específica con eager loading de relaciones
     * Para sistema de adelantos y pagos - incluye datos del profesional
     */
    @Query("SELECT DISTINCT po FROM ProfesionalObra po " +
           "LEFT JOIN FETCH po.profesional p " +
           "LEFT JOIN FETCH po.obra o " +
           "WHERE po.obra.id = :obraId " +
           "AND po.empresaId = :empresaId " +
           "ORDER BY p.nombre ASC")
    List<ProfesionalObra> findByObraIdAndEmpresaIdWithRelations(
            @Param("obraId") Long obraId,
            @Param("empresaId") Long empresaId);
}