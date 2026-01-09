package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtraItemCalculadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para TrabajoExtraItemCalculadora.
 */
@Repository
public interface TrabajoExtraItemCalculadoraRepository extends JpaRepository<TrabajoExtraItemCalculadora, Long> {
    
    /**
     * Encuentra todos los items de un trabajo extra específico.
     */
    List<TrabajoExtraItemCalculadora> findByTrabajoExtraId(Long trabajoExtraId);
    
    /**
     * Encuentra todos los items de un trabajo extra con profesionales cargados.
     * Se divide en múltiples queries para evitar MultipleBagFetchException.
     */
    @Query("SELECT DISTINCT i FROM TrabajoExtraItemCalculadora i " +
           "LEFT JOIN FETCH i.profesionales " +
           "WHERE i.trabajoExtra.id = :trabajoExtraId")
    List<TrabajoExtraItemCalculadora> findByTrabajoExtraIdWithProfesionales(@Param("trabajoExtraId") Long trabajoExtraId);
    
    /**
     * Encuentra todos los items de un trabajo extra con materiales cargados.
     */
    @Query("SELECT DISTINCT i FROM TrabajoExtraItemCalculadora i " +
           "LEFT JOIN FETCH i.materialesLista " +
           "WHERE i.trabajoExtra.id = :trabajoExtraId")
    List<TrabajoExtraItemCalculadora> findByTrabajoExtraIdWithMateriales(@Param("trabajoExtraId") Long trabajoExtraId);
    
    /**
     * Encuentra todos los items de un trabajo extra con jornales cargados.
     */
    @Query("SELECT DISTINCT i FROM TrabajoExtraItemCalculadora i " +
           "LEFT JOIN FETCH i.jornales " +
           "WHERE i.trabajoExtra.id = :trabajoExtraId")
    List<TrabajoExtraItemCalculadora> findByTrabajoExtraIdWithJornales(@Param("trabajoExtraId") Long trabajoExtraId);
    
    /**
     * Encuentra todos los items de un trabajo extra con gastos generales cargados.
     */
    @Query("SELECT DISTINCT i FROM TrabajoExtraItemCalculadora i " +
           "LEFT JOIN FETCH i.gastosGenerales " +
           "WHERE i.trabajoExtra.id = :trabajoExtraId")
    List<TrabajoExtraItemCalculadora> findByTrabajoExtraIdWithGastos(@Param("trabajoExtraId") Long trabajoExtraId);
    
    /**
     * Encuentra todos los items de una empresa específica.
     */
    List<TrabajoExtraItemCalculadora> findByEmpresaId(Long empresaId);
    
    /**
     * Encuentra todos los items de un trabajo extra que son gastos generales.
     */
    List<TrabajoExtraItemCalculadora> findByTrabajoExtraIdAndEsGastoGeneralTrue(Long trabajoExtraId);
    
    /**
     * Elimina todos los items de un trabajo extra.
     */
    void deleteByTrabajoExtraId(Long trabajoExtraId);
    
    /**
     * Cuenta items por trabajo extra.
     */
    long countByTrabajoExtraId(Long trabajoExtraId);
}
