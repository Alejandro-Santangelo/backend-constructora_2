package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.response.RubroResponseDTO;
import com.rodrigo.construccion.service.RubroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de Rubros
 */
@Tag(name = "Rubros", description = "Catálogo maestro de rubros de construcción")
@RestController
@RequestMapping("/api/rubros")
@RequiredArgsConstructor
@Slf4j
public class RubroController {

    private final RubroService rubroService;

    @GetMapping
    @Operation(summary = "Listar todos los rubros activos",
               description = "Obtiene el catálogo completo de rubros activos disponibles para usar en presupuestos")
    public ResponseEntity<List<RubroResponseDTO>> listarRubrosActivos() {
        log.info("GET /api/rubros - Listar rubros activos");
        List<RubroResponseDTO> rubros = rubroService.obtenerRubrosActivos();
        return ResponseEntity.ok(rubros);
    }

    @GetMapping("/todos")
    @Operation(summary = "Listar todos los rubros (incluidos inactivos)",
               description = "Obtiene todos los rubros del sistema, incluidos los marcados como inactivos")
    public ResponseEntity<List<RubroResponseDTO>> listarTodos() {
        log.info("GET /api/rubros/todos - Listar todos los rubros");
        List<RubroResponseDTO> rubros = rubroService.obtenerTodos();
        return ResponseEntity.ok(rubros);
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Listar rubros por categoría",
               description = "Filtra rubros activos por categoría (estructura, instalaciones, terminaciones, servicios)")
    public ResponseEntity<List<RubroResponseDTO>> listarPorCategoria(
            @Parameter(description = "Categoría del rubro") @PathVariable String categoria) {
        log.info("GET /api/rubros/categoria/{} - Listar por categoría", categoria);
        List<RubroResponseDTO> rubros = rubroService.obtenerPorCategoria(categoria);
        return ResponseEntity.ok(rubros);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar rubros por nombre",
               description = "Busca rubros que contengan el texto especificado en su nombre (case-insensitive)")
    public ResponseEntity<List<RubroResponseDTO>> buscar(
            @Parameter(description = "Texto a buscar en el nombre") @RequestParam String texto) {
        log.info("GET /api/rubros/buscar?texto={} - Buscar rubros", texto);
        List<RubroResponseDTO> rubros = rubroService.buscarPorNombre(texto);
        return ResponseEntity.ok(rubros);
    }
}
