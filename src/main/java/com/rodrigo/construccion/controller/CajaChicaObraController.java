package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.AsignarCajaChicaMultipleRequest;
import com.rodrigo.construccion.dto.response.CajaChicaObraResponseDTO;
import com.rodrigo.construccion.service.ICajaChicaObraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/caja-chica-obra")
@RequiredArgsConstructor
public class CajaChicaObraController {

    private final ICajaChicaObraService cajaChicaObraService;

    /* Asignar caja chica a múltiples profesionales de una obra */
    @PostMapping("/asignar-multiple")
    public ResponseEntity<List<CajaChicaObraResponseDTO>> asignarCajaChicaMultiple(@RequestParam Long empresaId,
                                                                                   @Valid @RequestBody AsignarCajaChicaMultipleRequest request) {
        request.setEmpresaId(empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaChicaObraService.asignarCajaChicaMultiple(request));
    }

    /* Obtener todas las asignaciones de caja chica de una obra */
    @GetMapping("/obra/{presupuestoNoClienteId}")
    public ResponseEntity<List<CajaChicaObraResponseDTO>> obtenerPorObra(@PathVariable Long presupuestoNoClienteId,
                                                                         @RequestParam Long empresaId) {
        return ResponseEntity.ok(cajaChicaObraService.obtenerPorObra(empresaId, presupuestoNoClienteId));
    }

    /* Obtener todas las asignaciones de caja chica de un profesional */
    @GetMapping("/profesional/{profesionalObraId}")
    public ResponseEntity<List<CajaChicaObraResponseDTO>> obtenerPorProfesional(@PathVariable Long profesionalObraId, @RequestParam Long empresaId) {
        return ResponseEntity.ok(cajaChicaObraService.obtenerPorProfesional(empresaId, profesionalObraId));
    }

    /* Obtener una asignación de caja chica por ID */
    @GetMapping("/{id}")
    public ResponseEntity<CajaChicaObraResponseDTO> obtenerPorId(@PathVariable Long id, @RequestParam Long empresaId) {
        return ResponseEntity.ok(cajaChicaObraService.obtenerPorId(empresaId, id));
    }

    /* Marcar caja chica como rendida */
    @PatchMapping("/{id}/rendir")
    public ResponseEntity<CajaChicaObraResponseDTO> rendir(@PathVariable Long id, @RequestParam Long empresaId) {
        return ResponseEntity.ok(cajaChicaObraService.rendir(empresaId, id));
    }

    /* Anular caja chica */
    @PatchMapping("/{id}/anular")
    public ResponseEntity<String> anular(@PathVariable Long id, @RequestParam Long empresaId) {
        cajaChicaObraService.anular(empresaId, id);
        return ResponseEntity.ok().body("La caja chica se anuló correctamente.");
    }
}
