package com.rodrigo.construccion.service;

import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.MaterialCalculadora;
import com.rodrigo.construccion.repository.MaterialCalculadoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialCalculadoraService implements IMaterialCalculadoraService {

    private final MaterialCalculadoraRepository materialCalculadoraRepository;

    @Override
    @Transactional(readOnly = true)
    public MaterialCalculadora buscarPorId(Long itemCalculadoraId) {
        return materialCalculadoraRepository.findById(itemCalculadoraId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Material calculadora no encontrado con ID: " + itemCalculadoraId));
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialCalculadora buscarPorIdOpcional(Long id) {
        return materialCalculadoraRepository.findById(id).orElse(null);
    }
}
