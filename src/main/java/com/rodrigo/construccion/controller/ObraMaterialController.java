package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.AsignarMaterialRequestDTO;
import com.rodrigo.construccion.dto.response.ObraMaterialResponseDTO;
import com.rodrigo.construccion.service.IObraMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar asignación de materiales a obras
 */
@RestController
@RequestMapping("/api/obras/{obraId}/materiales")
@RequiredArgsConstructor
@Tag(name = "Obra - Materiales", description = "Gestión de materiales asignados a obras")
public class ObraMaterialController {

    private final IObraMaterialService obraMaterialService;

    @PostMapping
    @Operation(summary = "Asignar material a obra", 
               description = "Asigna un material del presupuesto a una obra específica. " +
                             "Requiere empresaId en el header. " +
                             "No se permite asignar el mismo material dos veces a la misma obra.")
    public ResponseEntity<ObraMaterialResponseDTO> asignarMaterial(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "Datos del material a asignar", required = true)
            @Valid @RequestBody AsignarMaterialRequestDTO request) {
        
        // Asegurar que el obraId del path coincida con el del request
        request.setObraId(obraId);
        
        ObraMaterialResponseDTO response = obraMaterialService.asignar(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar materiales de una obra", 
               description = "Obtiene todos los materiales asignados a una obra específica. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<List<ObraMaterialResponseDTO>> listarMateriales(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId) {
        
        List<ObraMaterialResponseDTO> materiales = obraMaterialService.obtenerPorObra(empresaId, obraId);
        return ResponseEntity.ok(materiales);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener material asignado por ID", 
               description = "Obtiene los detalles de un material asignado específico. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<ObraMaterialResponseDTO> obtenerMaterial(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la asignación de material", required = true)
            @PathVariable Long id) {
        
        ObraMaterialResponseDTO material = obraMaterialService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(material);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar material asignado", 
               description = "Actualiza la cantidad asignada y observaciones de un material. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<ObraMaterialResponseDTO> actualizarMaterial(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la asignación de material", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del material", required = true)
            @Valid @RequestBody AsignarMaterialRequestDTO request) {
        
        // Asegurar que el obraId del path coincida con el del request
        request.setObraId(obraId);
        
        ObraMaterialResponseDTO response = obraMaterialService.actualizar(empresaId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar material asignado", 
               description = "Elimina la asignación de un material a una obra. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<Void> eliminarMaterial(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la asignación de material", required = true)
            @PathVariable Long id) {
        
        obraMaterialService.eliminar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
