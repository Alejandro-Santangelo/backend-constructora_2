package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.response.StockEstadisticasResponse;
import com.rodrigo.construccion.model.entity.StockMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IStockMaterialService {
    public List<StockMaterial> obtenerTodoStock();

    public Page<StockMaterial> obtenerStockPaginado(Pageable pageable);

    public StockMaterial obtenerPorId(Long id);

    public List<StockMaterial> buscarPorUbicacion(Long empresaId, String ubicacion);

    public List<StockMaterial> obtenerStockBajo(Long empresaId);

    public List<StockMaterial> obtenerStockAgotado(Long empresaId);

    public List<StockMaterial> obtenerStockProximoAVencer(Long empresaId, int diasAntelacion);

    public List<StockMaterial> obtenerPorMaterial(Long materialId, Long empresaId);

    public StockMaterial crear(StockMaterial stock);

    public StockMaterial actualizar(Long id, StockMaterial stockActualizado);

    public StockMaterial ajustarCantidad(Long id, Double nuevaCantidad, String motivo);

    public void eliminar(Long id);

    public StockEstadisticasResponse obtenerEstadisticas(Long empresaId);

    public List<String> obtenerUbicaciones();

    public List<String> obtenerEstados();

    public Double obtenerCantidadDisponible(Long materialId, Long empresaId, String ubicacion);

    public Double obtenerCantidadAsignada(Long materialCalculadoraId, Long empresaId);
}
