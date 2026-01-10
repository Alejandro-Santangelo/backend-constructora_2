package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.GastoGeneral;

import java.util.List;

/**
 * Interfaz de servicio para gestionar el catálogo de gastos generales
 */
public interface IGastoGeneralService {

    /**
     * Crear un nuevo gasto general
     */
    GastoGeneral crear(Long empresaId, GastoGeneral gastoGeneral);

    /**
     * Actualizar un gasto general existente
     */
    GastoGeneral actualizar(Long empresaId, Long id, GastoGeneral gastoGeneral);

    /**
     * Eliminar un gasto general
     */
    void eliminar(Long empresaId, Long id);

    /**
     * Obtener un gasto general por ID
     */
    GastoGeneral obtenerPorId(Long empresaId, Long id);

    /**
     * Listar todos los gastos generales de una empresa
     */
    List<GastoGeneral> listarPorEmpresa(Long empresaId);

    /**
     * Listar gastos generales por categoría
     */
    List<GastoGeneral> listarPorCategoria(Long empresaId, String categoria);

    /**
     * Actualizar precio de todos los gastos generales de una empresa
     */
    void actualizarPrecioTodos(Long empresaId, double porcentaje);

    /**
     * Actualizar precio de un gasto general específico
     */
    void actualizarPrecioPorId(Long empresaId, Long id, double porcentaje);

    /**
     * Actualizar precio de varios gastos generales
     */
    void actualizarPrecioVarios(Long empresaId, List<Long> ids, double porcentaje);
}
