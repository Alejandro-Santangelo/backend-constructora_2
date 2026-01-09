package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.model.entity.StockMaterial;
import com.rodrigo.construccion.service.StockMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller para gestión de stock de materiales
 * 
 * Este controller maneja el inventario y control de stock,
 * incluyendo alertas de stock bajo y próximo a vencer.
 */
@Tag(name = "Stock Material", description = "Gestión de inventario y stock de materiales")
@RestController
@RequestMapping("/stock-materiales")
public class StockMaterialController {

    // Logger eliminado
    
    @Autowired
    private StockMaterialService stockMaterialService;

    @Operation(summary = "Obtener todo el stock activo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de stock obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<StockMaterial>> obtenerTodoStock() {
        List<StockMaterial> stock = stockMaterialService.obtenerTodoStock();
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Obtener stock con paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de stock obtenida exitosamente")
    })
    @GetMapping("/paginado")
    public ResponseEntity<Page<StockMaterial>> obtenerStockPaginado(
        @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<StockMaterial> stock = stockMaterialService.obtenerStockPaginado(pageable);
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Obtener stock por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock encontrado"),
        @ApiResponse(responseCode = "404", description = "Stock no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StockMaterial> obtenerStockPorId(
        @Parameter(description = "ID del stock") @PathVariable Long id) {
        StockMaterial stock = stockMaterialService.obtenerPorId(id);
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Buscar stock por ubicación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock encontrado")
    })
    @GetMapping("/buscar/ubicacion")
    public ResponseEntity<List<StockMaterial>> buscarPorUbicacion(
        @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
        @Parameter(description = "Ubicación a buscar") @RequestParam String ubicacion) {
        List<StockMaterial> stock = stockMaterialService.buscarPorUbicacion(empresaId, ubicacion);
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Obtener stock con cantidad baja")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock bajo obtenido exitosamente")
    })
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<StockMaterial>> obtenerStockBajo() {
        List<StockMaterial> stock = stockMaterialService.obtenerStockBajo();
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Obtener stock agotado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock agotado obtenido exitosamente")
    })
    @GetMapping("/agotado")
    public ResponseEntity<List<StockMaterial>> obtenerStockAgotado(
        @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        List<StockMaterial> stock = stockMaterialService.obtenerStockAgotado(empresaId);
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Obtener stock próximo a vencer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock próximo a vencer obtenido exitosamente")
    })
    @GetMapping("/proximo-vencer")
    public ResponseEntity<List<StockMaterial>> obtenerStockProximoAVencer(
        @Parameter(description = "Días de antelación") @RequestParam(defaultValue = "30") int dias) {
        List<StockMaterial> stock = stockMaterialService.obtenerStockProximoAVencer(dias);
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Obtener stock por material")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock encontrado")
    })
    @GetMapping("/material/{materialId}")
    public ResponseEntity<List<StockMaterial>> obtenerPorMaterial(
        @Parameter(description = "ID del material") @PathVariable Long materialId) {
        List<StockMaterial> stock = stockMaterialService.obtenerPorMaterial(materialId);
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Crear nuevo registro de stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Stock creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación")
    })
    @PostMapping
    public ResponseEntity<StockMaterial> crearStock(@RequestBody StockMaterial stock) {
        StockMaterial nuevoStock = stockMaterialService.crear(stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoStock);
    }

    @Operation(summary = "Actualizar stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Stock no encontrado"),
        @ApiResponse(responseCode = "400", description = "Error de validación")
    })
    @PutMapping("/{id}")
    public ResponseEntity<StockMaterial> actualizarStock(
        @Parameter(description = "ID del stock") @PathVariable Long id,
        @RequestBody StockMaterial stock) {
        StockMaterial stockActualizado = stockMaterialService.actualizar(id, stock);
        return ResponseEntity.ok(stockActualizado);
    }

    @Operation(summary = "Ajustar cantidad de stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cantidad ajustada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Stock no encontrado")
    })
    @PatchMapping("/{id}/ajustar-cantidad")
    public ResponseEntity<StockMaterial> ajustarCantidad(
        @Parameter(description = "ID del stock") @PathVariable Long id,
        @Parameter(description = "Nueva cantidad") @RequestParam Double cantidad,
        @Parameter(description = "Motivo del ajuste") @RequestParam(required = false) String motivo) {
        StockMaterial stock = stockMaterialService.ajustarCantidad(id, cantidad, motivo);
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "Eliminar stock (desactivar)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Stock eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Stock no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarStock(
        @Parameter(description = "ID del stock") @PathVariable Long id) {
        stockMaterialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener estadísticas de stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    })
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = stockMaterialService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    @Operation(summary = "Obtener ubicaciones disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ubicaciones obtenidas exitosamente")
    })
    @GetMapping("/ubicaciones")
    public ResponseEntity<List<String>> obtenerUbicaciones() {
        List<String> ubicaciones = stockMaterialService.obtenerUbicaciones();
        return ResponseEntity.ok(ubicaciones);
    }

    @Operation(summary = "Obtener estados disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estados obtenidos exitosamente")
    })
    @GetMapping("/estados")
    public ResponseEntity<List<String>> obtenerEstados() {
        List<String> estados = stockMaterialService.obtenerEstados();
        return ResponseEntity.ok(estados);
    }
}