package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.PagoCuentaRequestDTO;
import com.rodrigo.construccion.dto.response.PagoCuentaResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Interface del servicio para gestión de pagos a cuenta sobre rubros
 */
public interface IPagoCuentaService {

    /**
     * Crear un pago a cuenta sobre un item de rubro
     */
    PagoCuentaResponseDTO crearPagoCuenta(PagoCuentaRequestDTO request);

    /**
     * Obtener un pago por ID
     */
    PagoCuentaResponseDTO obtenerPagoPorId(Long id, Long empresaId);

    /**
     * Listar todos los pagos de un presupuesto
     */
    List<PagoCuentaResponseDTO> listarPagosPorPresupuesto(Long presupuestoId, Long empresaId);

    /**
     * Listar pagos de un rubro específico
     */
    List<PagoCuentaResponseDTO> listarPagosPorRubro(Long presupuestoId, Long empresaId, String nombreRubro);

    /**
     * Listar pagos de un item específico (rubro + tipo)
     */
    List<PagoCuentaResponseDTO> listarPagosPorItem(Long presupuestoId, Long empresaId, String nombreRubro, String tipoItem);

    /**
     * Obtener resumen de pagos por presupuesto
     * Retorna map con estructura: {nombreRubro: {tipoItem: {totalPresupuestado, totalPagado, saldoPendiente}}}
     */
    Map<String, Map<String, Map<String, BigDecimal>>> obtenerResumenPagos(Long presupuestoId, Long empresaId);

    /**
     * Eliminar un pago
     */
    void eliminarPago(Long id, Long empresaId);

    /**
     * Calcular totales de un item específico
     */
    Map<String, BigDecimal> calcularTotalesItem(Long presupuestoId, Long empresaId, String nombreRubro, String tipoItem);
}
