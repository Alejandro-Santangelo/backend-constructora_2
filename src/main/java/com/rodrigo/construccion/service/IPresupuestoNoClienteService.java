package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;

import java.util.List;

public interface IPresupuestoNoClienteService {

    List<PresupuestoNoCliente> findAllByObraId(Long obraId);

    PresupuestoNoCliente obtenerPorId(Long id);
}
