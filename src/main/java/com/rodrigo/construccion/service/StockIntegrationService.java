package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio para integrar automáticamente stock con asignaciones de materiales
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockIntegrationService {

    private final StockMaterialService stockMaterialService;
    private final ObraMaterialRepository obraMaterialRepository;
    private final MovimientoMaterialRepository movimientoMaterialRepository;
    private final MaterialCalculadoraRepository materialCalculadoraRepository;

    /**
     * Se ejecuta automáticamente cuando se asigna un material a una obra
     */
    @Transactional
    public void procesarAsignacionMaterial(Long obraMaterialId) {
        log.info("🔄 Procesando asignación de material ID: {}", obraMaterialId);
        
        try {
            // Buscar la asignación
            ObraMaterial asignacion = obraMaterialRepository.findById(obraMaterialId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));
            
            // Buscar el material calculadora asociado
            MaterialCalculadora materialCalculadora = materialCalculadoraRepository
                .findById(asignacion.getMaterialCalculadoraId())
                .orElseThrow(() -> new RuntimeException("Material calculadora no encontrado"));
            
            // Registrar movimiento de salida del almacén
            registrarMovimientoSalida(asignacion, materialCalculadora);
            
            log.info("✅ Asignación procesada correctamente");
            
        } catch (Exception e) {
            log.error("❌ Error procesando asignación: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Registrar movimiento de material desde almacén hacia obra
     */
    private void registrarMovimientoSalida(ObraMaterial asignacion, MaterialCalculadora material) {
        log.info("📦 Registrando salida de material: {} cantidad: {}", 
                 material.getNombre(), asignacion.getCantidadAsignada());
        
        // Actualizar stock
        stockMaterialService.registrarMovimiento(
            null, // Se puede mejorar para encontrar el stock correcto
            "SALIDA",
            asignacion.getCantidadAsignada().doubleValue(),
            "Asignación a obra ID: " + asignacion.getObraId(),
            asignacion.getObraId()
        );
    }

    /**
     * Se ejecuta cuando se cancela una asignación
     */
    @Transactional 
    public void procesarCancelacionAsignacion(Long obraMaterialId) {
        log.info("↩️ Procesando cancelación de asignación ID: {}", obraMaterialId);
        
        // Buscar asignación antes de eliminarla
        ObraMaterial asignacion = obraMaterialRepository.findById(obraMaterialId)
            .orElse(null);
            
        if (asignacion != null) {
            // Registrar movimiento de retorno al almacén
            stockMaterialService.registrarMovimiento(
                null,
                "ENTRADA", 
                asignacion.getCantidadAsignada().doubleValue(),
                "Cancelación de asignación",
                asignacion.getObraId()
            );
        }
    }

    /**
     * Inicializar stock automáticamente cuando un presupuesto es aprobado
     */
    @Transactional
    public void inicializarStockPresupuestoAprobado(Long presupuestoId, Long empresaId) {
        log.info("🏗️ Inicializando stock para presupuesto aprobado: {}", presupuestoId);
        
        // TODO: Implementar lógica para crear StockMaterial automáticamente
        // desde los MaterialCalculadora del presupuesto
        
        stockMaterialService.inicializarStockDesdePresupuesto(presupuestoId, empresaId, "ALMACEN_GENERAL");
    }
}