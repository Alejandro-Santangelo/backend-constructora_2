package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.EtapasDiariasRequestDTO;
import com.rodrigo.construccion.dto.request.TareaRequestDTO;
import com.rodrigo.construccion.dto.response.EtapaDiariaCreacionResponseDTO;
import com.rodrigo.construccion.dto.response.EtapasDiariasResponseDTO;
import com.rodrigo.construccion.service.EtapasDiariasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para gestión de etapas diarias y asignación de profesionales a tareas
 */
@RestController
@RequestMapping("/api/profesionales/etapas-diarias")
@RequiredArgsConstructor
@Slf4j
public class EtapasDiariasController {

    private final EtapasDiariasService etapasDiariasService;

    /**
     * GET /api/profesionales/etapas-diarias/{obraId}/{fecha}
     * Obtener profesionales disponibles y tareas del día
     */
    @GetMapping("/{obraId}/{fecha}")
    public ResponseEntity<Map<String, Object>> obtenerEtapasDiarias(
            @PathVariable Long obraId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader("empresaId") Long empresaId) {
        
        try {
            log.info("GET /api/profesionales/etapas-diarias/{}/{} - empresaId: {}", obraId, fecha, empresaId);

            EtapasDiariasResponseDTO response = etapasDiariasService.obtenerEtapasDiarias(obraId, fecha, empresaId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response
            ));

        } catch (RuntimeException e) {
            log.error("Error al obtener etapas diarias", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "error", "ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/profesionales/etapas-diarias
     * Crear/Actualizar tareas del día
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> guardarEtapasDiarias(
            @Valid @RequestBody EtapasDiariasRequestDTO request,
            @RequestHeader("empresaId") Long empresaId) {
        
        try {
            log.info("POST /api/profesionales/etapas-diarias - obra: {}, fecha: {}, empresaId: {}", 
                     request.getObraId(), request.getFecha(), empresaId);

            EtapaDiariaCreacionResponseDTO response = etapasDiariasService.guardarEtapasDiarias(request, empresaId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Etapas diarias creadas correctamente");
            result.put("data", Map.of(
                    "fecha", request.getFecha(),
                    "obraId", request.getObraId(),
                    "tareasCreadas", response.getTareasCreadas(),
                    "asignacionesProfesionales", response.getAsignacionesProfesionales(),
                    "etapas", response.getTareas()
            ));

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (RuntimeException e) {
            log.error("Error al guardar etapas diarias", e);
            
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String errorCode = "VALIDATION_ERROR";
            
            if (e.getMessage().contains("no encontrada") || e.getMessage().contains("no encontrado")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "NOT_FOUND";
            }

            return ResponseEntity.status(status).body(Map.of(
                    "success", false,
                    "error", errorCode,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * PUT /api/profesionales/etapas-diarias/tarea/{tareaId}
     * Actualizar una tarea específica
     */
    @PutMapping("/tarea/{tareaId}")
    public ResponseEntity<Map<String, Object>> actualizarTarea(
            @PathVariable Long tareaId,
            @Valid @RequestBody TareaRequestDTO request,
            @RequestHeader("empresaId") Long empresaId) {
        
        try {
            log.info("PUT /api/profesionales/etapas-diarias/tarea/{} - empresaId: {}", tareaId, empresaId);

            etapasDiariasService.actualizarTarea(tareaId, request, empresaId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tarea actualizada correctamente"
            ));

        } catch (RuntimeException e) {
            log.error("Error al actualizar tarea", e);
            
            HttpStatus status = e.getMessage().contains("no encontrada") 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(Map.of(
                    "success", false,
                    "error", status == HttpStatus.NOT_FOUND ? "NOT_FOUND" : "ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * DELETE /api/profesionales/etapas-diarias/tarea/{tareaId}
     * Eliminar una tarea
     */
    @DeleteMapping("/tarea/{tareaId}")
    public ResponseEntity<Map<String, Object>> eliminarTarea(
            @PathVariable Long tareaId,
            @RequestHeader("empresaId") Long empresaId) {
        
        try {
            log.info("DELETE /api/profesionales/etapas-diarias/tarea/{} - empresaId: {}", tareaId, empresaId);

            etapasDiariasService.eliminarTarea(tareaId, empresaId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tarea eliminada correctamente"
            ));

        } catch (RuntimeException e) {
            log.error("Error al eliminar tarea", e);
            
            HttpStatus status = e.getMessage().contains("no encontrada") 
                    ? HttpStatus.NOT_FOUND 
                    : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(Map.of(
                    "success", false,
                    "error", status == HttpStatus.NOT_FOUND ? "NOT_FOUND" : "ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/profesionales/etapas-diarias/obra/{obraId}
     * Obtener todas las etapas de una obra (histórico)
     */
    @GetMapping("/obra/{obraId}")
    public ResponseEntity<Map<String, Object>> obtenerEtapasPorObra(
            @PathVariable Long obraId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String estado,
            @RequestHeader("empresaId") Long empresaId) {
        
        try {
            log.info("GET /api/profesionales/etapas-diarias/obra/{} - empresaId: {}", obraId, empresaId);

            // TODO: Implementar lógica de histórico
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Funcionalidad en desarrollo",
                    "data", Map.of(
                            "obraId", obraId,
                            "totalEtapas", 0
                    )
            ));

        } catch (RuntimeException e) {
            log.error("Error al obtener etapas por obra", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "error", "ERROR",
                    "message", e.getMessage()
            ));
        }
    }
}
