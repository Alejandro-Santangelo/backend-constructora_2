package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.response.ProfesionalObraFinancieroDTO;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;

import java.util.List;

public interface IPresupuestoNoClienteService {

    List<PresupuestoNoCliente> findAllByObraId(Long obraId);

    PresupuestoNoCliente obtenerPorId(Long id);

    List<PresupuestoNoCliente> buscarPorDireccionObra(String calle, String altura, String piso, String departamento);
    
    /**
     * Obtiene profesionales con datos financieros de un presupuesto.
     * Si el presupuesto está vinculado a obra (global), busca los profesionales en asignaciones_profesional_obra.
     */
    List<ProfesionalObraFinancieroDTO> obtenerProfesionalesFinancierosPorPresupuesto(Long presupuestoId, Long empresaId);
}
