package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.ProfesionalJornalDiarioRequestDTO;
import com.rodrigo.construccion.dto.response.ProfesionalJornalDiarioResponseDTO;
import com.rodrigo.construccion.dto.response.ProfesionalJornalResumenDTO;
import com.rodrigo.construccion.service.ProfesionalJornalDiarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Jornales Diarios", description = "Registro de tiempo trabajado por profesionales en obras")
@RestController
@RequestMapping("/api/jornales-diarios")
@RequiredArgsConstructor
@Slf4j
public class ProfesionalJornalDiarioController {

    private final ProfesionalJornalDiarioService jornalService;

    @Operation(summary = "Crear nuevo jornal diario",
               description = "Registra las horas trabajadas por un profesional en una obra en una fecha específica")
    @PostMapping
    public ResponseEntity<ProfesionalJornalDiarioResponseDTO> crear(
            @Valid @RequestBody ProfesionalJornalDiarioRequestDTO requestDTO) {
        log.info("POST /api/jornales-diarios - Crear jornal: profesional={}, obra={}, fecha={}", 
                 requestDTO.getProfesionalId(), requestDTO.getObraId(), requestDTO.getFecha());
        
        ProfesionalJornalDiarioResponseDTO response = jornalService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Actualizar jornal existente",
               description = "Modifica un jornal diario registrado previamente")
    @PutMapping("/{id}")
    public ResponseEntity<ProfesionalJornalDiarioResponseDTO> actualizar(
            @Parameter(description = "ID del jornal") @PathVariable Long id,
            @Valid @RequestBody ProfesionalJornalDiarioRequestDTO requestDTO) {
        log.info("PUT /api/jornales-diarios/{} - Actualizar jornal", id);
        
        ProfesionalJornalDiarioResponseDTO response = jornalService.actualizar(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener jornal por ID",
               description = "Consulta un jornal específico por su identificador")
    @GetMapping("/{id}")
    public ResponseEntity<ProfesionalJornalDiarioResponseDTO> obtenerPorId(
            @Parameter(description = "ID del jornal") @PathVariable Long id) {
        log.info("GET /api/jornales-diarios/{} - Obtener jornal", id);
        
        ProfesionalJornalDiarioResponseDTO response = jornalService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar jornal",
               description = "Elimina un registro de jornal diario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del jornal") @PathVariable Long id) {
        log.info("DELETE /api/jornales-diarios/{} - Eliminar jornal", id);
        
        jornalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener jornales de un profesional en una obra",
               description = "Lista todos los jornales registrados para un profesional específico en una obra específica")
    @GetMapping("/profesional/{profesionalId}/obra/{obraId}")
    public ResponseEntity<List<ProfesionalJornalDiarioResponseDTO>> obtenerPorProfesionalYObra(
            @Parameter(description = "ID del profesional") @PathVariable Long profesionalId,
            @Parameter(description = "ID de la obra") @PathVariable Long obraId) {
        log.info("GET /api/jornales-diarios/profesional/{}/obra/{}", profesionalId, obraId);
        
        List<ProfesionalJornalDiarioResponseDTO> jornales = 
            jornalService.obtenerJornalesPorProfesionalYObra(profesionalId, obraId);
        return ResponseEntity.ok(jornales);
    }

    @Operation(summary = "Obtener todos los jornales de un profesional",
               description = "Lista todos los jornales registrados para un profesional en todas sus obras")
    @GetMapping("/profesional/{profesionalId}")
    public ResponseEntity<List<ProfesionalJornalDiarioResponseDTO>> obtenerPorProfesional(
            @Parameter(description = "ID del profesional") @PathVariable Long profesionalId) {
        log.info("GET /api/jornales-diarios/profesional/{}", profesionalId);
        
        List<ProfesionalJornalDiarioResponseDTO> jornales = 
            jornalService.obtenerJornalesPorProfesional(profesionalId);
        return ResponseEntity.ok(jornales);
    }

    @Operation(summary = "Obtener todos los jornales de una obra",
               description = "Lista todos los jornales registrados en una obra (todos los profesionales)")
    @GetMapping("/obra/{obraId}")
    public ResponseEntity<List<ProfesionalJornalDiarioResponseDTO>> obtenerPorObra(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId) {
        log.info("GET /api/jornales-diarios/obra/{}", obraId);
        
        List<ProfesionalJornalDiarioResponseDTO> jornales = 
            jornalService.obtenerJornalesPorObra(obraId);
        return ResponseEntity.ok(jornales);
    }

