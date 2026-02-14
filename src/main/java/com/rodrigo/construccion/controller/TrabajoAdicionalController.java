package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.*;
import com.rodrigo.construccion.service.TrabajoAdicionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar trabajos adicionales
 */
@Tag(name = "Trabajos Adicionales", description = "Gestión de trabajos adicionales asociados a obras y trabajos extra")
@RestController
@RequestMapping("/api/trabajos-adicionales")
@RequiredArgsConstructor
@Slf4j
public class TrabajoAdicionalController {

    private final TrabajoAdicionalService trabajoAdicionalService;

    /**
     * Crear un nuevo trabajo adicional
     */
    @Operation(summary = "Crear nuevo trabajo adicional", 
               description = "Crea un trabajo adicional asociado a una obra o trabajo extra")
    @PostMapping
    public ResponseEntity<TrabajoAdicionalResponseDTO> crear(
            @Valid @RequestBody TrabajoAdicionalRequestDTO requestDTO) {
        
        log.info("POST /api/trabajos-adicionales - Creando trabajo adicional: {}", requestDTO.getNombre());
        log.info("DTO Recibido - obraId: {}, trabajoExtraId: {}, empresaId: {}", 
                 requestDTO.getObraId(), requestDTO.getTrabajoExtraId(), requestDTO.getEmpresaId());
        log.info("DTO Recibido - Profesionales: {}", requestDTO.getProfesionales().size());
        
        TrabajoAdicionalResponseDTO response = trabajoAdicionalService.crear(requestDTO);
        
        log.info("Trabajo adicional creado con ID: {} - obraId: {}, trabajoExtraId: {}", 
                 response.getId(), response.getObraId(), response.getTrabajoExtraId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener todos los trabajos adicionales con filtros opcionales
     */
    @Operation(summary = "Listar trabajos adicionales", 
               description = "Obtiene todos los trabajos adicionales de una empresa, con filtros opcionales por obra o trabajo extra")
    @GetMapping
    public ResponseEntity<List<TrabajoAdicionalResponseDTO>> obtenerTodos(
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId,
            
            @Parameter(description = "ID de la obra (opcional)")
            @RequestParam(required = false) Long obraId,
            
            @Parameter(description = "ID del trabajo extra (opcional)")
            @RequestParam(required = false) Long trabajoExtraId) {
        
        log.info("GET /api/trabajos-adicionales - empresaId: {}, obraId: {}, trabajoExtraId: {}", 
                 empresaId, obraId, trabajoExtraId);
        
        List<TrabajoAdicionalResponseDTO> response = trabajoAdicionalService.obtenerTodos(
                empresaId, obraId, trabajoExtraId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener un trabajo adicional por ID
     */
    @Operation(summary = "Obtener trabajo adicional por ID", 
               description = "Obtiene el detalle completo de un trabajo adicional específico")
    @GetMapping("/{id}")
    public ResponseEntity<TrabajoAdicionalResponseDTO> obtenerPorId(
            @Parameter(description = "ID del trabajo adicional", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        
        log.info("GET /api/trabajos-adicionales/{} - empresaId: {}", id, empresaId);
        TrabajoAdicionalResponseDTO response = trabajoAdicionalService.obtenerPorId(id, empresaId);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un trabajo adicional existente
     */
    @Operation(summary = "Actualizar trabajo adicional", 
               description = "Actualiza todos los datos de un trabajo adicional, incluyendo profesionales asignados")
    @PutMapping("/{id}")
    public ResponseEntity<TrabajoAdicionalResponseDTO> actualizar(
            @Parameter(description = "ID del trabajo adicional", required = true)
            @PathVariable Long id,
            
            @Valid @RequestBody TrabajoAdicionalRequestDTO requestDTO,
            
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        
        log.info("PUT /api/trabajos-adicionales/{} - Actualizando trabajo adicional", id);
        TrabajoAdicionalResponseDTO response = trabajoAdicionalService.actualizar(id, requestDTO, empresaId);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un trabajo adicional
     */
    @Operation(summary = "Eliminar trabajo adicional", 
               description = "Elimina un trabajo adicional y todos sus profesionales asociados")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del trabajo adicional", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        
        log.info("DELETE /api/trabajos-adicionales/{} - empresaId: {}", id, empresaId);
        trabajoAdicionalService.eliminar(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualizar solo el estado de un trabajo adicional
     */
    @Operation(summary = "Actualizar estado del trabajo adicional", 
               description = "Cambia el estado del trabajo adicional: PENDIENTE, EN_PROGRESO, COMPLETADO, CANCELADO")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TrabajoAdicionalResponseDTO> actualizarEstado(
            @Parameter(description = "ID del trabajo adicional", required = true)
            @PathVariable Long id,
            
            @Valid @RequestBody ActualizarEstadoTrabajoAdicionalDTO dto,
            
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        
        log.info("PATCH /api/trabajos-adicionales/{}/estado - Nuevo estado: {}", id, dto.getEstado());
        TrabajoAdicionalResponseDTO response = trabajoAdicionalService.actualizarEstado(
                id, dto.getEstado(), empresaId);
        return ResponseEntity.ok(response);
    }
}
