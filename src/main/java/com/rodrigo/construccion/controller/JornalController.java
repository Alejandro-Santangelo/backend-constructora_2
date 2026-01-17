package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.JornalRequestDTO;
import com.rodrigo.construccion.dto.response.ResumenPeriodoJornalDTO;
import com.rodrigo.construccion.model.entity.Jornal;
import com.rodrigo.construccion.dto.response.EstadisticasJornalResponseDTO;
import com.rodrigo.construccion.dto.response.ProfesionalJornalResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenJornalesProfesionalDTO;
import com.rodrigo.construccion.service.JornalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/jornales")
@RequiredArgsConstructor
public class JornalController {

    private final JornalService jornalService;

    @GetMapping
    public ResponseEntity<Page<ProfesionalJornalResponseDTO>> listarJornales(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                             @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(jornalService.obtenerTodosPaginados(pageable));
    }

    @GetMapping("/todos-simple")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerTodosJornalesProfesionales() {
        return ResponseEntity.ok(jornalService.obtenerTodosAgrupados());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesionalJornalResponseDTO> obtenerJornalPorId(@Parameter(description = "ID del jornal") @PathVariable Long id) {
        return ResponseEntity.ok(jornalService.obtenerJornalPorId(id));
    }

    @GetMapping("/empresa/{idEmpresa}")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerJornalesPorEmpresa(
            @Parameter(description = "ID de la empresa", required = true, example = "1")
            @PathVariable Long idEmpresa) {
        List<ResumenJornalesProfesionalDTO> jornales = jornalService.buscarPorEmpresa(idEmpresa);
        return ResponseEntity.ok(jornales);
    }

    @PostMapping
    public ResponseEntity<ProfesionalJornalResponseDTO> crearJornalDto(@RequestBody JornalRequestDTO jornalRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jornalService.crearJornal(jornalRequestDto));
    }

    @PutMapping("/{idJornal}")
    public ResponseEntity<ProfesionalJornalResponseDTO> actualizarJornal(@Parameter(description = "ID del jornal") @PathVariable Long idJornal,
                                                                         @RequestBody JornalRequestDTO jornalRequestDTO) {
        return ResponseEntity.ok().body(jornalService.actualizarJornal(idJornal, jornalRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarJornalPorId(@Parameter(description = "ID del jornal") @PathVariable Long id) {
        jornalService.eliminar(id);
        return ResponseEntity.ok("Jornal eliminado con éxito.");
    }

    @GetMapping("/por-fecha")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerJornalesPorRangoFechasDto(@Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                                                                                @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                                                                                @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(jornalService.buscarJornalesPorRangoFechas(empresaId, fechaInicio, fechaFin));
    }

    @GetMapping("/por-valor-minimo")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerJornalesPorValorMinimoDto(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                                                @Parameter(description = "Valor mínimo por hora") @RequestParam BigDecimal valorMinimo) {
        return ResponseEntity.ok(jornalService.buscarJornalPorValorMinimo(empresaId, valorMinimo));
    }

    @GetMapping("/mes-actual")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerTodosJornalesMesActual(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(jornalService.obtenerJornalesDelMesActual(empresaId));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasJornalResponseDTO> obtenerEstadisticas(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(jornalService.obtenerEstadisticas(empresaId));
    }

    @GetMapping("/resumen-periodo")
    public ResponseEntity<ResumenPeriodoJornalDTO> obtenerResumenPeriodo(@Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                                                         @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                                                         @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(jornalService.obtenerResumenPeriodo(empresaId, fechaInicio, fechaFin));
    }
}