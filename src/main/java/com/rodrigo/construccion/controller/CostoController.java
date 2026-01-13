package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.CostoRequestDTO;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.model.entity.Costo;
import com.rodrigo.construccion.service.ICostoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/costos")
public class CostoController {

    private final ICostoService costoService;

    @PostMapping
    @Operation(summary = "Crear costo", description = "Registra un nuevo costo en el sistema")
    public ResponseEntity<CostoResponseDTO> crearCosto(@RequestBody CostoRequestDTO dto, @RequestParam Long empresaId) {
        return new ResponseEntity<>(costoService.crearCostoDesdeDTO(dto, empresaId), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar costos", description = "Obtiene lista paginada de costos")
    public ResponseEntity<PaginacionCostosResponse> listarCostos(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                 @Parameter(description = "Número de página (la primera es 0)") @RequestParam(defaultValue = "0") int page,
                                                                 @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(costoService.listarCostosConPaginacion(empresaId, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener costo", description = "Obtiene un costo por su ID")
    public ResponseEntity<Costo> obtenerCosto(@Parameter(description = "ID del costo") @PathVariable Long id,
                                              @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return costoService.obtenerPorIdYEmpresa(id, empresaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar costo", description = "Actualiza los datos de un costo")
    public ResponseEntity<CostoResponseDTO> actualizarCosto(@Parameter(description = "ID del costo") @PathVariable Long id,
                                                            @RequestBody CostoRequestDTO costoRequestDTO,
                                                            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.actualizarCosto(id, costoRequestDTO, empresaId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar costo", description = "Elimina un costo del sistema")
    public ResponseEntity<String> eliminarCosto(@Parameter(description = "ID del costo") @PathVariable Long id,
                                                @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        costoService.eliminarCosto(id, empresaId);
        return ResponseEntity.ok("Costo eliminado con éxito.");
    }

    /* CONSULTAS POR OBRA */

    @GetMapping("/obra/{obraId}")
    @Operation(summary = "Costos por obra", description = "Obtiene todos los costos de una obra específica")
    public ResponseEntity<PaginacionCostosResponse> obtenerCostosPorObra(@Parameter(description = "ID de la obra") @PathVariable Long obraId,
                                                                         @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size) {
        PaginacionCostosResponse response = costoService.obtenerCostosPorObraConPaginacion(obraId, empresaId, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar costo", description = "Aprueba un costo registrado")
    public ResponseEntity<CostoResponseDTO> aprobarCosto(@Parameter(description = "ID del costo") @PathVariable Long id,
                                                         @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                         @Parameter(description = "Comentarios de aprobación") @RequestParam(required = false) String comentarios) {
        return ResponseEntity.ok(costoService.aprobarCosto(id, comentarios, empresaId));
    }

    @PostMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar costo", description = "Rechaza un costo registrado")
    public ResponseEntity<CostoResponseDTO> rechazarCosto(@Parameter(description = "ID del costo") @PathVariable Long id,
                                                          @Parameter(description = "Motivo del rechazo") @RequestParam String motivoRechazo,
                                                          @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.rechazarCosto(id, motivoRechazo, empresaId));
    }

    /* MÉTODOS QUE NO ESTÁN SIENDO USADOS EN EL FRONTEND */

    @GetMapping("/obra/{obraId}/total")
    @Operation(summary = "Total costos obra", description = "Obtiene el total de costos de una obra")
    public ResponseEntity<TotalCostosObraResponseDTO> obtenerTotalCostosObra(@Parameter(description = "ID de la obra") @PathVariable Long obraId,
                                                                             @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.calcularTotalCostosObra(obraId, empresaId));
    }

    /* CONSULTAS POR CATEGORÍA Y TIPO */

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Costos por categoría", description = "Obtiene costos de una categoría específica")
    public ResponseEntity<PaginacionCostosResponse> obtenerCostosPorCategoria(@Parameter(description = "Categoría del costo") @PathVariable String categoria,
                                                                              @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(costoService.obtenerCostosPorCategoriaConPaginacion(categoria, empresaId, PageRequest.of(page, size)));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Costos por tipo", description = "Obtiene costos de un tipo específico. Ejemplos de tipo: Materiales, " +
            "Mano de obra, Honorarios, Servicios, Gastos generales, Subcontratos, Equipos, Viáticos, Otros")
    public ResponseEntity<PaginacionCostosResponse> listarCostosPorTipo(@PathVariable String tipo,
                                                                        @RequestParam Long empresaId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(costoService.listarCostosPorTipoConPaginacion(empresaId, tipo, PageRequest.of(page, size)));
    }

    /* CONSULTAS POR FECHA  */

    @GetMapping("/periodo")
    @Operation(summary = "Costos por período", description = "Obtiene costos en un rango de fechas. Formato de fecha: YYYY-MM-DD (ejemplo: 2025-10-05)")
    public ResponseEntity<PaginacionCostosResponse> obtenerCostosPorPeriodo(@Parameter(description = "Fecha desde") @RequestParam LocalDate fechaDesde,
                                                                            @Parameter(description = "Fecha hasta") @RequestParam LocalDate fechaHasta,
                                                                            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size) {
        PaginacionCostosResponse response = costoService.obtenerCostosPorPeriodoConPaginacion(fechaDesde, fechaHasta, empresaId, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mes-actual")
    @Operation(summary = "Costos del mes", description = "Obtiene costos del mes actual")
    public ResponseEntity<PaginacionCostosResponse> obtenerCostosDelMes(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        PaginacionCostosResponse response = costoService.obtenerCostosDelMesConPaginacion(empresaId, PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    /* BÚSQUEDAS Y FILTROS */

    @GetMapping("/buscar")
    @Operation(summary = "Buscar costos", description = "Busca costos por texto en los campos 'descripción' y 'concepto' de cada costo. Ingrese una palabra o frase para encontrar coincidencias en ambos campos.")
    public ResponseEntity<PaginacionCostosResponse> buscarCostos(@Parameter(description = "Texto de búsqueda") @RequestParam String texto,
                                                                 @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(costoService.buscarCostosConPaginacion(empresaId, texto, PageRequest.of(page, size)));
    }

    @GetMapping("/filtrar")
    @Hidden
    public ResponseEntity<PaginacionCostosResponse> filtrarCostos(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                  @Parameter(description = "ID de la obra") @RequestParam(required = false) Long obraId,
                                                                  @Parameter(description = "Categoría") @RequestParam(required = false) String categoria,
                                                                  @Parameter(description = "Tipo") @RequestParam(required = false) String tipo,
                                                                  @Parameter(description = "Monto mínimo") @RequestParam(required = false) Double montoMinimo,
                                                                  @Parameter(description = "Monto máximo") @RequestParam(required = false) Double montoMaximo,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(costoService.filtrarCostosConPaginacion(empresaId, obraId, categoria, tipo, montoMinimo, montoMaximo, PageRequest.of(page, size)));
    }

    /* ANÁLISIS DE COSTOS */

    @GetMapping("/analisis/obra/{obraId}")
    @Operation(summary = "Análisis costos obra", description = "Obtiene análisis detallado de costos por obra")
    public ResponseEntity<AnalisisCostosObraResponseDTO> obtenerAnalisisCostosObra(@Parameter(description = "ID de la obra") @PathVariable Long obraId,
                                                                                   @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.obtenerAnalisisCostosObra(obraId, empresaId));
    }

    @GetMapping("/analisis/presupuesto-vs-real/{obraId}")
    @Operation(summary = "Presupuesto vs Real", description = "Compara costos presupuestados vs reales")
    public ResponseEntity<PresupuestoVsRealResponseDTO> compararPresupuestoVsReal(@Parameter(description = "ID de la obra") @PathVariable Long obraId,
                                                                                  @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.compararPresupuestoVsReal(obraId, empresaId));
    }

    @GetMapping("/analisis/rentabilidad/{obraId}")
    @Operation(summary = "Análisis rentabilidad", description = "Calcula la rentabilidad de una obra")
    public ResponseEntity<RentabilidadObraResponseDTO> calcularRentabilidad(@Parameter(description = "ID de la obra") @PathVariable Long obraId,
                                                                            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.calcularRentabilidad(obraId, empresaId));
    }

    /* ESTADÍSTICAS Y MÉTRICAS  */

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas generales", description = "Obtiene estadísticas generales de costos")
    public ResponseEntity<EstadisticasCostosResponseDTO> obtenerEstadisticas(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.obtenerEstadisticas(empresaId));
    }

    @GetMapping("/estadisticas/categorias")
    @Operation(summary = "Estadísticas por categoría", description = "Obtiene distribución de costos por categoría")
    public ResponseEntity<List<EstadisticasPorCategoriaDTO>> obtenerEstadisticasPorCategoria(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.obtenerEstadisticasPorCategoria(empresaId));
    }

    @GetMapping("/estadisticas/obras")
    @Operation(summary = "Estadísticas por obra", description = "Obtiene estadísticas de costos por obra")
    public ResponseEntity<List<EstadisticasPorObraDTO>> obtenerEstadisticasPorObra(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.obtenerEstadisticasPorObra(empresaId));
    }

    @GetMapping("/estadisticas/mensuales")
    @Operation(summary = "Estadísticas mensuales", description = "Obtiene evolución mensual de costos")
    public ResponseEntity<List<EstadisticasMensualesDTO>> obtenerEstadisticasMensuales(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.obtenerEstadisticasMensuales(empresaId));
    }

    /* REPORTES */

    @GetMapping("/reporte/resumen-obras")
    @Operation(summary = "Reporte resumen obras", description = "Obtiene reporte resumido de costos por obra")
    public ResponseEntity<List<ReporteResumenObraDTO>> obtenerReporteResumenObras(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.obtenerReporteResumenObras(empresaId));
    }

    @GetMapping("/reporte/variaciones-presupuesto")
    @Operation(summary = "Reporte variaciones", description = "Obtiene reporte de variaciones presupuestarias")
    public ResponseEntity<List<ReporteVariacionesPresupuestoDTO>> obtenerReporteVariacionesPresupuesto(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.obtenerReporteVariacionesPresupuesto(empresaId));
    }

    @GetMapping("/reporte/top-costos")
    @Operation(summary = "Top costos", description = "Obtiene los costos más altos del período")
    public ResponseEntity<List<TopCostosDTO>> obtenerTopCostos(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                               @Parameter(description = "Cantidad de registros") @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(costoService.obtenerTopCostos(empresaId, limite));
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorías", description = "Obtiene lista de categorías de costos disponibles")
    public ResponseEntity<List<String>> listarCategorias(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.listarCategorias(empresaId));
    }

    @GetMapping("/tipos")
    @Operation(summary = "Listar tipos", description = "Obtiene lista de tipos de costos disponibles")
    public ResponseEntity<List<String>> listarTipos(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.listarTipos(empresaId));
    }

    @GetMapping("/contar")
    @Operation(summary = "Contar costos", description = "Obtiene el número total de costos")
    public ResponseEntity<Long> contarCostos(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(costoService.contarCostosPorEmpresa(empresaId));
    }
}