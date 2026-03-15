package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.response.RubroResponseDTO;
import com.rodrigo.construccion.model.entity.Rubro;
import com.rodrigo.construccion.repository.RubroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Rubros
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RubroService {

    private final RubroRepository rubroRepository;

    /**
     * Obtener todos los rubros activos
     */
    @Transactional(readOnly = true)
    public List<RubroResponseDTO> obtenerRubrosActivos() {
        List<Rubro> rubros = rubroRepository.findByActivoTrue();
        return rubros.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los rubros (activos e inactivos)
     */
    @Transactional(readOnly = true)
    public List<RubroResponseDTO> obtenerTodos() {
        List<Rubro> rubros = rubroRepository.findAll();
        return rubros.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener rubros por categoría
     */
    @Transactional(readOnly = true)
    public List<RubroResponseDTO> obtenerPorCategoria(String categoria) {
        List<Rubro> rubros = rubroRepository.findByCategoriaAndActivoTrue(categoria);
        return rubros.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Buscar rubros por nombre
     */
    @Transactional(readOnly = true)
    public List<RubroResponseDTO> buscarPorNombre(String texto) {
        List<Rubro> rubros = rubroRepository.buscarPorNombre(texto);
        return rubros.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertir entidad a DTO
     */
    private RubroResponseDTO toDTO(Rubro rubro) {
        return RubroResponseDTO.builder()
                .id(rubro.getId())
                .nombre(rubro.getNombre())
                .descripcion(rubro.getDescripcion())
                .categoria(rubro.getCategoria())
                .activo(rubro.getActivo())
                .build();
    }
}
