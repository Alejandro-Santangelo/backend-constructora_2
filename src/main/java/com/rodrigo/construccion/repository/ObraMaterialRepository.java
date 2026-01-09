package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.ObraMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para ObraMaterial
 */
@Repository
public interface ObraMaterialRepository extends JpaRepository<ObraMaterial, Long> {

    /**
     * Busca todos los materiales asignados a una obra de una empresa específica
     */
    List<ObraMaterial> findByObraIdAndEmpresaId(Long obraId, Long empresaId);

    /**
     * Busca una asignación por ID y empresaId (para multi-tenancy)
     */
    Optional<ObraMaterial> findByIdAndEmpresaId(Long id, Long empresaId);

    // LEGACY: Los siguientes métodos están comentados porque referencian entidades eliminadas
    
    // /**
    //  * Busca una asignación específica de material a obra
    //  */
    // Optional<ObraMaterial> findByObraIdAndPresupuestoMaterialId(Long obraId, Long presupuestoMaterialId);

    // /**
    //  * Verifica si ya existe una asignación de ese material a esa obra
    //  */
    // boolean existsByObraIdAndPresupuestoMaterialId(Long obraId, Long presupuestoMaterialId);

    // /**
    //  * Obtiene materiales asignados con información completa del material
    //  */
    // @Query("SELECT om FROM ObraMaterial om " +
    //        "JOIN FETCH om.material m " +
    //        "WHERE om.obraId = :obraId AND om.empresaId = :empresaId " +
    //        "ORDER BY m.nombreMaterial")
    // List<ObraMaterial> findByObraIdWithMaterial(@Param("obraId") Long obraId, 
    //                                               @Param("empresaId") Long empresaId);

    /**
     * Elimina todos los materiales asignados a una obra
     */
    void deleteByObraId(Long obraId);
    
    /**
     * Elimina todas las asignaciones que referencian un material específico de calculadora
     */
    void deleteByMaterialCalculadoraId(Long materialCalculadoraId);
}
