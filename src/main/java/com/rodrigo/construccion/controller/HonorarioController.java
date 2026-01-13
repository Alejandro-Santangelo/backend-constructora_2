package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.HonorarioRequestDTO;
import com.rodrigo.construccion.dto.response.HonorarioProfesionalObraResponseDTO;
import com.rodrigo.construccion.dto.response.EstadisticasHonorarioResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenHonorariosProfesionalDTO;
import com.rodrigo.construccion.dto.response.ResumenPeriodoHonorarioDTO;
import com.rodrigo.construccion.service.HonorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/honorarios")
public class HonorarioController {

    private final HonorarioService honorarioService;

    @GetMapping("/todos")
    @Operation(summary = "Lista simple de honorarios en DTOs", description = "Obtiene todos los honorarios sin tener en cuenta la empresa.")
    public ResponseEntity<List<ResumenHonorariosProfesionalDTO>> obtenerTodosHonorarios() {
        return ResponseEntity.ok(honorarioService.obtenerTodosHonorarios());
    }

    @GetMapping("/todos/paginacion")
    @Operation(summary = "Listar honorarios con DTOs", description = "Obtiene todos los honorarios con paginación opcional")
    public ResponseEntity<Page<ResumenHonorariosProfesionalDTO>> listarHonorariosPaginacion(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                                            @PageableDefault(size = 10, sort = "profesional.nombre") Pageable pageable) {
        Pageable pageableCorregido = pageable;
        // Verificamos si el sort contiene la propiedad inválida "string"
        if (pageable.getSort().stream().anyMatch(order -> order.getProperty().equals("string"))) {
            // Si es así, creamos un nuevo Pageable con un ordenamiento por defecto válido
            pageableCorregido = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("profesional.nombre").ascending() // Ordenar por el nombre del profesional
            );
        }

        return ResponseEntity.ok(honorarioService.obtenerTodosHonorariosPaginados(pageableCorregido, empresaId));
    }

    @GetMapping("/buscar/{idHonorario}")
    @Operation(summary = "Obtener honorario DTO", description = "Obtiene un honorario específico por su ID")
    public ResponseEntity<HonorarioProfesionalObraResponseDTO> obtenerHonorarioPorId(@Parameter(description = "ID del honorario")
                                                                                     @PathVariable Long idHonorario) {
        return ResponseEntity.ok(honorarioService.obtenerHonorarioPorId(idHonorario));
    }

    @PostMapping("/crear")
    @Operation(summary = "Crear honorario DTO", description = "Crea un nuevo honorario en el sistema")
    public ResponseEntity<HonorarioProfesionalObraResponseDTO> crearNuevoHonorario(@RequestBody HonorarioRequestDTO honorarioRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(honorarioService.crearHonorario(honorarioRequestDTO));
    }

    @PutMapping("/modificar/{idHonorario}")
    @Operation(summary = "Actualizar honorario DTO", description = "Actualiza un honorario existente")
    public ResponseEntity<HonorarioProfesionalObraResponseDTO> actualizarHonorario(@Parameter(description = "ID del honorario") @PathVariable Long idHonorario,
                                                                                   @RequestBody HonorarioRequestDTO honorarioRequestDTO) {
        return ResponseEntity.ok(honorarioService.actualizarHonorario(idHonorario, honorarioRequestDTO));
    }

    @DeleteMapping("/eliminar/{id}")
    @Operation(summary = "Eliminar honorario", description = "Elimina un honorario del sistema")
    public ResponseEntity<String> eliminarHonorario(@Parameter(description = "ID del honorario") @PathVariable Long id) {
        honorarioService.eliminar(id);
        return ResponseEntity.ok().body("Honorario eliminado con éxito.");
    }

    @GetMapping("/buscar/fecha")
    @Operation(summary = "Honorarios por rango de fechas DTO", description = "Obtiene honorarios dentro de un rango de fechas")
    public ResponseEntity<List<ResumenHonorariosProfesionalDTO>> obtenerHonorariosPorRangoFechas(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(honorarioService.buscarHonorariosPorRangoFechas(fechaInicio, fechaFin, empresaId));
    }

    @GetMapping("/buscar/monto-minimo")
    @Operation(summary = "Honorarios por monto mínimo DTO", description = "Obtiene honorarios con monto mayor o igual al especificado")
    public ResponseEntity<List<ResumenHonorariosProfesionalDTO>> obtenerHonorarioPorMontoMinimo(@Parameter(description = "Monto mínimo") @RequestParam BigDecimal montoMinimo,
                                                                                                @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(honorarioService.buscarHonorariosPorMontoMinimo(montoMinimo, empresaId));
    }

    @GetMapping("/buscar/mes-actual")
    @Operation(summary = "Honorarios del mes actual DTOs", description = "Obtiene honorarios del mes en curso")
    public ResponseEntity<List<ResumenHonorariosProfesionalDTO>> obtenerHonorariosMesActual(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(honorarioService.obtenerHonorariosMesActualDto(empresaId));
    }

    @GetMapping("/estadisticas/por-empresa")
    @Operation(summary = "Estadísticas de honorarios por empresa (DTO)", description = "Obtiene estadísticas generales de honorarios para una empresa específica, retornando un DTO.")
    public ResponseEntity<EstadisticasHonorarioResponseDTO> obtenerEstadisticasPorEmpresa(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(honorarioService.obtenerEstadisticasPorEmpresa(empresaId));
    }

    @GetMapping("/obtener/resumen-periodo")
    @Operation(summary = "Resumen por período", description = "Obtiene resumen de honorarios en un período específico")
    public ResponseEntity<ResumenPeriodoHonorarioDTO> obtenerResumenHonorariosPeriodo(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(honorarioService.obtenerResumenPeriodo(fechaInicio, fechaFin, empresaId));
    }

    @GetMapping("/busqueda-dinamica")
    @Operation(summary = "Búsqueda avanzada", description = "Realiza búsqueda de honorarios con múltiples criterios")
    public ResponseEntity<List<ResumenHonorariosProfesionalDTO>> busquedaAvanzadaHonorarios(
            @Parameter(description = "Fecha inicio (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "Monto mínimo (opcional)") @RequestParam(required = false) BigDecimal montoMinimo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(honorarioService.busquedaAvanzada(empresaId, fechaInicio, fechaFin, montoMinimo));
    }

}