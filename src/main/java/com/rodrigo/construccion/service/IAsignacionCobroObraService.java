package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignacionCobroObraRequestDTO;
import com.rodrigo.construccion.dto.response.AsignacionCobroObraResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface IAsignacionCobroObraService {

    /**
     * Crear una nueva asignación de cobro a obra
     */
    AsignacionCobroObraResponseDTO crearAsignacion(AsignacionCobroObraRequestDTO request);

    /**
     * Actualizar una asignación existente
     */
    AsignacionCobroObraResponseDTO actualizarAsignacion(Long id, AsignacionCobroObraRequestDTO request);

    /**
     * Eliminar una asignación
     */
    void eliminarAsignacion(Long id);

    /**
     * Obtener asignación por ID
     */
    AsignacionCobroObraResponseDTO obtenerAsignacionPorId(Long id);

    /**
     * Obtener todas las asignaciones de un cobro
     */
    List<AsignacionCobroObraResponseDTO> obtenerAsignacionesPorCobro(Long cobroId);

    /**
     * Obtener todas las asignaciones de una obra
     */
    List<AsignacionCobroObraResponseDTO> obtenerAsignacionesPorObra(Long obraId);

    /**
     * Obtener asignaciones activas de un cobro
     */
    List<AsignacionCobroObraResponseDTO> obtenerAsignacionesActivasPorCobro(Long cobroId);

    /**
     * Calcular total asignado de un cobro
     */
    BigDecimal calcularTotalAsignadoPorCobro(Long cobroId);

    /**
     * Calcular total recibido por una obra
     */
    BigDecimal calcularTotalRecibidoPorObra(Long obraId);

    /**
     * Anular una asignación
     */
    AsignacionCobroObraResponseDTO anularAsignacion(Long id);

    /**
     * Obtener asignaciones por empresa
     */
    List<AsignacionCobroObraResponseDTO> obtenerAsignacionesPorEmpresa(Long empresaId);
}
