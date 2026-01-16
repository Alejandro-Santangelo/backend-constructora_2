
package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.response.StockEstadisticasResponse;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.StockMaterial;
import com.rodrigo.construccion.repository.StockMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMaterialService {

    private final StockMaterialRepository stockMaterialRepository;

    /* Obtener todo el stock */
    public List<StockMaterial> obtenerTodoStock() {
        return stockMaterialRepository.findAll();
    }

    /* Obtener stock con paginación */
    public Page<StockMaterial> obtenerStockPaginado(Pageable pageable) {
        return stockMaterialRepository.findAll(pageable);
    }

    /* Obtener stock por ID */
    public StockMaterial obtenerPorId(Long id) {
        return stockMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock no encontrado con ID: " + id));
    }

    /* Buscar stock por ubicación */
    public List<StockMaterial> buscarPorUbicacion(Long empresaId, String ubicacion) {
        return stockMaterialRepository.findByEmpresaIdAndUbicacionContaining(empresaId, ubicacion);
    }

    /* Obtener stock con cantidad baja */
    public List<StockMaterial> obtenerStockBajo(Long empresaId) {
        return stockMaterialRepository.findStockBajoByEmpresaId(empresaId);
    }

    /* Obtener stock agotado */
    public List<StockMaterial> obtenerStockAgotado(Long empresaId) {
        return stockMaterialRepository.findStockMenorA(empresaId, 1.0);
    }

    /* Obtener stock próximo a vencer */
    public List<StockMaterial> obtenerStockProximoAVencer(Long empresaId, int diasAntelacion) {
        return stockMaterialRepository.findStockProximoAVencer(empresaId, diasAntelacion);
    }

    /* Obtener stock por material y empresa */
    public List<StockMaterial> obtenerPorMaterial(Long materialId, Long empresaId) {
        return stockMaterialRepository.findByMaterialIdAndEmpresaId(materialId, empresaId);
    }

    /* Crear nuevo registro de stock */
    @Transactional
    public StockMaterial crear(StockMaterial stock) {
        return stockMaterialRepository.save(stock);
    }

    /* Actualizar stock existente */
    @Transactional
    public StockMaterial actualizar(Long id, StockMaterial stockActualizado) {
        obtenerPorId(id);
        stockActualizado.setId(id);
        return stockMaterialRepository.save(stockActualizado);
    }

    /* Ajustar cantidad de stock */
    @Transactional
    public StockMaterial ajustarCantidad(Long id, Double nuevaCantidad, String motivo) {
        // Validar que la nueva cantidad sea válida
        if (nuevaCantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }

        StockMaterial stock = obtenerPorId(id);

        // Registrar el ajuste para auditoría
        Double cantidadAnterior = stock.getCantidadActual();

        // Actualizar cantidad actual
        stock.setCantidadActual(nuevaCantidad);

        // Actualizar fecha de último movimiento
        stock.setFechaUltimoMovimiento(LocalDateTime.now());

        // Agregar motivo a las observaciones para auditoría
        String observacionAnterior = stock.getObservaciones() != null ? stock.getObservaciones() : "";
        String nuevaObservacion = String.format("[%s] Ajuste de cantidad: %.2f → %.2f. Motivo: %s",
                LocalDateTime.now().toString(),
                cantidadAnterior,
                nuevaCantidad,
                motivo != null ? motivo : "No especificado");

        // Agregar la nueva observación (manteniendo las anteriores)
        if (!observacionAnterior.isEmpty()) {
            stock.setObservaciones(observacionAnterior + "\n" + nuevaObservacion);
        } else {
            stock.setObservaciones(nuevaObservacion);
        }

        // Actualizar estado según la nueva cantidad
        if (nuevaCantidad <= 0) {
            stock.setEstado("AGOTADO");
        } else if (stock.getCantidadMinima() != null && nuevaCantidad <= stock.getCantidadMinima()) {
            stock.setEstado("STOCK_BAJO");
        } else {
            stock.setEstado("ACTIVO");
        }

        return stockMaterialRepository.save(stock);
    }

    /* Eliminar stock */
    @Transactional
    public void eliminar(Long id) {
        if (!stockMaterialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stock no encontrado con ID: " + id);
        }
        stockMaterialRepository.deleteById(id);
    }

    /* Obtener estadísticas completas de stock */
    @Transactional(readOnly = true)
    public StockEstadisticasResponse obtenerEstadisticas(Long empresaId) {
        List<StockMaterial> todoStock = stockMaterialRepository.findByEmpresaId(empresaId);

        // Total de registros de stock
        long totalRegistros = todoStock.size();

        // Total de materiales diferentes
        long totalMaterialesDiferentes = todoStock.stream()
                .map(StockMaterial::getMaterialId)
                .distinct()
                .count();

        // Materiales con stock bajo (cantidad actual < cantidad mínima)
        long materialesStockBajo = todoStock.stream()
                .filter(stock -> stock.getCantidadMinima() != null &&
                        stock.getCantidadActual() < stock.getCantidadMinima())
                .count();

        // Materiales agotados (cantidad <= 0)
        long materialesAgotados = todoStock.stream()
                .filter(stock -> stock.getCantidadActual() <= 0)
                .count();

        // Valor total del inventario
        java.math.BigDecimal valorTotalInventario = todoStock.stream()
                .filter(stock -> stock.getCantidadActual() > 0)
                .map(stock -> {
                    double cantidad = stock.getCantidadActual();
                    double precio = stock.getPrecioUnitarioPromedio() != null ? stock.getPrecioUnitarioPromedio() : 0.0;
                    return java.math.BigDecimal.valueOf(cantidad * precio);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total de ubicaciones distintas
        long totalUbicaciones = todoStock.stream()
                .map(StockMaterial::getUbicacion)
                .filter(ubicacion -> ubicacion != null && !ubicacion.isEmpty())
                .distinct()
                .count();

        // Materiales próximos a vencer (30 días)
        LocalDate fechaLimite = LocalDate.now().plusDays(30);
        long materialesProximosVencer = todoStock.stream()
                .filter(stock -> stock.getFechaVencimiento() != null &&
                        stock.getFechaVencimiento().isBefore(fechaLimite))
                .count();

        return StockEstadisticasResponse.builder()
                .totalRegistros(totalRegistros)
                .totalMaterialesDiferentes(totalMaterialesDiferentes)
                .materialesStockBajo(materialesStockBajo)
                .materialesAgotados(materialesAgotados)
                .valorTotalInventario(valorTotalInventario)
                .totalUbicaciones(totalUbicaciones)
                .materialesProximosVencer(materialesProximosVencer)
                .build();
    }

    /* Obtener ubicaciones disponibles (simplificado) */
    public List<String> obtenerUbicaciones() {
        return List.of("Almacén Principal", "Almacén Secundario", "Obra", "Depósito");
    }

    /* Obtener estados disponibles */
    public List<String> obtenerEstados() {
        return List.of("ACTIVO", "INACTIVO", "VENCIDO", "BLOQUEADO");
    }

    /* Obtener cantidad disponible de un material específico - usado en PresupuestoNoClienteService */
    @Transactional(readOnly = true)
    public Double obtenerCantidadDisponible(Long materialId, Long empresaId, String ubicacion) {
        // Validar parámetros obligatorios
        if (materialId == null) {
            throw new IllegalArgumentException("El ID del material no puede ser nulo");
        }
        if (empresaId == null) {
            throw new IllegalArgumentException("El ID de la empresa no puede ser nulo");
        }
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación no puede ser nula o vacía");
        }

        // Buscar el stock - si no existe, lanza ResourceNotFoundException
        StockMaterial stock = stockMaterialRepository
                .findByMaterialIdAndEmpresaIdAndUbicacion(materialId, empresaId, ubicacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No se encontró stock para el material ID: %d en la ubicación '%s' de la empresa ID: %d",
                                materialId, ubicacion, empresaId)
                ));

        return stock.getCantidadActual() != null ? stock.getCantidadActual() : 0.0;
    }

    /* Obtener cantidad asignada de un material a obras */
    @Transactional(readOnly = true)
    public Double obtenerCantidadAsignada(Long materialCalculadoraId, Long empresaId) {
        // TODO: Consultar ObraMaterial para obtener la suma de cantidades asignadas
        // por ahora retornamos 0
        return 0.0;
    }
}
