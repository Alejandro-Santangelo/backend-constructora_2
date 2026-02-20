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

    // === ENDPOINTS PARA BORRADORES DE TRABAJOS ADICIONALES ===

    @Operation(summary = "Crear trabajo adicional como borrador", 
               description = "Crea un trabajo adicional en estado BORRADOR que permite ir guardando datos por etapas. " +
                             "Todos los campos del formulario (incluido desglose de honorarios y descuentos) se persisten automáticamente.")
    @PostMapping("/borrador")
    public ResponseEntity<TrabajoAdicionalResponseDTO> crearBorrador(
            @Valid @RequestBody TrabajoAdicionalRequestDTO requestDTO) {
        
        log.info("POST /api/trabajos-adicionales/borrador - Creando borrador: {}", requestDTO.getNombre());
        TrabajoAdicionalResponseDTO borrador = trabajoAdicionalService.crearBorrador(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(borrador);
    }

    @Operation(summary = "Actualizar borrador de trabajo adicional",
               description = "Actualiza cualquier campo de un borrador de trabajo adicional. " +
                             "Solo funciona si el trabajo está en estado BORRADOR. Permite persistir cambios incrementales " +
                             "de todo el desglose de honorarios y descuentos.")
    @PutMapping("/borrador/{id}")
    public ResponseEntity<TrabajoAdicionalResponseDTO> actualizarBorrador(
            @Parameter(description = "ID del borrador de trabajo adicional") @PathVariable Long id,
            @Valid @RequestBody TrabajoAdicionalRequestDTO requestDTO) {
        
        log.info("PUT /api/trabajos-adicionales/borrador/{} - Actualizando borrador", id);
        TrabajoAdicionalResponseDTO trabajoActualizado = trabajoAdicionalService.actualizarBorrador(id, requestDTO);
        return ResponseEntity.ok(trabajoActualizado);
    }

    @Operation(summary = "Confirmar borrador como trabajo adicional activo",
               description = "Convierte un borrador en trabajo adicional activo, cambiando su estado de BORRADOR a PENDIENTE. " +
                             "Valida que tenga los datos mínimos requeridos.")
    @PostMapping("/borrador/{id}/confirmar")
    public ResponseEntity<TrabajoAdicionalResponseDTO> confirmarBorrador(
            @Parameter(description = "ID del borrador de trabajo adicional") @PathVariable Long id) {
        
        log.info("POST /api/trabajos-adicionales/borrador/{}/confirmar - Confirmando borrador", id);
        TrabajoAdicionalResponseDTO trabajoConfirmado = trabajoAdicionalService.confirmarBorrador(id);
        return ResponseEntity.ok(trabajoConfirmado);
    }

    @Operation(summary = "Listar borradores de trabajos adicionales",
               description = "Obtiene todos los trabajos adicionales en estado BORRADOR con filtros opcionales por obra o trabajo extra. " +
                             "Útil para mostrar lista de trabajos en progreso.")
    @GetMapping("/borradores")
    public ResponseEntity<List<TrabajoAdicionalResponseDTO>> obtenerBorradores(
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId,
            
            @Parameter(description = "ID de la obra (opcional)")
            @RequestParam(required = false) Long obraId,
            
            @Parameter(description = "ID del trabajo extra (opcional)")
            @RequestParam(required = false) Long trabajoExtraId) {
        
        log.info("GET /api/trabajos-adicionales/borradores - empresaId: {}, obraId: {}, trabajoExtraId: {}", 
                 empresaId, obraId, trabajoExtraId);
        
        List<TrabajoAdicionalResponseDTO> borradores = trabajoAdicionalService.obtenerBorradores(
                empresaId, obraId, trabajoExtraId);
        return ResponseEntity.ok(borradores);
    }
}
