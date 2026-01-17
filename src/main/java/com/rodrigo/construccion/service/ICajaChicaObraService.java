package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarCajaChicaMultipleRequest;
import com.rodrigo.construccion.dto.response.CajaChicaObraResponseDTO;

import java.util.List;

public interface ICajaChicaObraService {

    List<CajaChicaObraResponseDTO> asignarCajaChicaMultiple(AsignarCajaChicaMultipleRequest request);

    List<CajaChicaObraResponseDTO> obtenerPorObra(Long empresaId, Long presupuestoNoClienteId);

    List<CajaChicaObraResponseDTO> obtenerPorProfesional(Long empresaId, Long profesionalObraId);

    CajaChicaObraResponseDTO rendir(Long empresaId, Long id);

    void anular(Long empresaId, Long id);

    CajaChicaObraResponseDTO obtenerPorId(Long empresaId, Long id);
}
