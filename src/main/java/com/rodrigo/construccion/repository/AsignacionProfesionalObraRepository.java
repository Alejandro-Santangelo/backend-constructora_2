package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.AsignacionProfesionalObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para AsignacionProfesionalObra
 */
@Repository
public interface AsignacionProfesionalObraRepository extends JpaRepository<AsignacionProfesionalObra, Long> {

    /**
     * Obtiene todas las asignaciones ACTIVAS de una obra específica
     */
    List<AsignacionProfesionalObra> findByObra_IdAndEmpresaIdAndEstado(Long obraId, Long empresaId, String estado);

    /**
     * Elimina todas las asignaciones de una obra y empresa
     */
    void deleteByObra_IdAndEmpresaId(Long obraId, Long empresaId);
    
    /**
     * Obtiene todas las asignaciones de una obra (sin filtrar por estado - para uso interno)
     */
    List<AsignacionProfesionalObra> findByObra_IdAndEmpresaId(Long obraId, Long empresaId);

    /**
     * Obtiene todas las asignaciones de un profesional específico
     */
    List<AsignacionProfesionalObra> findByProfesional_IdAndEmpresaId(Long profesionalId, Long empresaId);

    /**
     * Obtiene asignaciones de un profesional en una obra
     */
    List<AsignacionProfesionalObra> findByObra_IdAndProfesional_IdAndEstado(Long obraId, Long profesionalId, String estado);

    /**
     * Suma total de jornales asignados ACTIVOS de un profesional
     * Usado para validar que no exceda sus jornales disponibles
     */
    @Query("SELECT COALESCE(SUM(a.cantidadJornales), 0) FROM AsignacionProfesionalObra a " +
           "WHERE a.profesional.id = :profesionalId " +
           "AND a.tipoAsignacion = 'JORNAL' " +
           "AND a.estado = 'ACTIVO'")
    Integer sumJornalesAsignadosByProfesionalId(@Param("profesionalId") Long profesionalId);

    /**
     * Obtiene asignaciones por rubro
     */
    List<AsignacionProfesionalObra> findByRubroIdAndEmpresaId(Long rubroId, Long empresaId);

    /**
     * Obtiene asignaciones por obra y rubro
     */
    List<AsignacionProfesionalObra> findByObra_IdAndRubroIdAndEmpresaId(Long obraId, Long rubroId, Long empresaId);

    /**
     * Busca asignación específica de un profesional en una obra con estado
     * Usado en asignación semanal para verificar si ya existe asignación activa
     */
    @Query("SELECT a FROM AsignacionProfesionalObra a " +
           "WHERE a.obra.id = :obraId " +
           "AND a.profesional.id = :profesionalId " +
           "AND a.empresaId = :empresaId " +
           "AND a.estado = :estado")
    java.util.Optional<AsignacionProfesionalObra> findAsignacionByObraAndProfesionalAndEstado(
            @Param("obraId") Long obraId,
            @Param("profesionalId") Long profesionalId,
            @Param("empresaId") Long empresaId,
            @Param("estado") String estado);

    /**
     * Suma total de jornales asignados ACTIVOS para un item específico del presupuesto
     * Usado para validar que no se exceda la cantidad total de jornales disponibles en el presupuesto
     */
    @Query("SELECT COALESCE(SUM(a.cantidadJornales), 0) FROM AsignacionProfesionalObra a " +
           "WHERE a.obra.id = :obraId " +
           "AND a.itemId = :itemId " +
           "AND a.tipoAsignacion = 'JORNAL' " +
           "AND a.estado = 'ACTIVO'")
    Integer sumJornalesAsignadosByObraAndItem(@Param("obraId") Long obraId, @Param("itemId") Long itemId);
}
