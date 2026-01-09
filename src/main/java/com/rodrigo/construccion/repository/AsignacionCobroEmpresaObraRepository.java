package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.AsignacionCobroEmpresaObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad AsignacionCobroEmpresaObra
 */
@Repository
public interface AsignacionCobroEmpresaObraRepository extends JpaRepository<AsignacionCobroEmpresaObra, Long> {

    /**
     * Buscar asignaciones por cobro empresa
     */
    @Query("SELECT a FROM AsignacionCobroEmpresaObra a WHERE a.cobroEmpresa.id = :cobroEmpresaId")
    List<AsignacionCobroEmpresaObra> findByCobroEmpresaId(@Param("cobroEmpresaId") Long cobroEmpresaId);

    /**
     * Buscar asignaciones por cobro obra
     */
    @Query("SELECT a FROM AsignacionCobroEmpresaObra a WHERE a.cobroObra.id = :cobroObraId")
    List<AsignacionCobroEmpresaObra> findByCobroObraId(@Param("cobroObraId") Long cobroObraId);

    /**
     * Verificar si existe una asignación entre cobro empresa y obra
     */
    @Query("SELECT COUNT(a) > 0 FROM AsignacionCobroEmpresaObra a " +
           "WHERE a.cobroEmpresa.id = :cobroEmpresaId " +
           "AND a.cobroObra.id = :cobroObraId")
    boolean existeAsignacion(@Param("cobroEmpresaId") Long cobroEmpresaId, 
                             @Param("cobroObraId") Long cobroObraId);

    /**
     * Buscar asignación específica
     */
    @Query("SELECT a FROM AsignacionCobroEmpresaObra a " +
           "WHERE a.cobroEmpresa.id = :cobroEmpresaId " +
           "AND a.cobroObra.id = :cobroObraId")
    Optional<AsignacionCobroEmpresaObra> findByCobroEmpresaIdAndCobroObraId(
        @Param("cobroEmpresaId") Long cobroEmpresaId, 
        @Param("cobroObraId") Long cobroObraId
    );

    /**
     * Contar asignaciones de un cobro empresa
     */
    @Query("SELECT COUNT(a) FROM AsignacionCobroEmpresaObra a " +
           "WHERE a.cobroEmpresa.id = :cobroEmpresaId")
    Long contarAsignacionesByCobroEmpresa(@Param("cobroEmpresaId") Long cobroEmpresaId);
}
