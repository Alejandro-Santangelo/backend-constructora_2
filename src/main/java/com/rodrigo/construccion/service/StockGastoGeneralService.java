package com.rodrigo.construccion.service;

import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.exception.SaldoInsuficienteException;
import com.rodrigo.construccion.model.entity.StockGastoGeneral;
import com.rodrigo.construccion.repository.GastoGeneralRepository;
import com.rodrigo.construccion.repository.StockGastoGeneralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StockGastoGeneralService {

    @Autowired
    private StockGastoGeneralRepository stockGastoGeneralRepository;

    @Autowired
    private GastoGeneralRepository gastoGeneralRepository;

    public BigDecimal obtenerCantidadDisponible(Long gastoGeneralId, Long empresaId) {
        return stockGastoGeneralRepository
                .findByGastoGeneralIdAndEmpresaId(gastoGeneralId, empresaId)
                .map(StockGastoGeneral::getCantidadDisponible)
                .orElse(BigDecimal.ZERO);
    }

    /* MÉTODOS QUE NO ESTÁN SIENDO USADOS */
    public void registrarMovimiento(Long gastoGeneralId, BigDecimal cantidad, String tipoMovimiento,
                                    String descripcion, Long empresaId) {
        StockGastoGeneral stock = stockGastoGeneralRepository
                .findByGastoGeneralIdAndEmpresaId(gastoGeneralId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("StockGastoGeneral", "gastoGeneralId", gastoGeneralId));

        BigDecimal cantidadActual = stock.getCantidadDisponible();

        if ("SALIDA".equals(tipoMovimiento)) {
            if (cantidadActual.compareTo(cantidad) < 0) {
                throw new SaldoInsuficienteException(
                        String.format("Stock insuficiente para el gasto general. Disponible: %s, Solicitado: %s",
                                cantidadActual, cantidad));
            }
            stock.setCantidadDisponible(cantidadActual.subtract(cantidad));
        } else if ("ENTRADA".equals(tipoMovimiento)) {
            stock.setCantidadDisponible(cantidadActual.add(cantidad));
        }

        stockGastoGeneralRepository.save(stock);
    }

    public List<StockGastoGeneral> obtenerTodoElStock(Long empresaId) {
        return stockGastoGeneralRepository.findByEmpresaIdWithGastoGeneralOrderByNombre(empresaId);
    }

    public List<StockGastoGeneral> obtenerStockBajo(Long empresaId) {
        return stockGastoGeneralRepository.findStockBajoByEmpresaId(empresaId);
    }

    public Optional<StockGastoGeneral> obtenerStockPorGastoGeneral(Long gastoGeneralId, Long empresaId) {
        return stockGastoGeneralRepository.findByGastoGeneralIdAndEmpresaId(gastoGeneralId, empresaId);
    }

    public StockGastoGeneral crearStock(Long gastoGeneralId, BigDecimal cantidadInicial, BigDecimal cantidadMinima,
                                        BigDecimal precioUnitario,
                                        Long empresaId) {
        // Verificar que el gasto general existe
        gastoGeneralRepository.findByIdAndEmpresaId(gastoGeneralId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("GastoGeneral", gastoGeneralId));

        // Verificar que no existe stock previo
        stockGastoGeneralRepository.findByGastoGeneralIdAndEmpresaId(gastoGeneralId, empresaId)
                .ifPresent(s -> {
                    throw new IllegalStateException("Ya existe stock para este gasto general");
                });

        StockGastoGeneral nuevoStock = new StockGastoGeneral(gastoGeneralId, cantidadInicial,
                cantidadMinima, precioUnitario, empresaId);

        return stockGastoGeneralRepository.save(nuevoStock);
    }
}