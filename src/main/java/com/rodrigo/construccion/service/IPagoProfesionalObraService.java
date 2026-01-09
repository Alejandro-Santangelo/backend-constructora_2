package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.PagoProfesionalObraRequestDTO;
import com.rodrigo.construccion.dto.response.PagoProfesionalObraResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IPagoProfesionalObraService {

    /**
     * Listar todos los pagos de una empresa
     */
    List<PagoProfesionalObraResponseDTO> listarTodosPorEmpresa(Long empresaId);

    /**
     * Crear un nuevo pago a profesional
     */
    PagoProfesionalObraResponseDTO crearPago(PagoProfesionalObraRequestDTO request);

    /**
     * Actualizar un pago existente
     */
    PagoProfesionalObraResponseDTO actualizarPago(Long id, PagoProfesionalObraRequestDTO request);

    /**
     * Obtener un pago por ID
     */
    PagoProfesionalObraResponseDTO obtenerPagoPorId(Long id);

    /**
     * Obtener todos los pagos de un profesional en una obra
     */
    List<PagoProfesionalObraResponseDTO> obtenerPagosPorProfesional(Long profesionalObraId);

    /**
     * Obtener pagos por tipo (SEMANAL, ADELANTO, PREMIO, etc.)
     */
    List<PagoProfesionalObraResponseDTO> obtenerPagosPorTipo(Long profesionalObraId, String tipoPago);

    /**
     * Obtener adelantos de un profesional
     */
    List<PagoProfesionalObraResponseDTO> obtenerAdelantos(Long profesionalObraId);

    /**
     * Calcular adelantos pendientes de descontar
     */
    BigDecimal calcularAdelantosPendientes(Long profesionalObraId);

    /**
     * Calcular total pagado a un profesional
     */
    BigDecimal calcularTotalPagado(Long profesionalObraId);

    /**
     * Anular un pago
     */
    void anularPago(Long id);

    /**
     * Anular un pago con motivo
     */
    PagoProfesionalObraResponseDTO anularPago(Long pagoId, Long empresaId, String motivo);

    /**
     * Eliminar un pago
     */
    void eliminarPago(Long id);

    /**
     * Obtener pagos por rango de fechas
     */
    List<PagoProfesionalObraResponseDTO> obtenerPagosPorFechas(LocalDate desde, LocalDate hasta);

    /**
     * Verificar si existe pago semanal para un período
     */
    boolean existePagoSemanalEnPeriodo(Long profesionalObraId, LocalDate periodoDesde, LocalDate periodoHasta);

    /**
     * Calcular promedio de presentismo de un profesional
     */
    BigDecimal calcularPromedioPresentismo(Long profesionalObraId);

    /**
     * Obtener pagos pendientes de un profesional
     */
    List<PagoProfesionalObraResponseDTO> obtenerPagosPendientes(Long profesionalObraId);

    /**
     * Obtener pagos por tipo para toda la empresa
     */
    List<PagoProfesionalObraResponseDTO> obtenerPagosPorTipoEmpresa(String tipoPago);

    /**
     * Marcar un pago como pagado
     */
    PagoProfesionalObraResponseDTO marcarComoPagado(Long id, LocalDate fechaPago);

    /**
     * Marcar un pago como pagado con validación de empresa
     */
    PagoProfesionalObraResponseDTO marcarComoPagado(Long pagoId, Long empresaId, LocalDate fechaPago);
}
