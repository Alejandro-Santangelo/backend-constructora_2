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
     * Buscar asignaciones por obra y empresa
     */
    List<ObraOtroCosto> findByObraIdAndEmpresaIdOrderByFechaAsignacionDesc(Long obraId, Long empresaId);

    /**
     * Buscar asignación específica por ID y empresa
     */
    ObraOtroCosto findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Obtener asignaciones con información de obra y gasto general
     */
    @Query("""
        SELECT ooc FROM ObraOtroCosto ooc 
        WHERE ooc.obraId = :obraId 
          AND ooc.empresaId = :empresaId
        ORDER BY ooc.fechaAsignacion DESC
        """)
    List<ObraOtroCosto> findByObraIdAndEmpresaIdWithDetails(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);
}