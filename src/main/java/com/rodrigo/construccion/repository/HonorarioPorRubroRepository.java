package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.HonorarioPorRubro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HonorarioPorRubroRepository extends JpaRepository<HonorarioPorRubro, Long> {

    List<HonorarioPorRubro> findByPresupuestoNoClienteId(Long presupuestoNoClienteId);

    List<HonorarioPorRubro> findByPresupuestoNoClienteIdAndActivoTrue(Long presupuestoNoClienteId);

    /**
     * Obtiene los rubros activos del presupuesto aprobado/en ejecución/terminado más reciente de una obra.
     * Solo devuelve rubros de presupuestos con estados: APROBADO, EN_EJECUCION, TERMINADO.
     * Esto previene agregar rubros a obras con presupuestos en estos estados.
     * Si no hay presupuesto en estos estados, devuelve lista vacía.
     */
    @Query("SELECT h FROM HonorarioPorRubro h " +
           "WHERE h.presupuestoNoCliente.id = (" +
           "  SELECT p.id FROM PresupuestoNoCliente p " +
           "  WHERE p.obra.id = :obraId AND p.estado IN ('APROBADO', 'EN_EJECUCION', 'TERMINADO') " +
           "  ORDER BY p.numeroVersion DESC LIMIT 1" +
           ") AND h.activo = true")
    List<HonorarioPorRubro> findRubrosActivosByObraId(@Param("obraId") Long obraId);
}
