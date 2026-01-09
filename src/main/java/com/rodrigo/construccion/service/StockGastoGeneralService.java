package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.GastoGeneral;
import com.rodrigo.construccion.model.entity.StockGastoGeneral;
import com.rodrigo.construccion.repository.GastoGeneralRepository;
import com.rodrigo.construccion.repository.StockGastoGeneralRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StockGastoGeneralService {
    
    private static final Logger log = LoggerFactory.getLogger(StockGastoGeneralService.class);
    
    @Autowired
    private StockGastoGeneralRepository stockGastoGeneralRepository;
    
    @Autowired
    private GastoGeneralRepository gastoGeneralRepository;
    
    public BigDecimal obtenerCantidadDisponible(Long gastoGeneralId, Long empresaId) {
        log.info("Obteniendo cantidad disponible para gasto general ID: {} en empresa: {}", gastoGeneralId, empresaId);
        
        Optional<StockGastoGeneral> stock = stockGastoGeneralRepository
            .findByGastoGeneralIdAndEmpresaId(gastoGeneralId, empresaId);
        
        if (stock.isPresent()) {
            BigDecimal cantidad = stock.get().getCantidadDisponible();
            log.info("Cantidad disponible encontrada: {}", cantidad);
            return cantidad;
        } else {
            log.info("No se encontró stock para el gasto general ID: {}, retornando 0", gastoGeneralId);
            return BigDecimal.ZERO;
        }
    }
    
    public void registrarMovimiento(Long gastoGeneralId, BigDecimal cantidad, String tipoMovimiento, 
                                  String descripcion, Long empresaId) {
        log.info("Registrando movimiento de stock - Gasto ID: {}, Cantidad: {}, Tipo: {}, Empresa: {}", 
                gastoGeneralId, cantidad, tipoMovimiento, empresaId);
        
        Optional<StockGastoGeneral> stockOpt = stockGastoGeneralRepository
            .findByGastoGeneralIdAndEmpresaId(gastoGeneralId, empresaId);
        
        if (stockOpt.isPresent()) {
            StockGastoGeneral stock = stockOpt.get();
            BigDecimal cantidadActual = stock.getCantidadDisponible();
            
            if ("SALIDA".equals(tipoMovimiento)) {
                if (cantidadActual.compareTo(cantidad) >= 0) {
                    stock.setCantidadDisponible(cantidadActual.subtract(cantidad));
                    log.info("Stock actualizado correctamente. Cantidad anterior: {}, Nueva cantidad: {}", 
                            cantidadActual, stock.getCantidadDisponible());
                } else {
                    log.warn("Stock insuficiente. Disponible: {}, Solicitado: {}", cantidadActual, cantidad);
                    throw new RuntimeException("Stock insuficiente para el gasto general");
                }
            } else if ("ENTRADA".equals(tipoMovimiento)) {
                stock.setCantidadDisponible(cantidadActual.add(cantidad));
                log.info("Stock incrementado. Cantidad anterior: {}, Nueva cantidad: {}", 
                        cantidadActual, stock.getCantidadDisponible());
            }
            
            stockGastoGeneralRepository.save(stock);
        } else {
            log.warn("No se encontró stock para el gasto general ID: {}", gastoGeneralId);
            throw new RuntimeException("Stock no encontrado para el gasto general especificado");
        }
    }
    
    public List<StockGastoGeneral> obtenerTodoElStock(Long empresaId) {
        log.info("Obteniendo todo el stock de gastos generales para empresa: {}", empresaId);
        return stockGastoGeneralRepository.findByEmpresaIdWithGastoGeneralOrderByNombre(empresaId);
    }
    
    public List<StockGastoGeneral> obtenerStockBajo(Long empresaId) {
        log.info("Obteniendo stock bajo de gastos generales para empresa: {}", empresaId);
        return stockGastoGeneralRepository.findStockBajoByEmpresaId(empresaId);
    }
    
    public Optional<StockGastoGeneral> obtenerStockPorGastoGeneral(Long gastoGeneralId, Long empresaId) {
        log.info("Obteniendo stock para gasto general ID: {} en empresa: {}", gastoGeneralId, empresaId);
        return stockGastoGeneralRepository.findByGastoGeneralIdAndEmpresaId(gastoGeneralId, empresaId);
    }
    
    public StockGastoGeneral crearStock(Long gastoGeneralId, BigDecimal cantidadInicial, 
                                       BigDecimal cantidadMinima, BigDecimal precioUnitario, 
                                       Long empresaId) {
        log.info("Creando nuevo stock para gasto general ID: {} con cantidad inicial: {}", 
                gastoGeneralId, cantidadInicial);
        
        // Verificar que el gasto general existe
        Optional<GastoGeneral> gastoOpt = gastoGeneralRepository.findByIdAndEmpresaId(gastoGeneralId, empresaId);
        if (gastoOpt.isEmpty()) {
            throw new RuntimeException("Gasto general no encontrado");
        }
        
        // Verificar que no existe stock previo
        Optional<StockGastoGeneral> stockExistente = stockGastoGeneralRepository
            .findByGastoGeneralIdAndEmpresaId(gastoGeneralId, empresaId);
        if (stockExistente.isPresent()) {
            throw new RuntimeException("Ya existe stock para este gasto general");
        }
        
        StockGastoGeneral nuevoStock = new StockGastoGeneral(gastoGeneralId, cantidadInicial, 
                                                           cantidadMinima, precioUnitario, empresaId);
        
        return stockGastoGeneralRepository.save(nuevoStock);
    }
}