package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarMaterialRequestDTO;
import com.rodrigo.construccion.dto.response.ObraMaterialResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para gestionar asignación de materiales a obras
 */
public interface IObraMaterialService {

    /**
     * Asigna un material del presupuesto a una obra
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param request Datos de la asignación
     * @return Material asignado
     */
    ObraMaterialResponseDTO asignar(Long empresaId, AsignarMaterialRequestDTO request);

    /**
     * Obtiene todos los materiales asignados a una obra
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param obraId ID de la obra
     * @return Lista de materiales asignados
     */
    List<ObraMaterialResponseDTO> obtenerPorObra(Long empresaId, Long obraId);

    /**
     * Obtiene un material asignado por su ID
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param id ID de la asignación
     * @return Material asignado
     */
    ObraMaterialResponseDTO obtenerPorId(Long empresaId, Long id);

    /**
     * Actualiza la cantidad asignada de un material
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param id ID de la asignación
     * @param request Nuevos datos
     * @return Material actualizado
     */
    ObraMaterialResponseDTO actualizar(Long empresaId, Long id, AsignarMaterialRequestDTO request);

    /**
     * Elimina una asignación de material a obra
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param id ID de la asignación
     */
    void eliminar(Long empresaId, Long id);
}
