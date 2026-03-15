package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.AsignacionSemanalRequestDTO;
import com.rodrigo.construccion.dto.response.AsignacionSemanalCreacionResponseDTO;
import com.rodrigo.construccion.dto.response.AsignacionSemanalResponseDTO;
import com.rodrigo.construccion.exception.BusinessException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.service.AsignacionSemanalService;
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
 * Controller para gestión de asignaciones semanales de profesionales
 */
@RestController
@RequestMapping("/api/profesionales")
@RequiredArgsConstructor
@Slf4j
public class AsignacionSemanalController {

    private final AsignacionSemanalService asignacionSemanalService;

    /**
     * POST /api/profesionales/asignar-semanal
     * Crear asignación semanal (modalidad "total" o "semanal")
     */
    @PostMapping("/asignar-semanal")
    public ResponseEntity<AsignacionSemanalCreacionResponseDTO> crearAsignacionSemanal(
            @Valid @RequestBody AsignacionSemanalRequestDTO request,
            @RequestHeader("empresaId") Long empresaId) {

        log.info("POST /api/profesionales/asignar-semanal - Obra: {}, Modalidad: {}", 
                request.getObraId(), request.getModalidad());

        AsignacionSemanalCreacionResponseDTO response = asignacionSemanalService
                .crearAsignacionSemanal(request, empresaId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/profesionales/asignar-semanal/gestion-completa
     * Reemplazar todas las asignaciones de una obra (UPSERT masivo)
     * Elimina todo lo existente y crea lo nuevo en una sola transacción.
     */
    @PutMapping("/asignar-semanal/gestion-completa")
    public ResponseEntity<AsignacionSemanalCreacionResponseDTO> reemplazarAsignacionSemanal(
            @Valid @RequestBody AsignacionSemanalRequestDTO request,
            @RequestHeader("empresaId") Long empresaId) {

        log.info("PUT /api/profesionales/asignar-semanal/gestion-completa - Obra: {}", request.getObraId());

        AsignacionSemanalCreacionResponseDTO response = asignacionSemanalService
                .reemplazarAsignacionSemanal(request, empresaId);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/profesionales/asignaciones/{obraId}
     * Obtener asignaciones semanales de una obra
     */
    @GetMapping("/asignaciones/{obraId}")
    public ResponseEntity<Map<String, Object>> obtenerAsignacionesPorObra(
            @PathVariable Long obraId,
            @RequestHeader("empresaId") Long empresaId) {

        log.info("GET /api/profesionales/asignaciones/{} - Empresa: {}", obraId, empresaId);

        List<AsignacionSemanalResponseDTO> asignaciones = asignacionSemanalService
                .obtenerAsignacionesPorObra(obraId, empresaId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", asignaciones);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/profesionales/asignar-semanal/{asignacionId}
     * Eliminar asignación semanal (soft delete)
     */
    @DeleteMapping("/asignar-semanal/{asignacionId}")
    public ResponseEntity<AsignacionSemanalCreacionResponseDTO> eliminarAsignacionSemanal(
            @PathVariable Long asignacionId,
            @RequestHeader("empresaId") Long empresaId) {

        log.info("DELETE /api/profesionales/asignar-semanal/{} - Empresa: {}", asignacionId, empresaId);

        try {
            AsignacionSemanalCreacionResponseDTO response = asignacionSemanalService
                    .eliminarAsignacion(asignacionId, empresaId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("Asignación no encontrada: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AsignacionSemanalCreacionResponseDTO.builder()
                    .success(false)
                    .message("Asignación no encontrada: " + e.getMessage())
                    .build()
            );
        } catch (BusinessException e) {
            log.error("Error de negocio al eliminar asignación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                AsignacionSemanalCreacionResponseDTO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("Error interno al eliminar asignación {}", asignacionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AsignacionSemanalCreacionResponseDTO.builder()
                    .success(false)
                    .message("Error interno: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * PUT /api/profesionales/asignar-semanal/{asignacionId}
     * Actualizar asignación semanal
     */
    @PutMapping("/asignar-semanal/{asignacionId}")
    public ResponseEntity<AsignacionSemanalCreacionResponseDTO> actualizarAsignacionSemanal(
            @PathVariable Long asignacionId,
            @Valid @RequestBody AsignacionSemanalRequestDTO request,
            @RequestHeader("empresaId") Long empresaId) {

        log.info("PUT /api/profesionales/asignar-semanal/{} - Obra: {}", asignacionId, request.getObraId());

        try {
            AsignacionSemanalCreacionResponseDTO response = asignacionSemanalService
                    .actualizarAsignacion(asignacionId, request, empresaId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AsignacionSemanalCreacionResponseDTO.builder()
                    .success(false)
                    .message("Asignación no encontrada: " + e.getMessage())
                    .build()
            );
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                AsignacionSemanalCreacionResponseDTO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AsignacionSemanalCreacionResponseDTO.builder()
                    .success(false)
                    .message("Error interno del servidor")
                    .build()
            );
        }
    }

    /**
     * DELETE /api/profesionales/asignaciones/obra/{obraId}
     * Eliminar todas las asignaciones de una obra
     */
    @DeleteMapping("/asignaciones/obra/{obraId}")
    public ResponseEntity<AsignacionSemanalCreacionResponseDTO> eliminarAsignacionesPorObra(
            @PathVariable Long obraId,
            @RequestHeader("empresaId") Long empresaId) {

        log.info("DELETE /api/profesionales/asignaciones/obra/{} - Empresa: {}", obraId, empresaId);

        try {
            AsignacionSemanalCreacionResponseDTO response = asignacionSemanalService
                    .eliminarAsignacionesPorObra(obraId, empresaId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                AsignacionSemanalCreacionResponseDTO.builder()
                    .success(false)
                    .message("Obra no encontrada: " + e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                AsignacionSemanalCreacionResponseDTO.builder()
                    .success(false)
                    .message("Error interno del servidor")
                    .build()
            );
        }
    }
}
