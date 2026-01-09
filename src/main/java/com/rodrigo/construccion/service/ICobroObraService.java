package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.CobroObraRequestDTO;
import com.rodrigo.construccion.dto.response.CobroObraResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ICobroObraService {

    /**
     * Crear un nuevo cobro
     */
    CobroObraResponseDTO crearCobro(CobroObraRequestDTO request);

    /**
     * Actualizar un cobro existente
     */
    CobroObraResponseDTO actualizarCobro(Long id, CobroObraRequestDTO request);

    /**
     * Obtener un cobro por ID
     */
    CobroObraResponseDTO obtenerCobroPorId(Long id);

    /**
     * Obtener todos los cobros de una obra
     */
    List<CobroObraResponseDTO> obtenerCobrosPorObra(Long obraId);

    /**
     * Obtener cobros pendientes de una obra
     */
    List<CobroObraResponseDTO> obtenerCobrosPendientes(Long obraId);

    /**
     * Obtener cobros vencidos
     */
    List<CobroObraResponseDTO> obtenerCobrosVencidos();

    /**
     * Obtener todos los cobros de una empresa
     */
    List<CobroObraResponseDTO> obtenerCobrosPorEmpresa(Long empresaId);

    /**
     * Marcar un cobro como cobrado
     */
    CobroObraResponseDTO marcarComoCobrado(Long id);

    /**
     * Marcar un cobro como cobrado con fecha específica
     */
    CobroObraResponseDTO marcarComoCobrado(Long cobroId, Long empresaId, LocalDate fechaCobro);

    /**
     * Anular un cobro
     */
    void anularCobro(Long id);

    /**
     * Anular un cobro con motivo
     */
    CobroObraResponseDTO anularCobro(Long cobroId, Long empresaId, String motivo);

    /**
     * Eliminar un cobro
     */
    void eliminarCobro(Long id);

    /**
     * Calcular total cobrado de una obra
     */
    BigDecimal calcularTotalCobrado(Long obraId);

    /**
     * Calcular total pendiente de cobro de una obra
     */
    BigDecimal calcularTotalPendiente(Long obraId);

    /**
     * Obtener cobros por rango de fechas
     */
    List<CobroObraResponseDTO> obtenerCobrosPorFechas(LocalDate desde, LocalDate hasta);

    /**
     * Actualizar cobros vencidos (cambiar estado a VENCIDO)
     */
    void actualizarCobrosVencidos();

    // ========== MÉTODOS POR DIRECCIÓN ==========

    /**
     * Obtener cobros por dirección exacta
     */
    List<CobroObraResponseDTO> obtenerCobrosPorDireccion(
        Long presupuestoNoClienteId,
        String calle,
        String altura,
        String barrio,
        String torre,
        String piso,
        String depto
    );

    /**
     * Obtener cobros pendientes por dirección
     */
    List<CobroObraResponseDTO> obtenerCobrosPendientesPorDireccion(
        Long presupuestoNoClienteId,
        String calle,
        String altura,
        String barrio,
        String torre,
        String piso,
        String depto
    );

    /**
     * Calcular total cobrado por dirección
     */
    BigDecimal calcularTotalCobradoPorDireccion(
        Long presupuestoNoClienteId,
        String calle,
        String altura,
        String barrio,
        String torre,
        String piso,
        String depto
    );

    /**
     * Calcular total pendiente por dirección
     */
    BigDecimal calcularTotalPendientePorDireccion(
        Long presupuestoNoClienteId,
        String calle,
        String altura,
        String barrio,
        String torre,
        String piso,
        String depto
    );

    /**
     * Marcar cobro como vencido
     */
    CobroObraResponseDTO marcarComoVencido(Long id);

    /**
     * Marcar cobro como vencido validando empresa
     */
    CobroObraResponseDTO marcarComoVencido(Long cobroId, Long empresaId);

    /**
     * Calcular saldo disponible de un cobro (monto total - suma de asignaciones)
     * Útil para saber cuánto se puede asignar aún
     */
    BigDecimal calcularSaldoDisponible(Long cobroId);
}
