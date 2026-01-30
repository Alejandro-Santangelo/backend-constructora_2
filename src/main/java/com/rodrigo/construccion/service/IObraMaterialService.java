package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarMaterialRequestDTO;
import com.rodrigo.construccion.dto.response.ObraMaterialResponseDTO;

import java.util.List;

public interface IObraMaterialService {

    ObraMaterialResponseDTO asignar(Long empresaId, AsignarMaterialRequestDTO request);

    List<ObraMaterialResponseDTO> obtenerPorObra(Long empresaId, Long obraId);

    ObraMaterialResponseDTO obtenerPorId(Long empresaId, Long id);

    ObraMaterialResponseDTO actualizar(Long empresaId, Long id, AsignarMaterialRequestDTO request);

    void eliminar(Long empresaId, Long obraId, Long id);
}
