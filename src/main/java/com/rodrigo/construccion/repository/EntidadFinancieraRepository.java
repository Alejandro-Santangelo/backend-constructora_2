package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.enums.TipoEntidadFinanciera;
import com.rodrigo.construccion.model.entity.EntidadFinanciera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntidadFinancieraRepository extends JpaRepository<EntidadFinanciera, Long> {

    /** Busca por clave natural (empresa, tipo, entidad original). */
    Optional<EntidadFinanciera> findByEmpresaIdAndTipoEntidadAndEntidadId(
            Long empresaId, TipoEntidadFinanciera tipoEntidad, Long entidadId);

    /** Todas las entidades activas de una empresa. */
    List<EntidadFinanciera> findByEmpresaIdAndActivoTrue(Long empresaId);

    /** Entidades activas de una empresa por tipo. */
    List<EntidadFinanciera> findByEmpresaIdAndTipoEntidadAndActivoTrue(
            Long empresaId, TipoEntidadFinanciera tipoEntidad);

    /** Busca por ID validando empresa (seguridad multi-tenant). */
    Optional<EntidadFinanciera> findByIdAndEmpresaId(Long id, Long empresaId);

    /** Busca múltiples IDs validando empresa (para endpoint de estadísticas). */
    @Query("SELECT ef FROM EntidadFinanciera ef WHERE ef.id IN :ids AND ef.empresaId = :empresaId AND ef.activo = true")
    List<EntidadFinanciera> findByIdsAndEmpresaId(
            @Param("ids") List<Long> ids,
            @Param("empresaId") Long empresaId);

    /** Verifica si ya existe una entidad financiera con presupuesto vinculado. */
    Optional<EntidadFinanciera> findByPresupuestoNoClienteIdAndEmpresaId(
            Long presupuestoNoClienteId, Long empresaId);
}
