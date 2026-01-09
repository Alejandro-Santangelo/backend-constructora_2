package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.model.entity.Material;
import com.rodrigo.construccion.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller para gestión de materiales (catálogo general)
 * 
 * Este controller maneja el catálogo general de materiales
 * que pueden ser utilizados por todas las empresas.
 */
@Tag(name = "Material", description = "Gestión de catálogo de materiales")
@RestController
@RequestMapping("/api/materiales")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @Operation(summary = "Obtener todos los materiales activos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de materiales obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Material>> obtenerTodosLosMateriales() {
        List<Material> materiales = materialService.obtenerTodosActivos();
        return ResponseEntity.ok(materiales);
    }

    @Operation(summary = "Obtener materiales con paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de materiales obtenida exitosamente")
    })
    @GetMapping("/paginados")
    public ResponseEntity<Page<Material>> obtenerMaterialesPaginados(
        @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<Material> materiales = materialService.obtenerMaterialesPaginados(pageable);
        return ResponseEntity.ok(materiales);
    }

    @Operation(summary = "Obtener material por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material encontrado"),
        @ApiResponse(responseCode = "404", description = "Material no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Material> obtenerMaterialPorId(
        @Parameter(description = "ID del material") @PathVariable Long id) {
        Material material = materialService.obtenerPorId(id);
        return ResponseEntity.ok(material);
    }

    @Operation(summary = "Buscar materiales por texto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Materiales encontrados")
    })
    @GetMapping("/buscar")
    public ResponseEntity<Page<Material>> buscarMateriales(
        @Parameter(description = "Texto a buscar") @RequestParam String texto,
        @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        Page<Material> materiales = materialService.buscarPorTexto(texto, pageable);
        return ResponseEntity.ok(materiales);
    }

    @Operation(summary = "Obtener materiales por unidad de medida")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Materiales encontrados")
    })
    @GetMapping("/unidad/{unidadMedida}")
    public ResponseEntity<List<Material>> obtenerPorUnidadMedida(
        @Parameter(description = "Unidad de medida") @PathVariable String unidadMedida) {
        List<Material> materiales = materialService.obtenerPorUnidadMedida(unidadMedida);
        return ResponseEntity.ok(materiales);
    }

    @Operation(summary = "Obtener materiales por rango de precio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Materiales encontrados")
    })
    @GetMapping("/precio")
    public ResponseEntity<List<Material>> obtenerPorRangoPrecio(
        @Parameter(description = "Precio mínimo") @RequestParam BigDecimal precioMin,
        @Parameter(description = "Precio máximo") @RequestParam BigDecimal precioMax) {
        List<Material> materiales = materialService.obtenerPorRangoPrecio(precioMin, precioMax);
        return ResponseEntity.ok(materiales);
    }

    @Operation(summary = "Crear nuevo material")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Material creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación")
    })
    @PostMapping
    public ResponseEntity<Material> crearMaterial(@RequestBody Material material) {
        Material nuevoMaterial = materialService.crear(material);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMaterial);
    }

    @Operation(summary = "Actualizar material")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Material actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Material no encontrado"),
        @ApiResponse(responseCode = "400", description = "Error de validación")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Material> actualizarMaterial(
        @Parameter(description = "ID del material") @PathVariable Long id,
        @RequestBody Material material) {
        Material materialActualizado = materialService.actualizar(id, material);
        return ResponseEntity.ok(materialActualizado);
    }

    @Operation(summary = "Eliminar material (desactivar)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Material eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Material no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMaterial(
        @Parameter(description = "ID del material") @PathVariable Long id) {
        materialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener estadísticas de materiales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente")
    })
    @GetMapping("/estadisticas")
    public ResponseEntity<Object> obtenerEstadisticas() {
        Object estadisticas = materialService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    @Operation(summary = "Obtener precio promedio de materiales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Precio promedio obtenido exitosamente")
    })
    @GetMapping("/precio-promedio")
    public ResponseEntity<BigDecimal> obtenerPrecioPromedio() {
        BigDecimal precioPromedio = materialService.obtenerPrecioPromedio();
        return ResponseEntity.ok(precioPromedio);
    }

    @Operation(summary = "Obtener materiales ordenados por nombre")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Materiales ordenados obtenidos exitosamente")
    })
    @GetMapping("/ordenados")
    public ResponseEntity<List<Material>> obtenerMaterialesOrdenados() {
        List<Material> materiales = materialService.obtenerTodosOrdenadosPorNombre();
        return ResponseEntity.ok(materiales);
    }
}