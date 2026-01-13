package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.response.MaterialEstadisticaResponseDTO;
import com.rodrigo.construccion.model.entity.Material;
import com.rodrigo.construccion.service.MaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/materiales")
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "Obtener todos los materiales activos")
    @GetMapping
    public ResponseEntity<List<Material>> obtenerTodosLosMateriales() {
        return ResponseEntity.ok(materialService.obtenerTodosActivos());
    }

    @Operation(summary = "Obtener material por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Material> obtenerMaterialPorId(@Parameter(description = "ID del material") @PathVariable Long id) {
        return ResponseEntity.ok(materialService.obtenerPorId(id));
    }

    @Operation(summary = "Buscar materiales por texto")
    @GetMapping("/buscar")
    public ResponseEntity<Page<Material>> buscarMateriales(@Parameter(description = "Texto a buscar") @RequestParam String texto,
                                                           @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        return ResponseEntity.ok(materialService.buscarPorTexto(texto, pageable));
    }


    @Operation(summary = "Obtener materiales por rango de precio")
    @GetMapping("/precio")
    public ResponseEntity<List<Material>> obtenerPorRangoPrecio(@Parameter(description = "Precio mínimo") @RequestParam BigDecimal precioMin,
                                                                @Parameter(description = "Precio máximo") @RequestParam BigDecimal precioMax) {
        return ResponseEntity.ok(materialService.obtenerPorRangoPrecio(precioMin, precioMax));
    }

    @Operation(summary = "Crear nuevo material")
    @PostMapping
    public ResponseEntity<Material> crearMaterial(@Valid @RequestBody Material material) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.crear(material));
    }

    @Operation(summary = "Actualizar material")
    @PutMapping("/{id}")
    public ResponseEntity<Material> actualizarMaterial(@Parameter(description = "ID del material") @PathVariable Long id,
                                                       @Valid @RequestBody Material material) {
        return ResponseEntity.ok(materialService.actualizar(id, material));
    }

    @Operation(summary = "Eliminar material (desactivar)")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarMaterial(@Parameter(description = "ID del material") @PathVariable Long id) {
        materialService.eliminar(id);
        return ResponseEntity.ok("Material eliminado exitosamente");
    }

    /* MÉTODOS QUE NO ESTÁN SIENDO USADOS POR EL FRONTEND - PARA BORRAR */
    @Operation(summary = "Obtener materiales con paginación")
    @GetMapping("/paginados")
    public ResponseEntity<Page<Material>> obtenerMaterialesPaginados(
            @Parameter(description = "Parámetros de paginación") Pageable pageable) {
        return ResponseEntity.ok(materialService.obtenerMaterialesPaginados(pageable));
    }

    @Operation(summary = "Obtener materiales por unidad de medida")
    @GetMapping("/unidad/{unidadMedida}")
    public ResponseEntity<List<Material>> obtenerPorUnidadMedida(
            @Parameter(description = "Unidad de medida") @PathVariable String unidadMedida) {
        return ResponseEntity.ok(materialService.obtenerPorUnidadMedida(unidadMedida));
    }

    @Operation(summary = "Obtener estadísticas de materiales")
    @GetMapping("/estadisticas")
    public ResponseEntity<MaterialEstadisticaResponseDTO> obtenerEstadisticas() {
        return ResponseEntity.ok(materialService.obtenerEstadisticas());
    }

    @Operation(summary = "Obtener precio promedio de materiales")
    @GetMapping("/precio-promedio")
    public ResponseEntity<BigDecimal> obtenerPrecioPromedio() {
        return ResponseEntity.ok(materialService.obtenerPrecioPromedio());
    }

    @Operation(summary = "Obtener materiales ordenados por nombre")
    @GetMapping("/ordenados")
    public ResponseEntity<List<Material>> obtenerMaterialesOrdenados() {
        return ResponseEntity.ok(materialService.obtenerTodosOrdenadosPorNombre());
    }
}