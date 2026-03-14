package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.response.GastoGeneralConStockResponseDTO;
import com.rodrigo.construccion.model.entity.GastoGeneral;
import com.rodrigo.construccion.service.PresupuestoNoClienteService;
import com.rodrigo.construccion.service.IGastoGeneralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Gastos Generales", description = "Gestión de catálogo de gastos generales")
public class GastoGeneralController {
    
    private static final Logger log = LoggerFactory.getLogger(GastoGeneralController.class);
    
    @Autowired
    private PresupuestoNoClienteService presupuestoService;
    
    @Autowired
    private IGastoGeneralService gastoGeneralService;
    
    // ==================== CRUD GASTOS GENERALES ====================
    
    @Operation(summary = "Crear gasto general")
    @PostMapping("/gastos-generales")
    public ResponseEntity<?> crearGastoGeneral(
            @RequestHeader("empresaId") Long empresaId,
            @RequestBody GastoGeneral gastoGeneral) {
        
        log.info("📝 POST /api/gastos-generales - Empresa: {}, Nombre: {}", empresaId, gastoGeneral.getNombre());
        
        try {
            GastoGeneral nuevo = gastoGeneralService.crear(empresaId, gastoGeneral);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
            
        } catch (Exception e) {
            log.error("❌ Error al crear gasto general: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al crear gasto general: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Listar gastos generales de la empresa")
    @GetMapping("/gastos-generales")
    public ResponseEntity<?> listarGastosGenerales(@RequestHeader("empresaId") Long empresaId) {
        
        log.info("🔍 GET /api/gastos-generales - Empresa: {}", empresaId);
        
        try {
            List<GastoGeneral> gastos = gastoGeneralService.listarPorEmpresa(empresaId);
            return ResponseEntity.ok(gastos);
            
        } catch (Exception e) {
            log.error("❌ Error al listar gastos generales: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al listar gastos generales: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Listar gastos generales por ID de empresa (Endpoint para catálogo)")
    @GetMapping("/gastos-generales/empresa/{empresaId}")
    public ResponseEntity<List<GastoGeneral>> listarGastosGeneralesPorPath(@PathVariable Long empresaId) {
        log.info("🔍 GET /api/gastos-generales/empresa/{}", empresaId);
        return ResponseEntity.ok(gastoGeneralService.listarPorEmpresa(empresaId));
    }

    
    @Operation(summary = "Obtener gasto general por ID")
    @GetMapping("/gastos-generales/{id}")
    public ResponseEntity<?> obtenerGastoGeneral(
            @RequestHeader("empresaId") Long empresaId,
            @PathVariable Long id) {
        
        log.info("🔍 GET /api/gastos-generales/{} - Empresa: {}", id, empresaId);
        
        try {
            GastoGeneral gasto = gastoGeneralService.obtenerPorId(empresaId, id);
            return ResponseEntity.ok(gasto);
            
        } catch (Exception e) {
            log.error("❌ Error al obtener gasto general: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(java.util.Map.of("error", "Gasto general no encontrado"));
        }
    }
    
    @Operation(summary = "Actualizar gasto general")
    @PutMapping("/gastos-generales/{id}")
    public ResponseEntity<?> actualizarGastoGeneral(
            @RequestHeader("empresaId") Long empresaId,
            @PathVariable Long id,
            @RequestBody GastoGeneral gastoGeneral) {
        
        log.info("🔄 PUT /api/gastos-generales/{} - Empresa: {}", id, empresaId);
        
        try {
            GastoGeneral actualizado = gastoGeneralService.actualizar(empresaId, id, gastoGeneral);
            return ResponseEntity.ok(actualizado);
            
        } catch (Exception e) {
            log.error("❌ Error al actualizar gasto general: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al actualizar gasto general: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Eliminar gasto general")
    @DeleteMapping("/gastos-generales/{id}")
    public ResponseEntity<?> eliminarGastoGeneral(
            @RequestHeader("empresaId") Long empresaId,
            @PathVariable Long id) {
        
        log.info("🗑️ DELETE /api/gastos-generales/{} - Empresa: {}", id, empresaId);
        
        try {
            gastoGeneralService.eliminar(empresaId, id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("❌ Error al eliminar gasto general: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al eliminar gasto general: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Listar gastos generales por categoría")
    @GetMapping("/gastos-generales/categoria/{categoria}")
    public ResponseEntity<?> listarPorCategoria(
            @RequestHeader("empresaId") Long empresaId,
            @PathVariable String categoria) {
        
        log.info("🔍 GET /api/gastos-generales/categoria/{} - Empresa: {}", categoria, empresaId);
        
        try {
            List<GastoGeneral> gastos = gastoGeneralService.listarPorCategoria(empresaId, categoria);
            return ResponseEntity.ok(gastos);
            
        } catch (Exception e) {
            log.error("❌ Error al listar gastos por categoría: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al listar gastos por categoría: " + e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar precio de todos los gastos generales")
    @PutMapping("/gastos-generales/actualizar-precio-todos")
    public ResponseEntity<?> actualizarPrecioTodos(
            @RequestHeader("empresaId") Long empresaId,
            @RequestParam("porcentaje") double porcentaje) {
        
        log.info("💰 PUT /api/gastos-generales/actualizar-precio-todos - Empresa: {}, Porcentaje: {}%", empresaId, porcentaje);
        
        try {
            gastoGeneralService.actualizarPrecioTodos(empresaId, porcentaje);
            return ResponseEntity.ok(java.util.Map.of("message", "Los precios de todos los gastos generales se han actualizado con éxito."));
            
        } catch (Exception e) {
            log.error("❌ Error al actualizar precios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al actualizar precios: " + e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar precio de un gasto general")
    @PutMapping("/gastos-generales/{id}/actualizar-precio")
    public ResponseEntity<?> actualizarPrecioPorId(
            @RequestHeader("empresaId") Long empresaId,
            @PathVariable Long id,
            @RequestParam("porcentaje") double porcentaje) {
        
        log.info("💰 PUT /api/gastos-generales/{}/actualizar-precio - Empresa: {}, Porcentaje: {}%", id, empresaId, porcentaje);
        
        try {
            gastoGeneralService.actualizarPrecioPorId(empresaId, id, porcentaje);
            return ResponseEntity.ok(java.util.Map.of("message", "El precio del gasto general se ha actualizado con éxito."));
            
        } catch (Exception e) {
            log.error("❌ Error al actualizar precio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al actualizar precio: " + e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar precio de varios gastos generales")
    @PutMapping("/gastos-generales/actualizar-precio-varios")
    public ResponseEntity<?> actualizarPrecioVarios(
            @RequestHeader("empresaId") Long empresaId,
            @RequestBody java.util.Map<String, Object> request) {
        
        log.info("💰 PUT /api/gastos-generales/actualizar-precio-varios - Empresa: {}", empresaId);
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) request.get("ids");
            double porcentaje = ((Number) request.get("porcentaje")).doubleValue();
            
            gastoGeneralService.actualizarPrecioVarios(empresaId, ids, porcentaje);
            return ResponseEntity.ok(java.util.Map.of("message", "Los precios de " + ids.size() + " gastos generales se han actualizado con éxito."));
            
        } catch (Exception e) {
            log.error("❌ Error al actualizar precios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of("error", "Error al actualizar precios: " + e.getMessage()));
        }
    }
    
    // ==================== ENDPOINT ORIGINAL ====================
    
    /**
     * Obtener gastos generales de un presupuesto con información de stock
     * GET /presupuestos-no-cliente/{id}/gastos-generales
     */
    @GetMapping("/presupuestos-no-cliente/{id}/gastos-generales")
    public ResponseEntity<List<GastoGeneralConStockResponseDTO>> obtenerGastosGeneralesConStock(
            @PathVariable Long id,
            @RequestHeader("empresaId") Long empresaId) {
        
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