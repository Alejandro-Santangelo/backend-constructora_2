package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AdelantoRequestDTO;
import com.rodrigo.construccion.dto.response.AdelantoResponseDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface para el servicio de gestión de adelantos
 */
public interface IAdelantoService {

    /**
     * Crear un nuevo adelanto para un profesional
     * @param request Datos del adelanto
     * @return Adelanto creado
     * @throws IllegalArgumentException si las validaciones fallan
     * @throws ResourceNotFoundException si el profesional-obra no existe
     */
    AdelantoResponseDTO crearAdelanto(AdelantoRequestDTO request);

    /**
     * Obtener un adelanto por ID
     * @param id ID del adelanto
     * @param empresaId ID de la empresa (validación multi-tenant)
     * @return Adelanto encontrado
     * @throws ResourceNotFoundException si no se encuentra
     */
    AdelantoResponseDTO obtenerAdelantoPorId(Long id, Long empresaId);

    /**
     * Listar todos los adelantos de una empresa
     * @param empresaId ID de la empresa
     * @return Lista de adelantos
     */
    List<AdelantoResponseDTO> listarAdelantosPorEmpresa(Long empresaId);

    /**
     * Listar adelantos de un profesional específico
     * @param profesionalObraId ID de la asignación profesional-obra
     * @param empresaId ID de la empresa
     * @return Lista de adelantos
     */
    List<AdelantoResponseDTO> listarAdelantosPorProfesional(Long profesionalObraId, Long empresaId);

    /**
     * Listar adelantos pendientes de descontar de un profesional
     * @param profesionalObraId ID de la asignación profesional-obra
     * @param empresaId ID de la empresa
     * @return Lista de adelantos con saldo > 0
     */
    List<AdelantoResponseDTO> listarAdelantosPendientes(Long profesionalObraId, Long empresaId);

    /**
     * Listar adelantos de una obra
     * @param obraId ID de la obra
     * @param empresaId ID de la empresa
     * @return Lista de adelantos
     */
    List<AdelantoResponseDTO> listarAdelantosPorObra(Long obraId, Long empresaId);

    /**
     * Actualizar un adelanto (observaciones, estado)
     * @param id ID del adelanto
     * @param request Datos a actualizar
     * @param empresaId ID de la empresa
     * @return Adelanto actualizado
     */
    AdelantoResponseDTO actualizarAdelanto(Long id, AdelantoRequestDTO request, Long empresaId);

    /**
     * Anular un adelanto (cambia estado a CANCELADO)
     * @param id ID del adelanto
     * @param empresaId ID de la empresa
     * @param motivo Motivo de la anulación
     * @return Adelanto anulado
     * @throws IllegalStateException si ya tiene descuentos aplicados
     */
    AdelantoResponseDTO anularAdelanto(Long id, Long empresaId, String motivo);

    /**
     * Calcular total de adelantos pendientes de un profesional
     * @param profesionalObraId ID de la asignación
     * @return Suma de saldos pendientes
     */
    BigDecimal calcularTotalAdelantosPendientes(Long profesionalObraId);

    /**
     * Validar si se puede otorgar un adelanto
     * @param profesionalObraId ID de la asignación
     * @param monto Monto solicitado
     * @return true si se puede otorgar
     * @throws IllegalArgumentException con mensaje descriptivo si no se puede
     */
    boolean validarAdelantoDisponible(Long profesionalObraId, BigDecimal monto);

    /**
     * Obtener historial completo de adelantos de un profesional
     * @param profesionalObraId ID de la asignación
     * @param empresaId ID de la empresa
     * @return Lista de adelantos ordenados por fecha (más reciente primero)
     */
    List<AdelantoResponseDTO> obtenerHistorialAdelantos(Long profesionalObraId, Long empresaId);
}
