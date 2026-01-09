package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarCajaChicaMultipleRequest;
import com.rodrigo.construccion.dto.response.CajaChicaObraResponseDTO;

import java.util.List;

public interface ICajaChicaObraService {

    /**
     * Asignar caja chica a múltiples profesionales
     */
    List<CajaChicaObraResponseDTO> asignarCajaChicaMultiple(AsignarCajaChicaMultipleRequest request);

    /**
     * Obtener todas las asignaciones de una obra
     */
    List<CajaChicaObraResponseDTO> obtenerPorObra(Long empresaId, Long presupuestoNoClienteId);

    /**
     * Obtener todas las asignaciones de un profesional
     */
    List<CajaChicaObraResponseDTO> obtenerPorProfesional(Long empresaId, Long profesionalObraId);

    /**
     * Marcar caja chica como rendida
     */
    CajaChicaObraResponseDTO rendir(Long empresaId, Long id);

    /**
     * Anular caja chica
     */
    void anular(Long empresaId, Long id);

    /**
     * Obtener caja chica por ID
     */
    CajaChicaObraResponseDTO obtenerPorId(Long empresaId, Long id);
}
