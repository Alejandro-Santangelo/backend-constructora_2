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

    /* Obtiene todas las asignaciones ACTIVAS de una obra específica */
    List<AsignacionProfesionalObra> findByObra_IdAndEmpresaIdAndEstado(Long obraId, Long empresaId, String estado);

    /* Elimina todas las asignaciones de una obra y empresa */
    void deleteByObra_IdAndEmpresaId(Long obraId, Long empresaId);
    
    /* Obtiene todas las asignaciones de una obra (sin filtrar por estado - para uso interno) */
    List<AsignacionProfesionalObra> findByObra_IdAndEmpresaId(Long obraId, Long empresaId);

    /* Suma total de jornales asignados ACTIVOS para un item específico del presupuesto
     * Usado para validar que no se exceda la cantidad total de jornales disponibles en el presupuesto
     */
    @Query("SELECT COALESCE(SUM(a.cantidadJornales), 0) FROM AsignacionProfesionalObra a " +
           "WHERE a.obraId = :obraId " +
           "AND a.itemId = :itemId " +
           "AND a.tipoAsignacion = 'JORNAL' " +
           "AND a.estado = 'ACTIVO'")
    Integer sumJornalesAsignadosByObraAndItem(@Param("obraId") Long obraId, @Param("itemId") Long itemId);
}
