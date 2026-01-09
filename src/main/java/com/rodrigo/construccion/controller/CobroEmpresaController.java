package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.AnularCobroEmpresaRequestDTO;
import com.rodrigo.construccion.dto.request.AsignarCobroEmpresaRequestDTO;
import com.rodrigo.construccion.dto.request.CobroEmpresaRequestDTO;
import com.rodrigo.construccion.dto.response.AsignacionCobroEmpresaObraResponseDTO;
import com.rodrigo.construccion.dto.response.AsignarCobroEmpresaResponseDTO;
import com.rodrigo.construccion.dto.response.CobroEmpresaResponseDTO;
import com.rodrigo.construccion.dto.response.DistribucionCobroObraResponseDTO;
import com.rodrigo.construccion.dto.response.EliminarAsignacionResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenCobrosEmpresaResponseDTO;
import com.rodrigo.construccion.dto.response.SaldoDisponibleResponseDTO;
import com.rodrigo.construccion.service.CobroEmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Cobros a nivel Empresa
 * 
 * Endpoints:
 * - POST   /api/v1/cobros-empresa - Registrar cobro
 * - POST   /api/v1/cobros-empresa/{id}/asignar - Asignar a obras
 * - GET    /api/v1/cobros-empresa - Listar cobros
 * - GET    /api/v1/cobros-empresa/{id} - Detalle
 * - GET    /api/v1/cobros-empresa/saldo-disponible - Saldo disponible
 * - GET    /api/v1/cobros-empresa/resumen - Resumen
 * - DELETE /api/v1/cobros-empresa/{id} - Eliminar
 * - PATCH  /api/v1/cobros-empresa/{id}/anular - Anular
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cobros-empresa")
@RequiredArgsConstructor
public class CobroEmpresaController {

    private final CobroEmpresaService cobroEmpresaService;

