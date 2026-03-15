package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.ObraMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObraMaterialRepository extends JpaRepository<ObraMaterial, Long> {

    /* Busca todos los materiales asignados a una obra de una empresa específica */
    List<ObraMaterial> findByObraIdAndEmpresaId(Long obraId, Long empresaId);

    /* Busca una asignación por ID y empresaId (para multi-tenancy) */
    Optional<ObraMaterial> findByIdAndEmpresaId(Long id, Long empresaId);

    /* Busca todas las asignaciones de materiales de una empresa */
    List<ObraMaterial> findByEmpresaId(Long empresaId);

    /* Elimina todas las asignaciones que referencian un material específico de calculadora */
    void deleteByMaterialCalculadoraId(Long materialCalculadoraId);
}
