package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.EtapaDiariaRequestDTO;
import com.rodrigo.construccion.dto.response.EtapaDiariaResponseDTO;
import com.rodrigo.construccion.service.IEtapaDiariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestión de Etapas Diarias
 */
@RestController
@RequestMapping("/api/etapas-diarias")
@RequiredArgsConstructor
@Tag(name = "Etapas Diarias", description = "Gestión de etapas diarias de obras")
public class EtapaDiariaController {

    private final IEtapaDiariaService etapaDiariaService;

    @GetMapping
    @Operation(summary = "Listar etapas diarias por obra (query param)", 
               description = "Obtiene todas las etapas diarias de una obra usando query parameter. " +
                             "Requiere empresaId en el header y obraId como query param.")
    public ResponseEntity<List<EtapaDiariaResponseDTO>> listar(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra (query param)", required = true)
            @RequestParam Long obraId) {
        
        List<EtapaDiariaResponseDTO> etapas = etapaDiariaService.obtenerPorObra(empresaId, obraId);
        return ResponseEntity.ok(etapas);
    }

    @GetMapping("/obra/{obraId}")
    @Operation(summary = "Obtener etapas diarias por obra (path param)", 
               description = "Obtiene todas las etapas diarias de una obra específica ordenadas por fecha descendente. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<List<EtapaDiariaResponseDTO>> obtenerPorObra(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId) {
        
        List<EtapaDiariaResponseDTO> etapas = etapaDiariaService.obtenerPorObra(empresaId, obraId);
        return ResponseEntity.ok(etapas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener etapa diaria por ID", 
               description = "Obtiene una etapa diaria específica por su ID. Requiere empresaId en el header.")
    public ResponseEntity<EtapaDiariaResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la etapa diaria", required = true)
            @PathVariable Long id) {
        
        EtapaDiariaResponseDTO etapa = etapaDiariaService.obtenerPorId(empresaId, id);
        return ResponseEntity.ok(etapa);
    }

    @GetMapping("/obra/{obraId}/fecha/{fecha}")
    @Operation(summary = "Obtener etapa diaria por obra y fecha", 
               description = "Obtiene la etapa diaria de una obra en una fecha específica. " +
                             "Requiere empresaId en el header. Formato de fecha: YYYY-MM-DD")
    public ResponseEntity<EtapaDiariaResponseDTO> obtenerPorObraYFecha(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "Fecha de la etapa (formato: YYYY-MM-DD)", required = true, example = "2025-12-03")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        EtapaDiariaResponseDTO etapa = etapaDiariaService.obtenerPorObraYFecha(empresaId, obraId, fecha);
        return ResponseEntity.ok(etapa);
    }

    @PostMapping
    @Operation(summary = "Crear nueva etapa diaria", 
               description = "Crea una nueva etapa diaria. Requiere empresaId en el header. " +
                             "Campos obligatorios: obraId, fecha y estado. " +
                             "Campos nulleables: descripcion y observaciones. " +
                             "No se permite duplicar la misma fecha para una obra.")
    public ResponseEntity<EtapaDiariaResponseDTO> crear(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "Datos de la etapa diaria a crear", required = true)
            @Valid @RequestBody EtapaDiariaRequestDTO request) {
        
        EtapaDiariaResponseDTO etapaCreada = etapaDiariaService.crear(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(etapaCreada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar etapa diaria", 
               description = "Actualiza una etapa diaria existente. Requiere empresaId en el header. " +
                             "No se permite modificar a una fecha que ya existe para la obra.")
    public ResponseEntity<EtapaDiariaResponseDTO> actualizar(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la etapa diaria a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados de la etapa diaria", required = true)
            @Valid @RequestBody EtapaDiariaRequestDTO request) {
        
        EtapaDiariaResponseDTO etapaActualizada = etapaDiariaService.actualizar(empresaId, id, request);
        return ResponseEntity.ok(etapaActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar etapa diaria", 
               description = "Elimina una etapa diaria. Requiere empresaId en el header.")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la etapa diaria a eliminar", required = true)
            @PathVariable Long id) {
        
        etapaDiariaService.eliminar(empresaId, id);
        return ResponseEntity.noContent().build();
    }
}
