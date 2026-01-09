package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.AsignacionCobroObraRequestDTO;
import com.rodrigo.construccion.dto.response.AsignacionCobroObraResponseDTO;
import com.rodrigo.construccion.service.IAsignacionCobroObraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/asignaciones-cobro-obra")
@RequiredArgsConstructor
public class AsignacionCobroObraController {

    private final IAsignacionCobroObraService asignacionService;

    /**
     * POST /api/v1/asignaciones-cobro-obra
     * Crear una nueva asignación de cobro a obra
     */
    @PostMapping
    public ResponseEntity<AsignacionCobroObraResponseDTO> crearAsignacion(
            @RequestParam Long empresaId,
            @Valid @RequestBody AsignacionCobroObraRequestDTO request) {
        request.setEmpresaId(empresaId);
        AsignacionCobroObraResponseDTO response = asignacionService.crearAsignacion(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/asignaciones-cobro-obra/{id}
     * Actualizar una asignación existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<AsignacionCobroObraResponseDTO> actualizarAsignacion(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @Valid @RequestBody AsignacionCobroObraRequestDTO request) {
        request.setEmpresaId(empresaId);
        AsignacionCobroObraResponseDTO response = asignacionService.actualizarAsignacion(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/asignaciones-cobro-obra/{id}
     * Eliminar una asignación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAsignacion(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        asignacionService.eliminarAsignacion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/asignaciones-cobro-obra/{id}
     * Obtener una asignación por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AsignacionCobroObraResponseDTO> obtenerAsignacionPorId(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        AsignacionCobroObraResponseDTO response = asignacionService.obtenerAsignacionPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/asignaciones-cobro-obra/cobro/{cobroId}
     * Obtener todas las asignaciones de un cobro
     */
    @GetMapping("/cobro/{cobroId}")
    public ResponseEntity<List<AsignacionCobroObraResponseDTO>> obtenerAsignacionesPorCobro(
            @PathVariable Long cobroId,
            @RequestParam Long empresaId) {
        List<AsignacionCobroObraResponseDTO> asignaciones = asignacionService.obtenerAsignacionesPorCobro(cobroId);
        return ResponseEntity.ok(asignaciones);
    }

    /**
     * GET /api/v1/asignaciones-cobro-obra/obra/{obraId}
     * Obtener todas las asignaciones de una obra
     */
    @GetMapping("/obra/{obraId}")
    public ResponseEntity<List<AsignacionCobroObraResponseDTO>> obtenerAsignacionesPorObra(
            @PathVariable Long obraId,
            @RequestParam Long empresaId) {
        List<AsignacionCobroObraResponseDTO> asignaciones = asignacionService.obtenerAsignacionesPorObra(obraId);
        return ResponseEntity.ok(asignaciones);
    }

    /**
     * GET /api/v1/asignaciones-cobro-obra/cobro/{cobroId}/activas
     * Obtener asignaciones activas de un cobro
     */
    @GetMapping("/cobro/{cobroId}/activas")
    public ResponseEntity<List<AsignacionCobroObraResponseDTO>> obtenerAsignacionesActivasPorCobro(
            @PathVariable Long cobroId,
            @RequestParam Long empresaId) {
        List<AsignacionCobroObraResponseDTO> asignaciones = asignacionService.obtenerAsignacionesActivasPorCobro(cobroId);
        return ResponseEntity.ok(asignaciones);
    }

    /**
     * GET /api/v1/asignaciones-cobro-obra/cobro/{cobroId}/total-asignado
     * Calcular total asignado de un cobro
     */
    @GetMapping("/cobro/{cobroId}/total-asignado")
    public ResponseEntity<BigDecimal> calcularTotalAsignadoPorCobro(
            @PathVariable Long cobroId,
            @RequestParam Long empresaId) {
        BigDecimal total = asignacionService.calcularTotalAsignadoPorCobro(cobroId);
        return ResponseEntity.ok(total);
    }

    /**
     * GET /api/v1/asignaciones-cobro-obra/obra/{obraId}/total-recibido
     * Calcular total recibido por una obra
     */
    @GetMapping("/obra/{obraId}/total-recibido")
    public ResponseEntity<BigDecimal> calcularTotalRecibidoPorObra(
            @PathVariable Long obraId,
            @RequestParam Long empresaId) {
        BigDecimal total = asignacionService.calcularTotalRecibidoPorObra(obraId);
        return ResponseEntity.ok(total);
    }

    /**
     * PATCH /api/v1/asignaciones-cobro-obra/{id}/anular
     * Anular una asignación
     */
    @PatchMapping("/{id}/anular")
    public ResponseEntity<AsignacionCobroObraResponseDTO> anularAsignacion(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        AsignacionCobroObraResponseDTO response = asignacionService.anularAsignacion(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/asignaciones-cobro-obra
     * Obtener todas las asignaciones de una empresa
     */
    @GetMapping
    public ResponseEntity<List<AsignacionCobroObraResponseDTO>> obtenerAsignacionesPorEmpresa(
            @RequestParam Long empresaId) {
        List<AsignacionCobroObraResponseDTO> asignaciones = asignacionService.obtenerAsignacionesPorEmpresa(empresaId);
        return ResponseEntity.ok(asignaciones);
    }
}
