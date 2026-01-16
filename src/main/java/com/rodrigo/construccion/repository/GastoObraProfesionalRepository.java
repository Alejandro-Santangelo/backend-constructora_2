package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.GastoObraProfesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GastoObraProfesionalRepository extends JpaRepository<GastoObraProfesional, Long> {

    /* Buscar todos los gastos de un profesional */
    @Query("SELECT g FROM GastoObraProfesional g " +
            "WHERE g.profesionalObra.id = :profesionalObraId " +
            "AND g.empresaId = :empresaId " +
            "ORDER BY g.fechaHora DESC")
    List<GastoObraProfesional> findByProfesionalObraId(@Param("profesionalObraId") Long profesionalObraId, @Param("empresaId") Long empresaId
    );

    /* Buscar todos los gastos de una obra (por los 4 campos de dirección) */
    @Query("SELECT g FROM GastoObraProfesional g " +
            "WHERE g.profesionalObra.obra.direccionObraCalle = :calle " +
            "AND g.profesionalObra.obra.direccionObraAltura = :altura " +
            "AND (COALESCE(g.profesionalObra.obra.direccionObraPiso, '') = COALESCE(:piso, '')) " +
            "AND (COALESCE(g.profesionalObra.obra.direccionObraDepartamento, '') = COALESCE(:depto, '')) " +
            "AND g.empresaId = :empresaId " +
            "ORDER BY g.fechaHora DESC")
    List<GastoObraProfesional> findByDireccionObra(@Param("calle") String direccionObraCalle, @Param("altura") String direccionObraAltura,
                                                   @Param("piso") String direccionObraPiso,
                                                   @Param("depto") String direccionObraDepartamento,
                                                   @Param("empresaId") Long empresaId
    );

    /* Buscar gasto por ID con validación de empresa */
    @Query("SELECT g FROM GastoObraProfesional g " +
            "WHERE g.id = :id AND g.empresaId = :empresaId")
    Optional<GastoObraProfesional> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /* Contar gastos de un profesional */
    @Query("SELECT COUNT(g) FROM GastoObraProfesional g " +
            "WHERE g.profesionalObra.id = :profesionalObraId " +
            "AND g.empresaId = :empresaId")
    Long countByProfesionalObraId(@Param("profesionalObraId") Long profesionalObraId, @Param("empresaId") Long empresaId);
}
