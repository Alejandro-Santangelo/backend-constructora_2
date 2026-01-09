package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.AsignarCajaChicaMultipleRequest;
import com.rodrigo.construccion.dto.response.CajaChicaObraResponseDTO;
import com.rodrigo.construccion.service.ICajaChicaObraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/caja-chica-obra")
@RequiredArgsConstructor
@Tag(name = "Caja Chica Obra", description = "Gestión de caja chica asignada a profesionales por obra")
public class CajaChicaObraController {

    private final ICajaChicaObraService cajaChicaObraService;

    /**
     * POST /api/v1/caja-chica-obra/asignar-multiple
     * Asignar caja chica a múltiples profesionales
     */
    @PostMapping("/asignar-multiple")
    @Operation(summary = "Asignar caja chica a múltiples profesionales", 
               description = "Crea múltiples registros de caja chica, uno por cada profesional en el array")
    public ResponseEntity<List<CajaChicaObraResponseDTO>> asignarCajaChicaMultiple(
            @RequestParam Long empresaId,
            @Valid @RequestBody AsignarCajaChicaMultipleRequest request) {
        request.setEmpresaId(empresaId);
        List<CajaChicaObraResponseDTO> response = cajaChicaObraService.asignarCajaChicaMultiple(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/caja-chica-obra/obra/{presupuestoNoClienteId}
     * Obtener todas las asignaciones de caja chica de una obra
     */
    @GetMapping("/obra/{presupuestoNoClienteId}")
    @Operation(summary = "Obtener caja chica por obra", 
               description = "Retorna todas las asignaciones de caja chica de un presupuesto")
    public ResponseEntity<List<CajaChicaObraResponseDTO>> obtenerPorObra(
            @PathVariable Long presupuestoNoClienteId,
            @RequestParam Long empresaId) {
        List<CajaChicaObraResponseDTO> response = cajaChicaObraService
                .obtenerPorObra(empresaId, presupuestoNoClienteId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/caja-chica-obra/profesional/{profesionalObraId}
     * Obtener todas las asignaciones de caja chica de un profesional
     */
    @GetMapping("/profesional/{profesionalObraId}")
    @Operation(summary = "Obtener caja chica por profesional", 
               description = "Retorna todas las asignaciones de caja chica de un profesional")
    public ResponseEntity<List<CajaChicaObraResponseDTO>> obtenerPorProfesional(
            @PathVariable Long profesionalObraId,
            @RequestParam Long empresaId) {
        List<CajaChicaObraResponseDTO> response = cajaChicaObraService
                .obtenerPorProfesional(empresaId, profesionalObraId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/caja-chica-obra/{id}
     * Obtener una asignación de caja chica por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener caja chica por ID")
    public ResponseEntity<CajaChicaObraResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        CajaChicaObraResponseDTO response = cajaChicaObraService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/caja-chica-obra/{id}/rendir
     * Marcar caja chica como rendida
     */
    @PatchMapping("/{id}/rendir")
    @Operation(summary = "Rendir caja chica", 
               description = "Cambia el estado de la caja chica a RENDIDO")
    public ResponseEntity<CajaChicaObraResponseDTO> rendir(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        CajaChicaObraResponseDTO response = cajaChicaObraService.rendir(empresaId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/caja-chica-obra/{id}/anular
     * Anular caja chica
     */
    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular caja chica", 
               description = "Cambia el estado de la caja chica a ANULADO")
    public ResponseEntity<Void> anular(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        cajaChicaObraService.anular(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
