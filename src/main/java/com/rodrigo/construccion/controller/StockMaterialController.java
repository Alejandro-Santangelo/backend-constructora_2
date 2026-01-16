package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.response.StockEstadisticasResponse;
import com.rodrigo.construccion.model.entity.StockMaterial;
import com.rodrigo.construccion.service.IStockMaterialService;
import com.rodrigo.construccion.service.StockMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/* NINGUNO DE ESTOS MÉTODOS SE ESTÁ USANDO EN EL FRONTEND, SINO QUE SE USA A TRAVÉS DEL CONTROLADOR DE MATERIAL */
@RestController
@RequestMapping("/stock-materiales")
public class StockMaterialController {

    @Autowired
    private IStockMaterialService stockMaterialService;

    @GetMapping
    public ResponseEntity<List<StockMaterial>> obtenerTodoStock() {
        return ResponseEntity.ok(stockMaterialService.obtenerTodoStock());
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<StockMaterial>> obtenerStockPaginado(@Parameter(description = "Parámetros de paginación") Pageable pageable) {
        return ResponseEntity.ok(stockMaterialService.obtenerStockPaginado(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMaterial> obtenerStockPorId(@Parameter(description = "ID del stock") @PathVariable Long id) {
        return ResponseEntity.ok(stockMaterialService.obtenerPorId(id));
    }

    @GetMapping("/buscar/ubicacion")
    public ResponseEntity<List<StockMaterial>> buscarPorUbicacion(@Parameter(description = "ID de la empresa")
                                                                  @RequestParam Long empresaId,
                                                                  @Parameter(description = "Ubicación a buscar") @RequestParam String ubicacion) {
        return ResponseEntity.ok(stockMaterialService.buscarPorUbicacion(empresaId, ubicacion));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<StockMaterial>> obtenerStockBajo(@Parameter(description = "ID de la empresa")
                                                                @RequestParam Long empresaId) {
        return ResponseEntity.ok(stockMaterialService.obtenerStockBajo(empresaId));
    }

    @GetMapping("/agotado")
    public ResponseEntity<List<StockMaterial>> obtenerStockAgotado(@Parameter(description = "ID de la empresa")
                                                                   @RequestParam Long empresaId) {
        return ResponseEntity.ok(stockMaterialService.obtenerStockAgotado(empresaId));
    }

    @GetMapping("/proximo-vencer")
    public ResponseEntity<List<StockMaterial>> obtenerStockProximoAVencer(@Parameter(description = "ID de la empresa")
                                                                          @RequestParam Long empresaId,
                                                                          @Parameter(description = "Días de antelación")
                                                                          @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(stockMaterialService.obtenerStockProximoAVencer(empresaId, dias));
    }

    @GetMapping("/material/{materialId}")
    public ResponseEntity<List<StockMaterial>> obtenerPorMaterial(@Parameter(description = "ID del material")
                                                                  @PathVariable Long materialId,
                                                                  @Parameter(description = "ID de la empresa")
                                                                  @RequestParam Long empresaId) {
        return ResponseEntity.ok(stockMaterialService.obtenerPorMaterial(materialId, empresaId));
    }

    @PostMapping
    public ResponseEntity<StockMaterial> crearStock(@Valid @RequestBody StockMaterial stock) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockMaterialService.crear(stock));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockMaterial> actualizarStock(@Parameter(description = "ID del stock") @PathVariable Long id,
                                                         @Valid @RequestBody StockMaterial stock) {
        return ResponseEntity.ok(stockMaterialService.actualizar(id, stock));
    }

    @PatchMapping("/{id}/ajustar-cantidad")
    public ResponseEntity<StockMaterial> ajustarCantidad(@Parameter(description = "ID del stock") @PathVariable Long id,
                                                         @Parameter(description = "Nueva cantidad") @RequestParam Double cantidad,
                                                         @Parameter(description = "Motivo del ajuste") @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(stockMaterialService.ajustarCantidad(id, cantidad, motivo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarStock(@Parameter(description = "ID del stock") @PathVariable Long id) {
        stockMaterialService.eliminar(id);
        return ResponseEntity.ok().body("Stock eliminado correctamente.");
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<StockEstadisticasResponse> obtenerEstadisticas(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(stockMaterialService.obtenerEstadisticas(empresaId));
    }

    @GetMapping("/ubicaciones")
    public ResponseEntity<List<String>> obtenerUbicaciones() {
        return ResponseEntity.ok(stockMaterialService.obtenerUbicaciones());
    }

    @GetMapping("/estados")
    public ResponseEntity<List<String>> obtenerEstados() {
        return ResponseEntity.ok(stockMaterialService.obtenerEstados());
    }
}