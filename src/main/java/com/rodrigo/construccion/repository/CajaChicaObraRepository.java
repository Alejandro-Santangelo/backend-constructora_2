package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.CajaChicaObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CajaChicaObraRepository extends JpaRepository<CajaChicaObra, Long> {

    /* Obtener todas las asignaciones de caja chica de una obra */
    List<CajaChicaObra> findByPresupuestoNoClienteId(Long presupuestoNoClienteId);

    /* Obtener todas las asignaciones de caja chica de un profesional */
    List<CajaChicaObra> findByProfesionalObraId(Long profesionalObraId);

}
