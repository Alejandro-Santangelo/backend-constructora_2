package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.CobroEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CobroEntidadRepository extends JpaRepository<CobroEntidad, Long> {

    /** Todos los cobros de una entidad financiera, ordenados por fecha desc. */
    List<CobroEntidad> findByEntidadFinanciera_IdAndEmpresaIdOrderByFechaCobroDesc(
            Long entidadFinancieraId, Long empresaId);

    /** Suma total de cobros de una entidad financiera. Retorna 0 si no hay cobros. */
    @Query("""
           SELECT COALESCE(SUM(c.monto), 0)
           FROM CobroEntidad c
           WHERE c.entidadFinanciera.id = :entidadFinancieraId
             AND c.empresaId = :empresaId
           """)
    BigDecimal sumMontoByEntidadFinancieraId(
            @Param("entidadFinancieraId") Long entidadFinancieraId,
            @Param("empresaId") Long empresaId);

    /** Sumas agrupadas por entidad financiera, para el endpoint de estadísticas múltiples. */
    @Query("""
           SELECT c.entidadFinanciera.id, COALESCE(SUM(c.monto), 0)
           FROM CobroEntidad c
           WHERE c.entidadFinanciera.id IN :ids
             AND c.empresaId = :empresaId
           GROUP BY c.entidadFinanciera.id
           """)
    List<Object[]> sumMontoGroupByEntidadFinancieraId(
            @Param("ids") List<Long> ids,
            @Param("empresaId") Long empresaId);
}