    @Operation(summary = "Obtener jornales de un profesional en un rango de fechas",
               description = "Filtra jornales por profesional y rango de fechas")
    @GetMapping("/profesional/{profesionalId}/fechas")
    public ResponseEntity<List<ProfesionalJornalDiarioResponseDTO>> obtenerPorProfesionalYFechas(
            @Parameter(description = "ID del profesional") @PathVariable Long profesionalId,
            @Parameter(description = "Fecha desde (formato: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @Parameter(description = "Fecha hasta (formato: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        log.info("GET /api/jornales-diarios/profesional/{}/fechas?fechaDesde={}&fechaHasta={}", 
                 profesionalId, fechaDesde, fechaHasta);
        
        List<ProfesionalJornalDiarioResponseDTO> jornales = 
            jornalService.obtenerJornalesPorProfesionalYFechas(profesionalId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(jornales);
    }

    @Operation(summary = "Obtener jornales en un rango de fechas (todos los profesionales/obras)",
               description = "Filtra jornales solo por rango de fechas y empresa")
    @GetMapping("/fechas")
    public ResponseEntity<List<ProfesionalJornalDiarioResponseDTO>> obtenerPorFechas(
            @Parameter(description = "Fecha desde (formato: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @Parameter(description = "Fecha hasta (formato: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @Parameter(description = "ID de la empresa") 
            @RequestParam Long empresaId) {
        log.info("GET /api/jornales-diarios/fechas?fechaDesde={}&fechaHasta={}&empresaId={}", 
                 fechaDesde, fechaHasta, empresaId);
        
        List<ProfesionalJornalDiarioResponseDTO> jornales = 
            jornalService.obtenerJornalesPorFechasYEmpresa(fechaDesde, fechaHasta, empresaId);
        return ResponseEntity.ok(jornales);
    }

    @Operation(summary = "Obtener jornales de una obra en un rango de fechas",
               description = "Filtra jornales por obra y rango de fechas")
    @GetMapping("/obra/{obraId}/fechas")
    public ResponseEntity<List<ProfesionalJornalDiarioResponseDTO>> obtenerPorObraYFechas(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "Fecha desde (formato: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @Parameter(description = "Fecha hasta (formato: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        log.info("GET /api/jornales-diarios/obra/{}/fechas?fechaDesde={}&fechaHasta={}", 
                 obraId, fechaDesde, fechaHasta);
        
        List<ProfesionalJornalDiarioResponseDTO> jornales = 
            jornalService.obtenerJornalesPorObraYFechas(obraId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(jornales);
    }

    @Operation(summary = "Obtener resumen de jornales de un profesional en una obra",
               description = "Retorna totales agregados: cantidad de jornales, horas totales y monto total cobrado")
    @GetMapping("/resumen/profesional/{profesionalId}/obra/{obraId}")
    public ResponseEntity<ProfesionalJornalResumenDTO> obtenerResumenProfesionalEnObra(
            @Parameter(description = "ID del profesional") @PathVariable Long profesionalId,
            @Parameter(description = "ID de la obra") @PathVariable Long obraId) {
        log.info("GET /api/jornales-diarios/resumen/profesional/{}/obra/{}", profesionalId, obraId);
        
        ProfesionalJornalResumenDTO resumen = 
            jornalService.obtenerResumenProfesionalEnObra(profesionalId, obraId);
        return ResponseEntity.ok(resumen);
    }

    @Operation(summary = "Obtener resumen de todos los profesionales en una obra",
               description = "Retorna totales agregados por cada profesional que trabajó en la obra")
    @GetMapping("/resumen/obra/{obraId}/profesionales")
    public ResponseEntity<List<ProfesionalJornalResumenDTO>> obtenerResumenProfesionalesPorObra(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId) {
        log.info("GET /api/jornales-diarios/resumen/obra/{}/profesionales", obraId);
        
        List<ProfesionalJornalResumenDTO> resumenes = 
            jornalService.obtenerResumenProfesionalesPorObra(obraId);
        return ResponseEntity.ok(resumenes);
    }

    @Operation(summary = "Obtener resumen de todas las obras de un profesional",
               description = "Retorna totales agregados por cada obra en la que trabajó el profesional")
    @GetMapping("/resumen/profesional/{profesionalId}/obras")
    public ResponseEntity<List<ProfesionalJornalResumenDTO>> obtenerResumenObrasPorProfesional(
            @Parameter(description = "ID del profesional") @PathVariable Long profesionalId) {
        log.info("GET /api/jornales-diarios/resumen/profesional/{}/obras", profesionalId);
        
        List<ProfesionalJornalResumenDTO> resumenes = 
            jornalService.obtenerResumenObrasPorProfesional(profesionalId);
        return ResponseEntity.ok(resumenes);
    }
}
