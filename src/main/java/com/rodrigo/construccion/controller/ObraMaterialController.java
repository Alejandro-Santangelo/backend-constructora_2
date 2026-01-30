package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.AsignarMaterialRequestDTO;
import com.rodrigo.construccion.dto.response.ObraMaterialResponseDTO;
import com.rodrigo.construccion.service.IObraMaterialService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/obras/{obraId}/materiales")
@RequiredArgsConstructor
public class ObraMaterialController {

    private final IObraMaterialService obraMaterialService;

    @PostMapping
    public ResponseEntity<ObraMaterialResponseDTO> asignarMaterial(@Parameter(description = "ID de la empresa (obligatorio)", required = true) @RequestHeader("empresaId") Long empresaId,
                                                                   @Parameter(description = "ID de la obra", required = true) @PathVariable Long obraId,
                                                                   @Parameter(description = "Datos del material a asignar", required = true) @Valid @RequestBody AsignarMaterialRequestDTO request) {

        // Asegurar que el obraId del path coincida con el del request
        request.setObraId(obraId);
        ObraMaterialResponseDTO response = obraMaterialService.asignar(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ObraMaterialResponseDTO>> listarMateriales(@Parameter(description = "ID de la empresa (obligatorio)", required = true) @RequestHeader("empresaId") Long empresaId,
                                                                          @Parameter(description = "ID de la obra", required = true) @PathVariable Long obraId) {
        List<ObraMaterialResponseDTO> materiales = obraMaterialService.obtenerPorObra(empresaId, obraId);
        return ResponseEntity.ok(materiales);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObraMaterialResponseDTO> obtenerMaterial(@Parameter(description = "ID de la empresa (obligatorio)", required = true)
                                                                   @RequestHeader("empresaId") Long empresaId,
                                                                   @Parameter(description = "ID de la asignación de material", required = true)
                                                                   @PathVariable Long id) {
        ObraMaterialResponseDTO material = obraMaterialService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(material);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObraMaterialResponseDTO> actualizarMaterial(@Parameter(description = "ID de la empresa (obligatorio)", required = true)
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
    public ResponseEntity<String> eliminarMaterial(@Parameter(description = "ID de la empresa (obligatorio)", required = true)
                                                   @RequestHeader("empresaId") Long empresaId,
                                                   @Parameter(description = "ID de la obra", required = true)
                                                   @PathVariable Long obraId,
                                                   @Parameter(description = "ID de la asignación de material", required = true)
                                                   @PathVariable Long id) {
        obraMaterialService.eliminar(empresaId, obraId, id);
        return ResponseEntity.ok("Material eliminado correctamente");
    }
}
