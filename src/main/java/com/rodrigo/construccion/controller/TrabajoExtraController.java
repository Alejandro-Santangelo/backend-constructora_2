package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.TrabajoExtraRequestDTO;
import com.rodrigo.construccion.dto.request.TrabajoExtraEstadoUpdateDTO;
import com.rodrigo.construccion.dto.response.TrabajoExtraResponseDTO;
import com.rodrigo.construccion.dto.response.TrabajoExtraPdfResponseDTO;
import com.rodrigo.construccion.service.ITrabajoExtraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para gestión de Trabajos Extra por Día
 */
@RestController
@RequestMapping("/api/v1/trabajos-extra")
@RequiredArgsConstructor
@Tag(name = "Trabajos Extra", description = "Gestión de trabajos extra (mini-presupuestos diarios) en obras")
public class TrabajoExtraController {

    private final ITrabajoExtraService trabajoExtraService;

    @GetMapping
    @Operation(summary = "Obtener trabajos extra por obra (query params)", 
               description = "Obtiene todos los trabajos extra de una obra específica usando query params. " +
                             "Requiere empresaId y obraId como parámetros de consulta.")
    public ResponseEntity<List<TrabajoExtraResponseDTO>> obtenerPorObraQueryParams(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestParam("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra (obligatorio)", required = true)
            @RequestParam("obraId") Long obraId) {
        
        List<TrabajoExtraResponseDTO> trabajos = trabajoExtraService.obtenerPorObra(empresaId, obraId);
        return ResponseEntity.ok(trabajos);
    }

