package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.HonorarioRequestDTO;
import com.rodrigo.construccion.dto.response.HonorarioProfesionalObraResponseDTO;
import com.rodrigo.construccion.dto.response.EstadisticasHonorarioResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenHonorariosProfesionalDTO;
import com.rodrigo.construccion.dto.response.ResumenPeriodoHonorarioDTO;
import com.rodrigo.construccion.model.entity.Honorario;
import com.rodrigo.construccion.service.HonorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/honorarios")
@Tag(name = "Honorarios", description = "Operaciones de gestión de honorarios profesionales")
public class HonorarioController {

    private final HonorarioService honorarioService;

    @GetMapping("/todos")
    @Operation(summary = "Lista simple de honorarios en DTOs", description = "Obtiene todos los honorarios sin tener en cuenta la empresa.")
    public ResponseEntity<List<ResumenHonorariosProfesionalDTO>> obtenerTodosHonorarios() {
        return ResponseEntity.ok(honorarioService.obtenerTodosHonorarios());
    }

    @GetMapping("/todos/paginacion")
    @Operation(summary = "Listar honorarios con DTOs", description = "Obtiene todos los honorarios con paginación opcional")
    public ResponseEntity<Page<ResumenHonorariosProfesionalDTO>> listarHonorariosPaginacion(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
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
    public ResponseEntity<HonorarioProfesionalObraResponseDTO> obtenerHonorarioPorId(
            @Parameter(description = "ID del honorario") @PathVariable Long idHonorario) {
        return ResponseEntity.ok(honorarioService.obtenerHonorarioPorId(idHonorario));
    }

    @PostMapping("/crear")
    @Operation(summary = "Crear honorario DTO", description = "Crea un nuevo honorario en el sistema")
    public ResponseEntity<HonorarioProfesionalObraResponseDTO> crearNuevoHonorario(
            @RequestBody HonorarioRequestDTO honorarioRequestDTO) {
        HonorarioProfesionalObraResponseDTO honorarioCreado = honorarioService.crearHonorario(honorarioRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(honorarioCreado);
    }

    @PutMapping("/modificar/{idHonorario}")
    @Operation(summary = "Actualizar honorario DTO", description = "Actualiza un honorario existente")
    public ResponseEntity<HonorarioProfesionalObraResponseDTO> actualizarHonorario(
            @Parameter(description = "ID del honorario") @PathVariable Long idHonorario,
            @RequestBody HonorarioRequestDTO honorarioRequestDTO) {
        HonorarioProfesionalObraResponseDTO honorarioActualizado = honorarioService.actualizarHonorario(idHonorario,
                honorarioRequestDTO);
        return ResponseEntity.ok(honorarioActualizado);
    }

    @DeleteMapping("/eliminar/{id}")
    @Operation(summary = "Eliminar honorario", description = "Elimina un honorario del sistema")
    public ResponseEntity<String> eliminarHonorario(
            @Parameter(description = "ID del honorario") @PathVariable Long id) {
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
    public ResponseEntity<List<ResumenHonorariosProfesionalDTO>> obtenerHonorarioPorMontoMinimo(
            @Parameter(description = "Monto mínimo") @RequestParam BigDecimal montoMinimo,
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
        EstadisticasHonorarioResponseDTO estadisticas = honorarioService.obtenerEstadisticasPorEmpresa(empresaId);
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/obtener/resumen-periodo")
    @Operation(summary = "Resumen por período", description = "Obtiene resumen de honorarios en un período específico")
    public ResponseEntity<ResumenPeriodoHonorarioDTO> obtenerResumenHonorariosPeriodo(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        ResumenPeriodoHonorarioDTO resumen = honorarioService.obtenerResumenPeriodo(fechaInicio, fechaFin, empresaId);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/busqueda-dinamica")
    @Operation(summary = "Búsqueda avanzada", description = "Realiza búsqueda de honorarios con múltiples criterios")
    public ResponseEntity<List<ResumenHonorariosProfesionalDTO>> busquedaAvanzadaHonorarios(
            @Parameter(description = "Fecha inicio (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "Monto mínimo (opcional)") @RequestParam(required = false) BigDecimal montoMinimo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        List<ResumenHonorariosProfesionalDTO> resultado = honorarioService.busquedaAvanzada(empresaId, fechaInicio,
                fechaFin, montoMinimo);
        return ResponseEntity.ok(resultado);
    }

    // --------------------------------------------------------------

    /* METODOS ANTIGUOS NO RECOMENDADOS */
    @GetMapping
    @Operation(summary = "Listar honorarios", description = "Obtiene todos los honorarios con paginación opcional")
    public ResponseEntity<Page<Honorario>> listarHonorarios(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {

        System.out.println("Listando honorarios para empresa: " + empresaId + " con paginación: " + pageable);
        Page<Honorario> honorarios = honorarioService.obtenerTodosPaginados(pageable);
        return ResponseEntity.ok(honorarios);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener honorario", description = "Obtiene un honorario específico por su ID")
    public ResponseEntity<Honorario> obtenerHonorario(
            @Parameter(description = "ID del honorario") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Obteniendo honorario ID: " + id + " para empresa: " + empresaId);
        return honorarioService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear honorario", description = "Crea un nuevo honorario en el sistema")
    public ResponseEntity<Honorario> crearHonorario(
            @RequestBody Honorario honorario,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Creando nuevo honorario para empresa: " + empresaId);
        Honorario honorarioCreado = honorarioService.crear(honorario);
        return ResponseEntity.status(HttpStatus.CREATED).body(honorarioCreado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar honorario", description = "Actualiza un honorario existente")
    public ResponseEntity<Honorario> actualizarUnHonorario(
            @Parameter(description = "ID del honorario") @PathVariable Long id,
            @RequestBody Honorario honorario,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Actualizando honorario ID: " + id + " para empresa: " + empresaId);
        try {
            Honorario honorarioActualizado = honorarioService.actualizar(id, honorario);
            return ResponseEntity.ok(honorarioActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar honorario", description = "Elimina un honorario del sistema")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del honorario") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Eliminando honorario ID: " + id + " para empresa: " + empresaId);
        try {
            honorarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/obtener/estadisticas")
    @Operation(summary = "Estadísticas de honorarios", description = "Obtiene estadísticas generales de honorarios")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasHonorarios(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Obteniendo estadísticas de honorarios para empresa: " + empresaId);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalHonorarios", honorarioService.contarTotal());

        // Honorarios del mes actual
        List<Honorario> honorariosMes = honorarioService.obtenerHonorariosMesActual();
        estadisticas.put("honorariosMesActual", honorariosMes.size());

        BigDecimal totalMes = honorariosMes.stream()
                .map(Honorario::getMonto)
                .filter(monto -> monto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        estadisticas.put("montoTotalMesActual", totalMes);
        estadisticas.put("fechaConsulta", LocalDate.now());

        return ResponseEntity.ok(estadisticas);
    }

    /* OPERACIONES ESPECIALIZADAS DE HONORARIOS */

    @GetMapping("/por-fecha")
    @Operation(summary = "Honorarios por rango de fechas", description = "Obtiene honorarios dentro de un rango de fechas")
    public ResponseEntity<List<Honorario>> obtenerPorRangoFechas(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println(
                "Obteniendo honorarios entre " + fechaInicio + " y " + fechaFin + " para empresa: " + empresaId);
        List<Honorario> honorarios = honorarioService.buscarPorRangoFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(honorarios);
    }

    @GetMapping("/por-monto-minimo")
    @Operation(summary = "Honorarios por monto mínimo", description = "Obtiene honorarios con monto mayor o igual al especificado")
    public ResponseEntity<List<Honorario>> obtenerPorMontoMinimo(
            @Parameter(description = "Monto mínimo") @RequestParam BigDecimal montoMinimo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Obteniendo honorarios con monto >= " + montoMinimo + " para empresa: " + empresaId);
        List<Honorario> honorarios = honorarioService.buscarPorMontoMinimo(montoMinimo);
        return ResponseEntity.ok(honorarios);
    }

    @GetMapping("/mes-actual")
    @Operation(summary = "Honorarios del mes actual", description = "Obtiene honorarios del mes en curso")
    public ResponseEntity<List<Honorario>> obtenerMesActual(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Obteniendo honorarios del mes actual para empresa: " + empresaId);
        List<Honorario> honorarios = honorarioService.obtenerHonorariosMesActual();
        return ResponseEntity.ok(honorarios);
    }

    /* Me falta desde aca */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de honorarios", description = "Obtiene estadísticas generales de honorarios")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Obteniendo estadísticas de honorarios para empresa: " + empresaId);

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalHonorarios", honorarioService.contarTotal());

        // Honorarios del mes actual
        List<Honorario> honorariosMes = honorarioService.obtenerHonorariosMesActual();
        estadisticas.put("honorariosMesActual", honorariosMes.size());

        BigDecimal totalMes = honorariosMes.stream()
                .map(Honorario::getMonto)
                .filter(monto -> monto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        estadisticas.put("montoTotalMesActual", totalMes);
        estadisticas.put("fechaConsulta", LocalDate.now());

        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/resumen-periodo")
    @Operation(summary = "Resumen por período", description = "Obtiene resumen de honorarios en un período específico")
    public ResponseEntity<Map<String, Object>> obtenerResumenPeriodo(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Obteniendo resumen de honorarios entre " + fechaInicio + " y " + fechaFin
                + " para empresa: " + empresaId);

        List<Honorario> honorariosPeriodo = honorarioService.buscarPorRangoFechas(fechaInicio, fechaFin);
        BigDecimal totalPeriodo = honorarioService.calcularTotalPeriodo(fechaInicio, fechaFin);

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("periodo", fechaInicio + " - " + fechaFin);
        resumen.put("cantidadHonorarios", honorariosPeriodo.size());
        resumen.put("montoTotal", totalPeriodo);

        if (!honorariosPeriodo.isEmpty()) {
            BigDecimal promedio = totalPeriodo.divide(
                    BigDecimal.valueOf(honorariosPeriodo.size()),
                    2,
                    BigDecimal.ROUND_HALF_UP);
            resumen.put("montoPromedio", promedio);
        } else {
            resumen.put("montoPromedio", BigDecimal.ZERO);
        }

        return ResponseEntity.ok(resumen);
    }

    /**
     * OPERACIONES DE CONSULTA AVANZADA
     */

    @GetMapping("/todos-simple")
    @Operation(summary = "Lista simple de honorarios", description = "Obtiene todos los honorarios sin paginación")
    public ResponseEntity<List<Honorario>> obtenerTodos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Obteniendo todos los honorarios para empresa: " + empresaId);
        List<Honorario> honorarios = honorarioService.obtenerTodos();
        return ResponseEntity.ok(honorarios);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Búsqueda avanzada", description = "Realiza búsqueda de honorarios con múltiples criterios")
    public ResponseEntity<List<Honorario>> busquedaAvanzada(
            @Parameter(description = "Fecha inicio (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "Monto mínimo (opcional)") @RequestParam(required = false) BigDecimal montoMinimo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

        System.out.println("Búsqueda avanzada de honorarios para empresa: " + empresaId);

        List<Honorario> resultado = honorarioService.obtenerTodos();

        // Aplicar filtros según los parámetros proporcionados
        if (fechaInicio != null && fechaFin != null) {
            resultado = honorarioService.buscarPorRangoFechas(fechaInicio, fechaFin);
        }

        if (montoMinimo != null) {
            resultado = resultado.stream()
                    .filter(honorario -> honorario.getMonto() != null &&
                            honorario.getMonto().compareTo(montoMinimo) >= 0)
                    .toList();
        }

        return ResponseEntity.ok(resultado);
    }
}