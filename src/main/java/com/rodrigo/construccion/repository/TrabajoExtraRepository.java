package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad TrabajoExtra
 * Todas las consultas incluyen filtrado por empresa (Multi-Tenant)
 */
@Repository
public interface TrabajoExtraRepository extends JpaRepository<TrabajoExtra, Long> {

    /**
     * Buscar trabajos extra por obra y empresa
     */
    @Query("SELECT te FROM TrabajoExtra te WHERE te.obraId = :obraId AND te.empresaId = :empresaId")
    List<TrabajoExtra> findByObraIdAndEmpresaId(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);

    /**
     * Buscar trabajo extra por ID y empresa (validación multi-tenant)
     */
    @Query("SELECT te FROM TrabajoExtra te WHERE te.id = :id AND te.empresaId = :empresaId")
    Optional<TrabajoExtra> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Verificar si existe un trabajo extra por ID y empresa
     */
    @Query("SELECT COUNT(te) > 0 FROM TrabajoExtra te WHERE te.id = :id AND te.empresaId = :empresaId")
    boolean existsByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Buscar trabajos extra por empresa
     */
    @Query("SELECT te FROM TrabajoExtra te WHERE te.empresaId = :empresaId")
    List<TrabajoExtra> findByEmpresaId(@Param("empresaId") Long empresaId);
}