    @GetMapping("/obra/{obraId}")
    @Operation(summary = "Obtener trabajos extra por obra (path variable)", 
               description = "Obtiene todos los trabajos extra de una obra específica. Requiere empresaId en el header.")
    public ResponseEntity<List<TrabajoExtraResponseDTO>> obtenerPorObra(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId) {
        
        List<TrabajoExtraResponseDTO> trabajos = trabajoExtraService.obtenerPorObra(empresaId, obraId);
        return ResponseEntity.ok(trabajos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener trabajo extra por ID", 
               description = "Obtiene un trabajo extra específico por su ID. Requiere empresaId en el header.")
    public ResponseEntity<TrabajoExtraResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID del trabajo extra", required = true)
            @PathVariable Long id) {
        
        TrabajoExtraResponseDTO trabajo = trabajoExtraService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(trabajo);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo trabajo extra", 
               description = "Crea un nuevo trabajo extra por día (mini-presupuesto). Requiere empresaId en el header o como parámetro. " +
                             "El clienteId se obtiene automáticamente de la obra padre. " +
                             "Los campos nulleables son: observaciones, días, profesionales y tareas.")
    public ResponseEntity<TrabajoExtraResponseDTO> crear(
            @Parameter(description = "ID de la empresa (requerido en header o query param)")
            @RequestHeader(value = "empresaId", required = false) Long empresaIdHeader,
            @Parameter(description = "ID de la empresa (requerido en header o query param)")
            @RequestParam(value = "empresaId", required = false) Long empresaIdParam,
            @Parameter(description = "Datos del trabajo extra a crear", required = true)
            @Valid @RequestBody TrabajoExtraRequestDTO request) {
        
        Long empresaId = empresaIdHeader != null ? empresaIdHeader : empresaIdParam;
        if (empresaId == null) {
            throw new RuntimeException("El empresaId es obligatorio (en header o parámetro)");
        }
        
        TrabajoExtraResponseDTO trabajoCreado = trabajoExtraService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trabajoCreado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar trabajo extra", 
               description = "Actualiza un trabajo extra existente. Requiere empresaId en el header o como parámetro. " +
                             "Las listas de días, profesionales y tareas se recrean completamente. " +
                             "El clienteId se obtiene automáticamente de la obra padre.")
    public ResponseEntity<TrabajoExtraResponseDTO> actualizar(
            @Parameter(description = "ID de la empresa (requerido en header o query param)")
            @RequestHeader(value = "empresaId", required = false) Long empresaIdHeader,
            @Parameter(description = "ID de la empresa (requerido en header o query param)")
            @RequestParam(value = "empresaId", required = false) Long empresaIdParam,
            @Parameter(description = "ID del trabajo extra a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados del trabajo extra", required = true)
            @Valid @RequestBody TrabajoExtraRequestDTO request) {
        
        Long empresaId = empresaIdHeader != null ? empresaIdHeader : empresaIdParam;
        if (empresaId == null) {
            throw new RuntimeException("El empresaId es obligatorio (en header o parámetro)");
        }
        
        TrabajoExtraResponseDTO trabajoActualizado = trabajoExtraService.actualizar(empresaId, id, request);
        return ResponseEntity.ok(trabajoActualizado);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente trabajo extra (PATCH)", 
               description = "Actualiza parcialmente un trabajo extra existente. Solo actualiza los campos enviados en el request. " +
                             "Requiere empresaId en el header o como parámetro. " +
                             "Las listas (días, profesionales, tareas) solo se actualizan si se envían en el request.")
    public ResponseEntity<TrabajoExtraResponseDTO> actualizarParcial(
            @Parameter(description = "ID de la empresa (requerido en header o query param)")
            @RequestHeader(value = "empresaId", required = false) Long empresaIdHeader,
            @Parameter(description = "ID de la empresa (requerido en header o query param)")
            @RequestParam(value = "empresaId", required = false) Long empresaIdParam,
            @Parameter(description = "ID del trabajo extra a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Datos a actualizar del trabajo extra (campos opcionales)", required = true)
            @RequestBody TrabajoExtraRequestDTO request) {
        
        Long empresaId = empresaIdHeader != null ? empresaIdHeader : empresaIdParam;
        if (empresaId == null) {
            throw new RuntimeException("El empresaId es obligatorio (en header o parámetro)");
        }
        
        TrabajoExtraResponseDTO trabajoActualizado = trabajoExtraService.actualizarParcial(empresaId, id, request);
        return ResponseEntity.ok(trabajoActualizado);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de trabajo extra", 
               description = "Cambia únicamente el estado de un trabajo extra (ej: de A_ENVIAR a ENVIADO). " +
                             "Este endpoint está optimizado para actualizaciones rápidas de estado desde el frontend.")
    public ResponseEntity<TrabajoExtraResponseDTO> cambiarEstado(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID del trabajo extra", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado del trabajo extra", required = true)
            @Valid @RequestBody TrabajoExtraEstadoUpdateDTO estadoRequest) {
        
        TrabajoExtraResponseDTO trabajoActualizado = trabajoExtraService.cambiarEstado(empresaId, id, estadoRequest.getEstado());
        return ResponseEntity.ok(trabajoActualizado);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar trabajo extra", 
               description = "Elimina un trabajo extra y todos sus datos relacionados (días, profesionales, tareas) en cascada. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID del trabajo extra a eliminar", required = true)
            @PathVariable Long id) {
        
        trabajoExtraService.eliminar(empresaId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/profesionales/{profesionalId}")
    @Operation(summary = "Eliminar profesional de asignación", 
               description = "Elimina un profesional específico de una asignación de trabajo extra. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<Void> eliminarProfesional(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID del profesional a eliminar", required = true)
            @PathVariable Long profesionalId) {
        
        trabajoExtraService.eliminarProfesional(empresaId, profesionalId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/materiales/{materialId}")
    @Operation(summary = "Eliminar material de asignación", 
               description = "Elimina un material específico de una asignación de trabajo extra. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<Void> eliminarMaterial(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID del material a eliminar", required = true)
            @PathVariable Long materialId) {
        
        trabajoExtraService.eliminarMaterial(empresaId, materialId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/gastos-generales/{gastoId}")
    @Operation(summary = "Eliminar gasto general de asignación", 
               description = "Elimina un gasto general específico de una asignación de trabajo extra. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<Void> eliminarGastoGeneral(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID del gasto general a eliminar", required = true)
            @PathVariable Long gastoId) {
        
        trabajoExtraService.eliminarGastoGeneral(empresaId, gastoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir PDF de trabajo extra", 
               description = "Sube un archivo PDF generado para un trabajo extra. " +
                             "El archivo debe estar en formato PDF. Devuelve el ID del PDF guardado y la fecha de generación.")
    public ResponseEntity<TrabajoExtraPdfResponseDTO> subirPdf(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID del trabajo extra", required = true)
            @PathVariable Long id,
            @Parameter(description = "Archivo PDF (obligatorio)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Usuario que genera el PDF")
            @RequestParam(value = "generadoPor", required = false) String generadoPor) {
        
        TrabajoExtraPdfResponseDTO response = trabajoExtraService.guardarPdf(empresaId, id, file, generadoPor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
