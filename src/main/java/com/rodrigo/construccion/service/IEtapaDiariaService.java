package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.EtapaDiariaRequestDTO;
import com.rodrigo.construccion.dto.response.EtapaDiariaResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz del servicio para etapas diarias
 */
public interface IEtapaDiariaService {

    /**
     * Obtener todas las etapas diarias de una obra
     */
    List<EtapaDiariaResponseDTO> obtenerPorObra(Long empresaId, Long obraId);

    /**
     * Obtener una etapa diaria por ID
     */
    EtapaDiariaResponseDTO obtenerPorId(Long empresaId, Long id);

    /**
     * Obtener etapa diaria por obra y fecha
     */
    EtapaDiariaResponseDTO obtenerPorObraYFecha(Long empresaId, Long obraId, LocalDate fecha);

    /**
     * Crear una nueva etapa diaria
     */
    EtapaDiariaResponseDTO crear(Long empresaId, EtapaDiariaRequestDTO request);

    /**
     * Actualizar una etapa diaria existente
     */
    EtapaDiariaResponseDTO actualizar(Long empresaId, Long id, EtapaDiariaRequestDTO request);

    /**
     * Eliminar una etapa diaria
     */
    void eliminar(Long empresaId, Long id);
}
