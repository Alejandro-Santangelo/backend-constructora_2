package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.CheckInRequest;
import com.rodrigo.construccion.dto.request.CheckOutRequest;
import com.rodrigo.construccion.dto.response.AsistenciaObraResponse;
import com.rodrigo.construccion.exception.AsistenciaDuplicadaException;
import com.rodrigo.construccion.exception.CheckInNoEncontradoException;
import com.rodrigo.construccion.exception.HorarioInvalidoException;
import com.rodrigo.construccion.service.AsistenciaObraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar asistencias de profesionales en obras
 */
@RestController
@RequestMapping("/v1/asistencia-obra")
@Tag(name = "Asistencia Obra", description = "Control de asistencia con geolocalización y cálculo de horas trabajadas")
public class AsistenciaObraController {

    private final AsistenciaObraService asistenciaService;

    public AsistenciaObraController(AsistenciaObraService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    /**
     * Registrar check-in (entrada)
     */
    @PostMapping("/check-in")
    @Operation(
        summary = "Registrar entrada (check-in)",
        description = "Registra la entrada de un profesional a la obra con geolocalización. " +
                     "Solo puede haber un registro por profesional por día."
    )
    public ResponseEntity<AsistenciaObraResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        try {
            AsistenciaObraResponse response = asistenciaService.checkIn(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AsistenciaDuplicadaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (HorarioInvalidoException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Registrar check-out (salida)
     */
    @PutMapping("/{id}/check-out")
    @Operation(
        summary = "Registrar salida (check-out)",
        description = "Registra la salida de un profesional de la obra con geolocalización. " +
                     "Calcula automáticamente las horas trabajadas en formato decimal."
    )
    public ResponseEntity<AsistenciaObraResponse> checkOut(
            @Parameter(description = "ID del registro de asistencia", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CheckOutRequest request) {
        try {
            AsistenciaObraResponse response = asistenciaService.checkOut(id, request);
            return ResponseEntity.ok(response);
        } catch (CheckInNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (HorarioInvalidoException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Listar historial de asistencias de un profesional
     */
    @GetMapping("/profesional/{profesionalObraId}")
    @Operation(
        summary = "Historial de asistencias por profesional",
        description = "Obtiene el historial completo de asistencias de un profesional con horas trabajadas por día"
    )
    public ResponseEntity<List<AsistenciaObraResponse>> listarAsistenciasPorProfesional(
            @Parameter(description = "ID de la asignación profesional-obra", required = true)
            @PathVariable Long profesionalObraId,
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        List<AsistenciaObraResponse> asistencias = asistenciaService.listarAsistenciasPorProfesional(
            profesionalObraId, empresaId
        );
        return ResponseEntity.ok(asistencias);
    }

    /**
     * Obtener reporte de asistencias de una obra (por dirección)
     */
    @GetMapping("/reporte-obra")
    @Operation(
        summary = "Reporte de asistencias por obra",
        description = "Obtiene un reporte de asistencias de una obra con total de horas " +
                     "trabajadas por profesional. La obra se identifica por sus 4 campos de dirección."
    )
    public ResponseEntity<Map<String, Object>> obtenerReporteObra(
            @Parameter(description = "Calle de la obra", required = true)
            @RequestParam String direccionObraCalle,
            @Parameter(description = "Altura de la obra", required = true)
            @RequestParam String direccionObraAltura,
            @Parameter(description = "Piso de la obra")
            @RequestParam(required = false) String direccionObraPiso,
            @Parameter(description = "Departamento de la obra")
            @RequestParam(required = false) String direccionObraDepartamento,
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        Map<String, Object> reporte = asistenciaService.obtenerReporteObra(
            direccionObraCalle,
            direccionObraAltura,
            direccionObraPiso,
            direccionObraDepartamento,
            empresaId
        );
        return ResponseEntity.ok(reporte);
    }

    /**
     * Obtener asistencia de hoy para un profesional
     */
    @GetMapping("/hoy/{profesionalObraId}")
    @Operation(
        summary = "Obtener asistencia de hoy",
        description = "Obtiene la asistencia del día actual para un profesional si existe, " +
                     "o retorna 404 si no ha marcado entrada todavía"
    )
    public ResponseEntity<AsistenciaObraResponse> obtenerAsistenciaHoy(
            @Parameter(description = "ID de la asignación profesional-obra", required = true)
            @PathVariable Long profesionalObraId,
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        try {
            AsistenciaObraResponse asistencia = asistenciaService.obtenerAsistenciaHoy(
                profesionalObraId, empresaId
            );
            return ResponseEntity.ok(asistencia);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
