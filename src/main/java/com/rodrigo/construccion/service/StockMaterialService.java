
package com.rodrigo.construccion.service;

import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.StockMaterial;
import com.rodrigo.construccion.repository.StockMaterialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class StockMaterialService {
    /**
     * Obtener stock con paginación
     */
    public Page<StockMaterial> obtenerStockPaginado(Pageable pageable) {
        log.info("Obteniendo stock paginado");
        return stockMaterialRepository.findAll(pageable);
    }

    /**
     * Buscar stock por ubicación
     */
        public List<StockMaterial> buscarPorUbicacion(Long empresaId, String ubicacion) {
            log.info("Buscando stock por ubicación: {}", ubicacion);
            return stockMaterialRepository.findByEmpresaIdAndUbicacionContaining(empresaId, ubicacion);
        }

    /**
     * Obtener stock agotado
     */
        public List<StockMaterial> obtenerStockAgotado(Long empresaId) {
            log.info("Obteniendo stock agotado");
            return stockMaterialRepository.findStockMenorA(empresaId, 1.0);
        }
    private final StockMaterialRepository stockMaterialRepository;

    public StockMaterialService(StockMaterialRepository stockMaterialRepository) {
        this.stockMaterialRepository = stockMaterialRepository;
    }

    /**
     * Crear nuevo registro de stock
     */
    @Transactional
    public StockMaterial crear(StockMaterial stock) {
        log.info("Creando nuevo registro de stock");
        return stockMaterialRepository.save(stock);
    }

    /**
     * Obtener todo el stock
     */
    public List<StockMaterial> obtenerTodoStock() {
        log.info("Obteniendo todo el stock de materiales");
        return stockMaterialRepository.findAll();
    }

    /**
     * Actualizar stock existente
     */
    @Transactional
    public StockMaterial actualizar(Long id, StockMaterial stockActualizado) {
        log.info("Actualizando stock ID: {}", id);
        obtenerPorId(id);
        stockActualizado.setId(id);
        return stockMaterialRepository.save(stockActualizado);
    }

    /**
     * Obtener stock por ID
     */
    public StockMaterial obtenerPorId(Long id) {
        log.info("Obteniendo stock por ID: {}", id);
        return stockMaterialRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Stock no encontrado con ID: " + id));
    }

    /**
     * Ajustar cantidad de stock (simplificado)
     */
    @Transactional
    public StockMaterial ajustarCantidad(Long id, Double nuevaCantidad, String motivo) {
        log.info("Ajustando cantidad de stock ID: {} a: {}", id, nuevaCantidad);
        StockMaterial stock = obtenerPorId(id);
        // Lógica de ajuste simplificada
        return stockMaterialRepository.save(stock);
    }

    /**
     * Obtener stock con cantidad baja (simplificado)
     */
    public List<StockMaterial> obtenerStockBajo() {
        log.info("Obteniendo stock con cantidad baja (funcionalidad simplificada)");
        return stockMaterialRepository.findAll();
    }

    /**
     * Eliminar stock
     */
    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando stock ID: {}", id);
        if (!stockMaterialRepository.existsById(id)) {
            throw new ResourceNotFoundException("Stock no encontrado con ID: " + id);
        }
        stockMaterialRepository.deleteById(id);
    }

    /**
     * Obtener stock próximo a vencer (simplificado)
     */
    public List<StockMaterial> obtenerStockProximoAVencer(int diasAntelacion) {
        log.info("Obteniendo stock próximo a vencer (funcionalidad simplificada)");
        return stockMaterialRepository.findAll();
    }

    /**
     * Obtener stock por material (simplificado)
     */
    public List<StockMaterial> obtenerPorMaterial(Long materialId) {
        log.info("Obteniendo stock por material ID: {}", materialId);
        return stockMaterialRepository.findAll();
    }

    /**
     * Obtener estadísticas básicas de stock
     */
    public Map<String, Object> obtenerEstadisticas() {
        log.info("Obteniendo estadísticas de stock");
        List<StockMaterial> todoStock = stockMaterialRepository.findAll();
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalRegistros", todoStock.size());
        return estadisticas;
    }

    /**
     * Obtener ubicaciones disponibles (simplificado)
     */
    public List<String> obtenerUbicaciones() {
        return List.of("Almacén Principal", "Almacén Secundario", "Obra", "Depósito");
    }

    /**
     * Obtener estados disponibles
     */
    public List<String> obtenerEstados() {
        return List.of("ACTIVO", "INACTIVO", "VENCIDO", "BLOQUEADO");
    }

    // =================== NUEVOS MÉTODOS PARA CONTROL DE STOCK ===================

    /**
     * Inicializar stock desde un presupuesto aprobado
     */
    @Transactional
    public void inicializarStockDesdePresupuesto(Long presupuestoId, Long empresaId, String ubicacion) {
        log.info("🏗️ Inicializando stock desde presupuesto {} en ubicación: {}", presupuestoId, ubicacion);
        
        // TODO: Implementar lógica para crear StockMaterial desde MaterialCalculadora
        // cuando un presupuesto es aprobado
    }

    /**
     * Obtener cantidad disponible de un material específico
     */
    @Transactional(readOnly = true)
    public Double obtenerCantidadDisponible(Long materialId, Long empresaId, String ubicacion) {
        log.info("📊 Obteniendo cantidad disponible material {} en ubicación {} empresa {}", materialId, ubicacion, empresaId);
        
        try {
            // Buscar el stock del material en ubicación específica
            Optional<StockMaterial> stock = stockMaterialRepository.findByMaterialIdAndEmpresaIdAndUbicacion(
                materialId, empresaId, ubicacion);
            
            Double cantidad = stock.map(StockMaterial::getCantidadActual).orElse(0.0);
            log.info("✅ Cantidad disponible encontrada: {}", cantidad);
            return cantidad;
        } catch (Exception e) {
            log.error("❌ Error obteniendo cantidad disponible: {}", e.getMessage());
            return 0.0; // Retornar 0 en caso de error
        }
    }

    /**
     * Obtener cantidad asignada de un material a obras
     */
    @Transactional(readOnly = true)
    public Double obtenerCantidadAsignada(Long materialCalculadoraId, Long empresaId) {
        log.info("📋 Obteniendo cantidad asignada material calculadora {}", materialCalculadoraId);
        
        // TODO: Consultar ObraMaterial para obtener la suma de cantidades asignadas
        // por ahora retornamos 0
        return 0.0;
    }

    /**
     * Registrar movimiento de material (entrada/salida)
     */
    @Transactional
    public void registrarMovimiento(Long stockMaterialId, String tipoMovimiento, 
                                   Double cantidad, String motivo, Long obraId) {
        log.info("📦 Registrando movimiento {} de {} unidades para stock {}", 
            tipoMovimiento, cantidad, stockMaterialId);
        
        StockMaterial stock = obtenerPorId(stockMaterialId);
        
        // Actualizar cantidad según el tipo de movimiento
        if ("ENTRADA".equals(tipoMovimiento)) {
            stock.setCantidadActual(stock.getCantidadActual() + cantidad);
        } else if ("SALIDA".equals(tipoMovimiento)) {
            if (stock.getCantidadActual() < cantidad) {
                throw new RuntimeException("Stock insuficiente. Disponible: " + stock.getCantidadActual() + 
                                         ", Solicitado: " + cantidad);
            }
            stock.setCantidadActual(stock.getCantidadActual() - cantidad);
        }
        
        // Actualizar fecha de último movimiento
        stock.setFechaUltimoMovimiento(java.time.LocalDateTime.now());
        
        // Actualizar estado según cantidad
        if (stock.getCantidadActual() <= 0) {
            stock.setEstado("AGOTADO");
        } else if (stock.getCantidadMinima() != null && 
                   stock.getCantidadActual() <= stock.getCantidadMinima()) {
            stock.setEstado("STOCK_BAJO");
        } else {
            stock.setEstado("ACTIVO");
        }
        
        stockMaterialRepository.save(stock);
        
        // TODO: Crear registro en MovimientoMaterial para auditoría
    }
}
