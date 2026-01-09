package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.response.GastoGeneralConStockResponseDTO;
import com.rodrigo.construccion.service.PresupuestoNoClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/presupuestos-no-cliente")
@CrossOrigin(origins = "*")
public class GastoGeneralController {
    
    private static final Logger log = LoggerFactory.getLogger(GastoGeneralController.class);
    
    @Autowired
    private PresupuestoNoClienteService presupuestoService;
    
    /**
     * Obtener gastos generales de un presupuesto con información de stock
     * GET /presupuestos-no-cliente/{id}/gastos-generales
     */
    @GetMapping("/{id}/gastos-generales")
    public ResponseEntity<List<GastoGeneralConStockResponseDTO>> obtenerGastosGeneralesConStock(
            @PathVariable Long id,
            @RequestHeader Long empresaId) {
        
        log.info("🔍 GET /presupuestos-no-cliente/{}/gastos-generales - Empresa: {}", id, empresaId);
        
        try {
            List<GastoGeneralConStockResponseDTO> gastosGenerales = presupuestoService
                .obtenerGastosGeneralesPresupuesto(id, empresaId);
            
            log.info("✅ Obtenidos {} gastos generales para presupuesto {}", gastosGenerales.size(), id);
            
            return ResponseEntity.ok(gastosGenerales);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener gastos generales para presupuesto {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}