    /**
     * POST /api/v1/cobros-empresa
     * Registrar un nuevo cobro a nivel empresa
     */
    @PostMapping
    public ResponseEntity<CobroEmpresaResponseDTO> crearCobroEmpresa(
            @RequestParam Long empresaId,
            @Valid @RequestBody CobroEmpresaRequestDTO request) {
        
        log.info("POST /api/v1/cobros-empresa - empresaId: {}", empresaId);
        request.setEmpresaId(empresaId);
        
        CobroEmpresaResponseDTO response = cobroEmpresaService.crearCobroEmpresa(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * POST /api/v1/cobros-empresa/{cobroEmpresaId}/asignar
     * Asignar cobro empresa a una o varias obras
     */
    @PostMapping("/{cobroEmpresaId}/asignar")
    public ResponseEntity<AsignarCobroEmpresaResponseDTO> asignarCobroAObras(
            @PathVariable Long cobroEmpresaId,
            @RequestParam Long empresaId,
            @Valid @RequestBody AsignarCobroEmpresaRequestDTO request) {
        
        log.info("POST /api/v1/cobros-empresa/{}/asignar - empresaId: {}", 
                 cobroEmpresaId, empresaId);
        
        AsignarCobroEmpresaResponseDTO response = 
            cobroEmpresaService.asignarCobroAObras(cobroEmpresaId, empresaId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/cobros-empresa
     * Listar cobros de empresa
     * 
     * Query params:
     * - empresaId (requerido)
     * - estado (opcional): DISPONIBLE, ASIGNADO_PARCIAL, ASIGNADO_TOTAL, ANULADO
     */
    @GetMapping
    public ResponseEntity<List<CobroEmpresaResponseDTO>> listarCobrosEmpresa(
            @RequestParam Long empresaId,
            @RequestParam(required = false) String estado) {
        
        log.info("GET /api/v1/cobros-empresa - empresaId: {}, estado: {}", 
                 empresaId, estado);
        
        List<CobroEmpresaResponseDTO> response = 
            cobroEmpresaService.listarCobrosEmpresa(empresaId, estado);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/cobros-empresa/{id}
     * Obtener detalle de un cobro empresa con sus asignaciones
     */
    @GetMapping("/{id}")
    public ResponseEntity<CobroEmpresaResponseDTO> obtenerDetalleCobroEmpresa(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        
        log.info("GET /api/v1/cobros-empresa/{} - empresaId: {}", id, empresaId);
        
        CobroEmpresaResponseDTO response = 
            cobroEmpresaService.obtenerDetalleCobroEmpresa(id, empresaId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/cobros-empresa/saldo-disponible
     * Obtener saldo disponible total de la empresa
     */
    @GetMapping("/saldo-disponible")
    public ResponseEntity<SaldoDisponibleResponseDTO> obtenerSaldoDisponible(
            @RequestParam Long empresaId) {
        
        log.info("GET /api/v1/cobros-empresa/saldo-disponible - empresaId: {}", empresaId);
        
        SaldoDisponibleResponseDTO response = 
            cobroEmpresaService.obtenerSaldoDisponible(empresaId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/cobros-empresa/resumen
     * Obtener resumen completo de cobros empresa
     */
    @GetMapping("/resumen")
    public ResponseEntity<ResumenCobrosEmpresaResponseDTO> obtenerResumen(
            @RequestParam Long empresaId) {
        
        log.info("GET /api/v1/cobros-empresa/resumen - empresaId: {}", empresaId);
        
        ResumenCobrosEmpresaResponseDTO response = 
            cobroEmpresaService.obtenerResumen(empresaId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/cobros-empresa/{id}
     * Eliminar cobro empresa (solo si no tiene asignaciones)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarCobroEmpresa(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        
        log.info("DELETE /api/v1/cobros-empresa/{} - empresaId: {}", id, empresaId);
        
        cobroEmpresaService.eliminarCobroEmpresa(id, empresaId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Cobro eliminado exitosamente");
        response.put("id", id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/cobros-empresa/{cobroEmpresaId}/asignaciones
     * Listar todas las asignaciones de un cobro empresa
     */
    @GetMapping("/{cobroEmpresaId}/asignaciones")
    public ResponseEntity<List<AsignacionCobroEmpresaObraResponseDTO>> listarAsignacionesCobroEmpresa(
            @PathVariable Long cobroEmpresaId,
            @RequestParam Long empresaId) {
        
        log.info("GET /api/v1/cobros-empresa/{}/asignaciones - empresaId: {}", cobroEmpresaId, empresaId);
        
        List<AsignacionCobroEmpresaObraResponseDTO> asignaciones = cobroEmpresaService
            .listarAsignacionesCobroEmpresa(cobroEmpresaId, empresaId);
        
        return ResponseEntity.ok(asignaciones);
    }

    /**
     * DELETE /api/v1/cobros-empresa/{cobroEmpresaId}/asignaciones/{asignacionId}
     * Eliminar una asignación individual de cobro empresa
     * Libera el monto asignado y actualiza el estado del cobro
     */
    @DeleteMapping("/{cobroEmpresaId}/asignaciones/{asignacionId}")
    public ResponseEntity<EliminarAsignacionResponseDTO> eliminarAsignacionCobroEmpresa(
            @PathVariable Long cobroEmpresaId,
            @PathVariable Long asignacionId,
            @RequestParam Long empresaId) {
        
        log.info("DELETE /api/v1/cobros-empresa/{}/asignaciones/{} - empresaId: {}", 
                 cobroEmpresaId, asignacionId, empresaId);
        
        EliminarAsignacionResponseDTO response = cobroEmpresaService
            .eliminarAsignacionCobroEmpresa(cobroEmpresaId, asignacionId, empresaId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/cobros-empresa/{id}/anular
     * Anular cobro empresa
     */
    @PatchMapping("/{id}/anular")
    public ResponseEntity<CobroEmpresaResponseDTO> anularCobroEmpresa(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @Valid @RequestBody AnularCobroEmpresaRequestDTO request) {
        
        log.info("PATCH /api/v1/cobros-empresa/{}/anular - empresaId: {}", id, empresaId);
        
        CobroEmpresaResponseDTO response = 
            cobroEmpresaService.anularCobroEmpresa(id, empresaId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/cobros-empresa/distribucion-por-obra
     * Obtener distribución consolidada de cobros empresa por obra
     */
    @GetMapping("/distribucion-por-obra")
    public ResponseEntity<List<DistribucionCobroObraResponseDTO>> obtenerDistribucionPorObra(
            @RequestParam Long empresaId) {
        
        log.info("GET /api/v1/cobros-empresa/distribucion-por-obra - empresaId: {}", empresaId);
        
        List<DistribucionCobroObraResponseDTO> distribucion = 
            cobroEmpresaService.obtenerDistribucionPorObra(empresaId);
        
        return ResponseEntity.ok(distribucion);
    }

    /**
     * Manejo de excepciones
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Error de validación: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        log.error("Error de estado: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("Error interno del servidor", ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error interno del servidor");
        error.put("detalle", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
