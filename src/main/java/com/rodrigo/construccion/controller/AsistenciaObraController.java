package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.CheckInRequest;
import com.rodrigo.construccion.dto.request.CheckOutRequest;
import com.rodrigo.construccion.dto.response.AsistenciaObraResponse;
import com.rodrigo.construccion.dto.response.ReporteAsistenciasObraResponseDTO;
import com.rodrigo.construccion.service.IAsistenciaObraService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/asistencia-obra")
public class AsistenciaObraController {

    private final IAsistenciaObraService asistenciaService;

    /* Registrar check-in (entrada) */
    @PostMapping("/check-in")
    public ResponseEntity<AsistenciaObraResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asistenciaService.checkIn(request));
    }

    /* Registrar check-out (salida) */
    @PutMapping("/{id}/check-out")
    public ResponseEntity<AsistenciaObraResponse> checkOut(@Parameter(description = "ID del registro de asistencia", required = true)
                                                           @PathVariable Long id,
                                                           @Valid @RequestBody CheckOutRequest request) {
        return ResponseEntity.ok(asistenciaService.checkOut(id, request));
    }

    /* Listar historial de asistencias de un profesional */
    @GetMapping("/profesional/{profesionalObraId}")
    public ResponseEntity<List<AsistenciaObraResponse>> listarAsistenciasPorProfesional(@Parameter(description = "ID de la asignación profesional-obra", required = true)
                                                                                        @PathVariable Long profesionalObraId,
                                                                                        @Parameter(description = "ID de la empresa", required = true)
                                                                                        @RequestParam Long empresaId) {
        List<AsistenciaObraResponse> asistencias = asistenciaService.listarAsistenciasPorProfesional(
                profesionalObraId, empresaId
        );
        return ResponseEntity.ok(asistencias);
    }

    /* Obtener asistencia de hoy para un profesional */
    @GetMapping("/hoy/{profesionalObraId}")
    public ResponseEntity<AsistenciaObraResponse> obtenerAsistenciaHoy(@Parameter(description = "ID de la asignación profesional-obra", required = true)
                                                                       @PathVariable Long profesionalObraId,
                                                                       @Parameter(description = "ID de la empresa", required = true)
                                                                       @RequestParam Long empresaId) {
        return ResponseEntity.ok(asistenciaService.obtenerAsistenciaHoy(profesionalObraId, empresaId));
    }

    /* Obtener reporte de asistencias de una obra (por dirección) - NO USADO EN EL FRONTEND */
    @GetMapping("/reporte-obra")
    public ResponseEntity<ReporteAsistenciasObraResponseDTO> obtenerReporteObra(@Parameter(description = "Calle de la obra", required = true)
                                                                                @RequestParam String direccionObraCalle,
                                                                                @Parameter(description = "Altura de la obra", required = true)
                                                                                @RequestParam String direccionObraAltura,
                                                                                @Parameter(description = "Piso de la obra")
                                                                                @RequestParam(required = false) String direccionObraPiso,
                                                                                @Parameter(description = "Departamento de la obra")
                                                                                @RequestParam(required = false) String direccionObraDepartamento,
                                                                                @Parameter(description = "ID de la empresa", required = true)
                                                                                @RequestParam Long empresaId) {
        ReporteAsistenciasObraResponseDTO reporte = asistenciaService.obtenerReporteObra(
                direccionObraCalle,
                direccionObraAltura,
                direccionObraPiso,
                direccionObraDepartamento,
                empresaId
        );
        return ResponseEntity.ok(reporte);
    }
}
