package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.entity.ObraOtroCosto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gestionar otros costos asignados a obras
 */
@Repository
public interface IObraOtroCostoRepository extends JpaRepository<ObraOtroCosto, Long> {

    /**
     * Buscar asignaciones por obra y empresa ordenadas por semana y fecha.
     * Las asignaciones semanales (sin fecha) van primero dentro de cada semana.
     */
    @Query("""
        SELECT ooc FROM ObraOtroCosto ooc 
        WHERE ooc.obraId = :obraId 
          AND ooc.empresaId = :empresaId
        ORDER BY ooc.semana ASC, ooc.fechaAsignacion ASC NULLS FIRST
        """)
    List<ObraOtroCosto> findByObraIdAndEmpresaIdOrderByFechaAsignacionDesc(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);

    /**
     * Buscar asignación específica por ID y empresa
     */
    ObraOtroCosto findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Obtener asignaciones con orden optimizado para visualización:
     * primero por semana, luego asignaciones semanales (null), luego diarias ordenadas
     */
    @Query("""
        SELECT ooc FROM ObraOtroCosto ooc 
        WHERE ooc.obraId = :obraId 
          AND ooc.empresaId = :empresaId
        ORDER BY ooc.semana ASC, ooc.fechaAsignacion ASC NULLS FIRST
        """)
    List<ObraOtroCosto> findByObraIdAndEmpresaIdWithDetails(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);
}