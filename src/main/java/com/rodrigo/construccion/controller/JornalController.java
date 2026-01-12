package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.JornalRequestDTO;
import com.rodrigo.construccion.dto.response.ResumenPeriodoJornalDTO;
import com.rodrigo.construccion.model.entity.Jornal;
import com.rodrigo.construccion.dto.response.EstadisticasJornalResponseDTO;
import com.rodrigo.construccion.dto.response.ProfesionalJornalResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenJornalesProfesionalDTO;
import com.rodrigo.construccion.service.JornalService;
import com.rodrigo.construccion.service.ProfesionalService;
// import com.rodrigo.construccion.dto.response.ProfesionalCatalogoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jornales") // Changed to /api/jornales
@RequiredArgsConstructor
@Tag(name = "Jornales", description = "Operaciones de trabajo diario de cada profesional")
public class JornalController {

    private final JornalService jornalService;
    private final ProfesionalService profesionalService;

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Obtener catálogo de jornales/profesionales", description = "Devuelve profesionales formateados para catálogo")
    public ResponseEntity<List<java.util.Map<String, Object>>> obtenerCatalogo(@PathVariable Long empresaId) {
        return ResponseEntity.ok(profesionalService.obtenerCatalogoPorEmpresa(empresaId));
    }


    /* ENDPOINTS QUE RETORNAN DTO'S - LISTOS PARA USAR, VERIFICAR URLS */
    @GetMapping("/todos")
    @Operation(summary = "Listar todos los jornales tipo DTOs", description = "Obtiene todos los jornales del sistema, agrupados por profesional y obra.")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerTodosJornalesProfesionales() {
        return ResponseEntity.ok(jornalService.obtenerTodosAgrupados());
    }

    @GetMapping("/buscar/{id}")
    @Operation(summary = "Obtener jornal por ID", description = "Obtiene un jornal específico por su ID, retornando un DTO")
    public ResponseEntity<ProfesionalJornalResponseDTO> obtenerJornalPorId(
            @Parameter(description = "ID del jornal") @PathVariable Long id) {
        return ResponseEntity.ok(jornalService.obtenerJornalPorId(id));
    }

    @GetMapping("/buscar-por-idEmpresa")
    @Operation(summary = "Obtener jornales por empresa (agrupados)", description = "Obtiene los jornales de una empresa, agrupados por profesional y obra para una respuesta más eficiente.")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerJornalPorIdEmpresa(
            @Parameter(description = "ID de la empresa") @RequestParam Long idEmpresa) {
        return ResponseEntity.ok(jornalService.buscarPorEmpresaAgrupado(idEmpresa));
    }

