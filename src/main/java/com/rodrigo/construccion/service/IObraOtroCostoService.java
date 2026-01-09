package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarOtroCostoRequestDTO;
import com.rodrigo.construccion.dto.response.ObraOtroCostoResponseDTO;

import java.util.List;

/**
 * Interfaz de servicio para gestionar asignación de otros costos a obras
 */
public interface IObraOtroCostoService {

    /**
     * Asigna un otro costo del presupuesto a una obra
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param request Datos de la asignación
     * @return Otro costo asignado
     */
    ObraOtroCostoResponseDTO asignar(Long empresaId, AsignarOtroCostoRequestDTO request);

    /**
     * Obtiene todos los otros costos asignados a una obra
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param obraId ID de la obra
     * @return Lista de otros costos asignados
     */
    List<ObraOtroCostoResponseDTO> obtenerPorObra(Long empresaId, Long obraId);

    /**
     * Obtiene un otro costo asignado por su ID
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param id ID de la asignación
     * @return Otro costo asignado
     */
    ObraOtroCostoResponseDTO obtenerPorId(Long empresaId, Long id);

    /**
     * Actualiza el importe asignado de un otro costo
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param id ID de la asignación
     * @param request Nuevos datos
     * @return Otro costo actualizado
     */
    ObraOtroCostoResponseDTO actualizar(Long empresaId, Long id, AsignarOtroCostoRequestDTO request);

    /**
     * Elimina una asignación de otro costo a obra
     * 
     * @param empresaId ID de la empresa (multi-tenancy)
     * @param id ID de la asignación
     */
    void eliminar(Long empresaId, Long id);
}
