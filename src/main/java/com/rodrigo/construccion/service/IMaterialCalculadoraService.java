package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.MaterialCalculadora;

import java.util.Optional;

public interface IMaterialCalculadoraService {
    MaterialCalculadora buscarPorId(Long itemCalculadoraId);

    /* Busca un MaterialCalculadora por ID sin lanzar excepción si no existe */
    MaterialCalculadora buscarPorIdOpcional(Long id);
}
