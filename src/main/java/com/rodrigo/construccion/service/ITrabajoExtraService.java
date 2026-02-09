package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.TrabajoExtraRequestDTO;
import com.rodrigo.construccion.dto.response.TrabajoExtraResponseDTO;
import com.rodrigo.construccion.dto.response.TrabajoExtraPdfResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interfaz del servicio para trabajos extra
 */
public interface ITrabajoExtraService {

    /**
     * Obtener todos los trabajos extra de una obra
     */
    List<TrabajoExtraResponseDTO> obtenerPorObra(Long empresaId, Long obraId);

    /**
     * Obtener un trabajo extra por ID
     */
    TrabajoExtraResponseDTO obtenerPorId(Long empresaId, Long id);

    /**
     * Crear un nuevo trabajo extra
     */
    TrabajoExtraResponseDTO crear(Long empresaId, TrabajoExtraRequestDTO request);

    /**
     * Actualizar un trabajo extra existente
     */
    TrabajoExtraResponseDTO actualizar(Long empresaId, Long id, TrabajoExtraRequestDTO request);

    /**
     * Actualizar parcialmente un trabajo extra (PATCH)
     */
    TrabajoExtraResponseDTO actualizarParcial(Long empresaId, Long id, TrabajoExtraRequestDTO request);

    /**
     * Eliminar un trabajo extra
     */
    void eliminar(Long empresaId, Long id);

    /**
     * Eliminar un profesional específico de una asignación
     */
    void eliminarProfesional(Long empresaId, Long profesionalId);

    /**
     * Eliminar un material específico de una asignación
     */
    void eliminarMaterial(Long empresaId, Long materialId);

    /**
     * Eliminar un gasto general específico de una asignación
     */
    void eliminarGastoGeneral(Long empresaId, Long gastoId);

    /**
     * Guardar PDF de trabajo extra
     */
    TrabajoExtraPdfResponseDTO guardarPdf(Long empresaId, Long trabajoExtraId, MultipartFile archivo, String generadoPor);
}
