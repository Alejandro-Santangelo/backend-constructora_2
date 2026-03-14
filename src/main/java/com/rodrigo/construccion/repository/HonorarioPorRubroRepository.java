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
     * Obtiene los rubros activos del presupuesto aprobado más reciente de una obra.
     * Si no hay presupuesto aprobado, devuelve lista vacía.
     */
    @Query("SELECT h FROM HonorarioPorRubro h " +
           "WHERE h.presupuestoNoCliente.id = (" +
           "  SELECT p.id FROM PresupuestoNoCliente p " +
           "  WHERE p.obra.id = :obraId AND p.estado = 'APROBADO' " +
           "  ORDER BY p.numeroVersion DESC LIMIT 1" +
           ") AND h.activo = true")
    List<HonorarioPorRubro> findRubrosActivosByObraId(@Param("obraId") Long obraId);
}