    @PostMapping("/crear")
    @Operation(summary = "Crear jornal", description = "Crea un nuevo jornal en el sistema")
    public ResponseEntity<ProfesionalJornalResponseDTO> crearJornalDto(@RequestBody JornalRequestDTO jornalRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jornalService.crearJornal(jornalRequestDto));
    }

    @PutMapping("/actualizar/{idJornal}")
    @Operation(summary = "Actualizar jornal", description = "Actualiza un jornal existente")
    public ResponseEntity<ProfesionalJornalResponseDTO> actualizarJornal(
            @Parameter(description = "ID del jornal") @PathVariable Long idJornal,
            @RequestBody JornalRequestDTO jornalRequestDTO) {
        return ResponseEntity.ok().body(jornalService.actualizarJornal(idJornal, jornalRequestDTO));
    }

    @DeleteMapping("/eliminar/{id}")
    @Operation(summary = "Eliminar jornal", description = "Elimina un jornal del sistema")
    public ResponseEntity<String> eliminarJornalPorId(@Parameter(description = "ID del jornal") @PathVariable Long id) {
        jornalService.eliminar(id);
        return ResponseEntity.ok("Jornal eliminado con éxito.");
    }

    @GetMapping("/buscar/por-fecha")
    @Operation(summary = "Jornales por rango de fechas", description = "Obtiene jornales dentro de un rango de fechas")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerJornalesPorRangoFechasDto(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(jornalService.buscarJornalesPorRangoFechas(empresaId, fechaInicio, fechaFin));
    }

    @GetMapping("/buscar/valor-minimo")
    @Operation(summary = "Jornales por valor mínimo", description = "Obtiene jornales con valor por hora mayor o igual al especificado")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerJornalesPorValorMinimoDto(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(description = "Valor mínimo por hora") @RequestParam BigDecimal valorMinimo) {
        List<ResumenJornalesProfesionalDTO> resumen = jornalService.buscarJornalPorValorMinimo(empresaId, valorMinimo);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/buscar/mes-actual")
    @Operation(summary = "Jornales del mes actual", description = "Obtiene jornales del mes en curso")
    public ResponseEntity<List<ResumenJornalesProfesionalDTO>> obtenerTodosJornalesMesActual(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(jornalService.obtenerJornalesDelMesActual(empresaId));
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de jornales", description = "Obtiene estadísticas generales de jornales")
    public ResponseEntity<EstadisticasJornalResponseDTO> obtenerEstadisticas(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(jornalService.obtenerEstadisticas(empresaId));
    }

    @GetMapping("/resumen-periodo")
    @Operation(summary = "Resumen por período", description = "Obtiene resumen de jornales en un período específico")
    public ResponseEntity<ResumenPeriodoJornalDTO> obtenerResumenPeriodo(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        ResumenPeriodoJornalDTO resumen = jornalService.obtenerResumenPeriodo(empresaId, fechaInicio, fechaFin);
        return ResponseEntity.ok(resumen);
    }


    /* METODOS NO RECOMENDADOS PARA USAR POR SUS BUCLES INFINITOS */
    @GetMapping
    @Operation(summary = "Listar jornales", description = "Obtiene todos los jornales con paginación opcional")
    public ResponseEntity<Page<Jornal>> listarJornales(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Jornal> jornales = jornalService.obtenerTodosPaginados(pageable);
        return ResponseEntity.ok(jornales);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener jornal", description = "Obtiene un jornal específico por su ID")
    public ResponseEntity<Jornal> obtenerJornal(
            @Parameter(description = "ID del jornal") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return jornalService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear jornal", description = "Crea un nuevo jornal en el sistema")
    public ResponseEntity<Jornal> crearJornal(
            @RequestBody Jornal jornal,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        Jornal jornalCreado = jornalService.crear(jornal);
        return ResponseEntity.status(HttpStatus.CREATED).body(jornalCreado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar jornal", description = "Actualiza un jornal existente")
    public ResponseEntity<Jornal> actualizarJornal(
            @Parameter(description = "ID del jornal") @PathVariable Long id,
            @RequestBody Jornal jornal,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            Jornal jornalActualizado = jornalService.actualizar(id, jornal);
            return ResponseEntity.ok(jornalActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar jornal", description = "Elimina un jornal del sistema")
    public ResponseEntity<Void> eliminarJornal(
            @Parameter(description = "ID del jornal") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            jornalService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * OPERACIONES ESPECIALIZADAS DE JORNALES
     */

    @GetMapping("/por-fecha")
    @Operation(summary = "Jornales por rango de fechas", description = "Obtiene jornales dentro de un rango de fechas")
    public ResponseEntity<List<Jornal>> obtenerJornalesPorRangoFechas(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        List<Jornal> jornales = jornalService.buscarPorRangoFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(jornales);
    }

    @GetMapping("/por-valor-minimo")
    @Operation(summary = "Jornales por valor mínimo", description = "Obtiene jornales con valor por hora mayor o igual al especificado")
    public ResponseEntity<List<Jornal>> obtenerJornalesPorValorMinimo(
            @Parameter(description = "Valor mínimo por hora") @RequestParam BigDecimal valorMinimo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        List<Jornal> jornales = jornalService.buscarPorValorMinimo(valorMinimo);
        return ResponseEntity.ok(jornales);
    }

    @GetMapping("/mes-actual")
    @Operation(summary = "Jornales del mes actual", description = "Obtiene jornales del mes en curso")
    public ResponseEntity<List<Jornal>> obtenerJornalesMesActual(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        List<Jornal> jornales = jornalService.obtenerJornalesMesActual();
        return ResponseEntity.ok(jornales);
    }

    @GetMapping("/todos-simple")
    @Operation(summary = "Lista simple de jornales", description = "Obtiene todos los jornales sin paginación")
    public ResponseEntity<List<Jornal>> obtenerTodosJornales(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Obteniendo todos los jornales para empresa: " + empresaId);
        List<Jornal> jornales = jornalService.obtenerTodos();
        return ResponseEntity.ok(jornales);
    }
}