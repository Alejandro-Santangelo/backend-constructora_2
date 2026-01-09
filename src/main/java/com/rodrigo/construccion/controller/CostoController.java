package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.model.entity.Costo;
import com.rodrigo.construccion.service.CostoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Costos
 * 
 * Maneja todas las operaciones CRUD y consultas especializadas para costos de obras.
 * Incluye análisis de costos, presupuestos vs reales, y reportes financieros.
 */
@RestController
@RequestMapping("/costos")
@Tag(name = "Costos", description = "Gestión de costos y análisis financiero de obras")
public class CostoController {

    private final CostoService costoService;

    public CostoController(CostoService costoService) {
        this.costoService = costoService;
    }

    /**
     * OPERACIONES CRUD
     */

    @PostMapping
    @Operation(summary = "Crear costo", description = "Registra un nuevo costo en el sistema",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Ejemplo de creación de costo",
                    value = "{\n  \"anio\": 2025,\n  \"fecha\": \"2025-10-05\",\n  \"fechaAprobacion\": \"2025-10-07\",\n  \"imputable\": true,\n  \"monto\": 300000,\n  \"semana\": 41,\n  \"fechaActualizacion\": \"2025-10-05\",\n  \"fechaCreacion\": \"2025-10-05\",\n  \"idObra\": 1,\n  \"estado\": \"Aprobado\",\n  \"tipoCosto\": \"Materiales\",\n  \"categoria\": \"Albañilería\",\n  \"concepto\": \"Compra de ladrillos\",\n  \"comentarios\": \"Compra urgente por faltante\",\n  \"descripcion\": \"Se adquirieron 5000 ladrillos para la obra principal\",\n  \"motivoRechazo\": \"\"\n}")))
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Costo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "El costo ya existe")
    })
    public ResponseEntity<com.rodrigo.construccion.dto.response.CostoResponseDTO> crearCosto(
            @RequestBody com.rodrigo.construccion.dto.request.CostoRequestDTO dto,
            @RequestParam Long empresaId) {
        if (dto.getIdObra() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        var costoCreado = costoService.crearCostoDesdeDTO(dto, empresaId);
        com.rodrigo.construccion.dto.response.CostoResponseDTO response = new com.rodrigo.construccion.dto.response.CostoResponseDTO();
        response.id_costo = costoCreado.getId();
        response.anio = costoCreado.getAnio();
        response.fecha = costoCreado.getFecha();
        response.fecha_aprobacion = costoCreado.getFechaAprobacion();
        response.imputable = costoCreado.getImputable();
        response.monto = costoCreado.getMonto();
        response.semana = costoCreado.getSemana();
        response.fecha_actualizacion = costoCreado.getFechaActualizacion() != null ? costoCreado.getFechaActualizacion().toLocalDate() : null;
        response.fecha_creacion = costoCreado.getFechaCreacion() != null ? costoCreado.getFechaCreacion().toLocalDate() : null;
        response.id_obra = costoCreado.getObra() != null ? costoCreado.getObra().getId() : null;
        response.estado = costoCreado.getEstado();
    response.tipo_costo = costoCreado.getTipoCosto();
    response.concepto = costoCreado.getConcepto();
        response.comentarios = costoCreado.getComentarios();
        response.descripcion = costoCreado.getDescripcion();
        response.motivo_rechazo = costoCreado.getMotivoRechazo();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar costos", description = "Obtiene lista paginada de costos")
    public ResponseEntity<com.rodrigo.construccion.dto.response.PaginacionCostosResponse> listarCostos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(description = "Número de página (la primera es 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Costo> costosPage = costoService.listarCostosPorEmpresa(empresaId, PageRequest.of(page, size));
            List<com.rodrigo.construccion.dto.response.CostoResponseDTO> contenido = costosPage.getContent().stream().map(costo -> {
                com.rodrigo.construccion.dto.response.CostoResponseDTO dto = new com.rodrigo.construccion.dto.response.CostoResponseDTO();
                dto.id_costo = costo.getId();
                dto.anio = costo.getAnio();
                dto.fecha = costo.getFecha();
                dto.fecha_aprobacion = costo.getFechaAprobacion();
                dto.imputable = costo.getImputable();
                dto.monto = costo.getMonto();
                dto.semana = costo.getSemana();
                dto.fecha_actualizacion = costo.getFechaActualizacion() != null ? costo.getFechaActualizacion().toLocalDate() : null;
                dto.fecha_creacion = costo.getFechaCreacion() != null ? costo.getFechaCreacion().toLocalDate() : null;
                dto.id_obra = costo.getObra() != null ? costo.getObra().getId() : null;
                dto.estado = costo.getEstado();
                dto.tipo_costo = costo.getTipoCosto();
                dto.categoria = costo.getCategoria();
                dto.concepto = costo.getConcepto();
                dto.comentarios = costo.getComentarios();
                dto.descripcion = costo.getDescripcion();
                dto.motivo_rechazo = costo.getMotivoRechazo();
                return dto;
            }).toList();

            com.rodrigo.construccion.dto.response.PaginacionCostosResponse.PaginacionInfo paginacion = new com.rodrigo.construccion.dto.response.PaginacionCostosResponse.PaginacionInfo();
            paginacion.numeroPagina = costosPage.getNumber();
            paginacion.tamanoPagina = costosPage.getSize();
            com.rodrigo.construccion.dto.response.PaginacionCostosResponse.OrdenInfo orden = new com.rodrigo.construccion.dto.response.PaginacionCostosResponse.OrdenInfo();
            orden.vacio = costosPage.getSort().isEmpty();
            orden.ordenado = costosPage.getSort().isSorted();
            orden.noOrdenado = costosPage.getSort().isUnsorted();
            paginacion.orden = orden;
            paginacion.desplazamiento = (int) costosPage.getPageable().getOffset();
            paginacion.paginado = costosPage.getPageable().isPaged();
            paginacion.noPaginado = costosPage.getPageable().isUnpaged();

            com.rodrigo.construccion.dto.response.PaginacionCostosResponse response = new com.rodrigo.construccion.dto.response.PaginacionCostosResponse();
            response.contenido = contenido;
            response.paginacion = paginacion;
            response.ultimo = costosPage.isLast();
            response.totalElementos = (int) costosPage.getTotalElements();
            response.totalPaginas = costosPage.getTotalPages();
            response.tamano = costosPage.getSize();
            response.numero = costosPage.getNumber();
            response.primero = costosPage.isFirst();
            response.numeroElementos = costosPage.getNumberOfElements();
            response.vacio = costosPage.isEmpty();
            if (contenido.isEmpty()) {
                response.mensaje = "No hay costos registrados para los parámetros solicitados.";
            }
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            System.out.println("Error al listar costos: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener costo", description = "Obtiene un costo por su ID")
    public ResponseEntity<Costo> obtenerCosto(
            @Parameter(description = "ID del costo") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return costoService.obtenerPorIdYEmpresa(id, empresaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar costo", description = "Actualiza los datos de un costo")
    public ResponseEntity<Costo> actualizarCosto(
            @Parameter(description = "ID del costo") @PathVariable Long id,
            @RequestBody Costo costo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var costoActualizado = costoService.actualizarCosto(id, costo, empresaId);
            return ResponseEntity.ok(costoActualizado);
        } catch (IllegalArgumentException e) {
            System.out.println("Error al actualizar costo: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar costo", description = "Elimina un costo del sistema")
    public ResponseEntity<Void> eliminarCosto(
            @Parameter(description = "ID del costo") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            costoService.eliminarCosto(id, empresaId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            System.out.println("Error al eliminar costo: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * CONSULTAS POR OBRA
     */

    @GetMapping("/obra/{obraId}")
    @Operation(summary = "Costos por obra", description = "Obtiene todos los costos de una obra específica")
    public ResponseEntity<Page<Costo>> obtenerCostosPorObra(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var pageable = PageRequest.of(page, size);
            var costos = costoService.obtenerCostosPorObra(obraId, empresaId, pageable);
            if (costos.isEmpty()) {
                return ResponseEntity.ok().header("X-Info","No hay costos registrados para la obra solicitada.").body(costos);
            }
            return ResponseEntity.ok(costos);
        } catch (Exception e) {
            System.out.println("Error al obtener costos por obra: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/obra/{obraId}/total")
    @Operation(summary = "Total costos obra", description = "Obtiene el total de costos de una obra")
    public ResponseEntity<Map<String, Object>> obtenerTotalCostosObra(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var total = costoService.calcularTotalCostosObra(obraId, empresaId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            System.out.println("Error al calcular total costos obra: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * CONSULTAS POR CATEGORÍA Y TIPO
     */

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Costos por categoría", description = "Obtiene costos de una categoría específica")
    public ResponseEntity<Page<Costo>> obtenerCostosPorCategoria(
            @Parameter(description = "Categoría del costo") @PathVariable String categoria,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var pageable = PageRequest.of(page, size);
            var costos = costoService.obtenerCostosPorCategoria(categoria, empresaId, pageable);
            if (costos.isEmpty()) {
                return ResponseEntity.ok().header("X-Info","No hay costos registrados para la categoría solicitada.").body(costos);
            }
            return ResponseEntity.ok(costos);
        } catch (Exception e) {
            System.out.println("Error al obtener costos por categoría: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Costos por tipo", description = "Obtiene costos de un tipo específico. Ejemplos de tipo: Materiales, Mano de obra, Honorarios, Servicios, Gastos generales, Subcontratos, Equipos, Viáticos, Otros",
        parameters = {
            @Parameter(name = "tipo", description = "Tipo de costo. Ejemplo: Materiales, Mano de obra, Honorarios, Servicios, Gastos generales, Subcontratos, Equipos, Viáticos, Otros", example = "Materiales"),
            @Parameter(name = "empresaId", description = "ID de la empresa", example = "1"),
            @Parameter(name = "page", description = "Número de página (la primera es 0)", example = "0"),
            @Parameter(name = "size", description = "Tamaño de página", example = "10")
        })
    public ResponseEntity<com.rodrigo.construccion.dto.response.PaginacionCostosResponse> listarCostosPorTipo(
            @PathVariable String tipo,
            @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Costo> costosPage = costoService.listarCostosPorTipo(empresaId, tipo, PageRequest.of(page, size));
        List<com.rodrigo.construccion.dto.response.CostoResponseDTO> contenido = costosPage.getContent().stream().map(costo -> {
            com.rodrigo.construccion.dto.response.CostoResponseDTO dto = new com.rodrigo.construccion.dto.response.CostoResponseDTO();
            dto.id_costo = costo.getId();
            dto.anio = costo.getAnio();
            dto.fecha = costo.getFecha();
            dto.fecha_aprobacion = costo.getFechaAprobacion();
            dto.imputable = costo.getImputable();
            dto.monto = costo.getMonto();
            dto.semana = costo.getSemana();
            dto.fecha_actualizacion = costo.getFechaActualizacion() != null ? costo.getFechaActualizacion().toLocalDate() : null;
            dto.fecha_creacion = costo.getFechaCreacion() != null ? costo.getFechaCreacion().toLocalDate() : null;
            dto.id_obra = costo.getObra() != null ? costo.getObra().getId() : null;
            dto.estado = costo.getEstado();
            dto.tipo_costo = costo.getTipoCosto();
            dto.categoria = costo.getCategoria();
            dto.concepto = costo.getConcepto();
            dto.comentarios = costo.getComentarios();
            dto.descripcion = costo.getDescripcion();
            dto.motivo_rechazo = costo.getMotivoRechazo();
            return dto;
        }).toList();

        com.rodrigo.construccion.dto.response.PaginacionCostosResponse.PaginacionInfo paginacion = new com.rodrigo.construccion.dto.response.PaginacionCostosResponse.PaginacionInfo();
        paginacion.numeroPagina = costosPage.getNumber();
        paginacion.tamanoPagina = costosPage.getSize();
        com.rodrigo.construccion.dto.response.PaginacionCostosResponse.OrdenInfo orden = new com.rodrigo.construccion.dto.response.PaginacionCostosResponse.OrdenInfo();
        orden.vacio = costosPage.getSort().isEmpty();
        orden.ordenado = costosPage.getSort().isSorted();
        orden.noOrdenado = costosPage.getSort().isUnsorted();
        paginacion.orden = orden;
        paginacion.desplazamiento = (int) costosPage.getPageable().getOffset();
        paginacion.paginado = costosPage.getPageable().isPaged();
        paginacion.noPaginado = costosPage.getPageable().isUnpaged();

        com.rodrigo.construccion.dto.response.PaginacionCostosResponse response = new com.rodrigo.construccion.dto.response.PaginacionCostosResponse();
        response.contenido = contenido;
        response.paginacion = paginacion;
        response.ultimo = costosPage.isLast();
        response.totalElementos = (int) costosPage.getTotalElements();
        response.totalPaginas = costosPage.getTotalPages();
        response.tamano = costosPage.getSize();
        response.numero = costosPage.getNumber();
        response.primero = costosPage.isFirst();
        response.numeroElementos = costosPage.getNumberOfElements();
        response.vacio = costosPage.isEmpty();
            if (contenido.isEmpty()) {
                response.mensaje = "No hay costos registrados para el tipo solicitado.";
            }
        return ResponseEntity.ok(response);
    }

    /**
     * CONSULTAS POR FECHA
     */

    @GetMapping("/periodo")
    @Operation(summary = "Costos por período", description = "Obtiene costos en un rango de fechas. Formato de fecha: YYYY-MM-DD (ejemplo: 2025-10-05)")
    public ResponseEntity<Page<Costo>> obtenerCostosPorPeriodo(
            @Parameter(description = "Fecha desde") @RequestParam LocalDate fechaDesde,
            @Parameter(description = "Fecha hasta") @RequestParam LocalDate fechaHasta,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var pageable = PageRequest.of(page, size);
            var costos = costoService.obtenerCostosPorPeriodo(fechaDesde, fechaHasta, empresaId, pageable);
            if (costos.isEmpty()) {
                return ResponseEntity.ok().header("X-Info","No hay costos registrados para el período solicitado.").body(costos);
            }
            return ResponseEntity.ok(costos);
        } catch (Exception e) {
                System.out.println("Error al obtener costos por período: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mes-actual")
    @Operation(summary = "Costos del mes", description = "Obtiene costos del mes actual")
    public ResponseEntity<Page<Costo>> obtenerCostosDelMes(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var pageable = PageRequest.of(page, size);
            var costos = costoService.obtenerCostosDelMes(empresaId, pageable);
            if (costos.isEmpty()) {
                return ResponseEntity.ok().header("X-Info","No hay costos registrados para el mes actual.").body(costos);
            }
            return ResponseEntity.ok(costos);
        } catch (Exception e) {
                System.out.println("Error al obtener costos del mes: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * BÚSQUEDAS Y FILTROS
     */

    @GetMapping("/buscar")
    @Operation(summary = "Buscar costos", description = "Busca costos por texto en los campos 'descripción' y 'concepto' de cada costo. Ingrese una palabra o frase para encontrar coincidencias en ambos campos.")
    public ResponseEntity<Page<Costo>> buscarCostos(
            @Parameter(description = "Texto de búsqueda") @RequestParam String texto,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var pageable = PageRequest.of(page, size);
            var costos = costoService.buscarCostos(empresaId, texto, pageable);
            if (costos.isEmpty()) {
                return ResponseEntity.ok().header("X-Info","No hay costos registrados para la búsqueda solicitada.").body(costos);
            }
            return ResponseEntity.ok(costos);
        } catch (Exception e) {
            System.out.println("Error al buscar costos: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/filtrar")
    @Hidden
    public ResponseEntity<Page<Costo>> filtrarCostos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(description = "ID de la obra") @RequestParam(required = false) Long obraId,
            @Parameter(description = "Categoría") @RequestParam(required = false) String categoria,
            @Parameter(description = "Tipo") @RequestParam(required = false) String tipo,
            @Parameter(description = "Monto mínimo") @RequestParam(required = false) Double montoMinimo,
            @Parameter(description = "Monto máximo") @RequestParam(required = false) Double montoMaximo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            var pageable = PageRequest.of(page, size);
            var costos = costoService.filtrarCostos(empresaId, obraId, categoria, tipo, montoMinimo, montoMaximo, pageable);
            return ResponseEntity.ok(costos);
        } catch (Exception e) {
            System.out.println("Error al filtrar costos: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ANÁLISIS DE COSTOS
     */

    @GetMapping("/analisis/obra/{obraId}")
    @Operation(summary = "Análisis costos obra", description = "Obtiene análisis detallado de costos por obra")
    public ResponseEntity<Map<String, Object>> obtenerAnalisisCostosObra(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var analisis = costoService.obtenerAnalisisCostosObra(obraId, empresaId);
            return ResponseEntity.ok(analisis);
        } catch (Exception e) {
            System.out.println("Error al obtener análisis costos obra: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analisis/presupuesto-vs-real/{obraId}")
    @Operation(summary = "Presupuesto vs Real", description = "Compara costos presupuestados vs reales")
    public ResponseEntity<Map<String, Object>> compararPresupuestoVsReal(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var comparacion = costoService.compararPresupuestoVsReal(obraId, empresaId);
            return ResponseEntity.ok(comparacion);
        } catch (Exception e) {
            System.out.println("Error al comparar presupuesto vs real: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analisis/rentabilidad/{obraId}")
    @Operation(summary = "Análisis rentabilidad", description = "Calcula la rentabilidad de una obra")
    public ResponseEntity<Map<String, Object>> calcularRentabilidad(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var rentabilidad = costoService.calcularRentabilidad(obraId, empresaId);
            return ResponseEntity.ok(rentabilidad);
        } catch (Exception e) {
            System.out.println("Error al calcular rentabilidad: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ESTADÍSTICAS Y MÉTRICAS
     */

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas generales", description = "Obtiene estadísticas generales de costos")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var estadisticas = costoService.obtenerEstadisticas(empresaId);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            System.out.println("Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/estadisticas/categorias")
    @Operation(summary = "Estadísticas por categoría", description = "Obtiene distribución de costos por categoría")
    public ResponseEntity<List<Map<String, Object>>> obtenerEstadisticasPorCategoria(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var estadisticas = costoService.obtenerEstadisticasPorCategoria(empresaId);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            System.out.println("Error al obtener estadísticas por categoría: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/estadisticas/obras")
    @Operation(summary = "Estadísticas por obra", description = "Obtiene estadísticas de costos por obra")
    public ResponseEntity<com.rodrigo.construccion.dto.response.ListaConMensajeResponse<Map<String, Object>>> obtenerEstadisticasPorObra(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var estadisticas = costoService.obtenerEstadisticasPorObra(empresaId);
            if (estadisticas == null || estadisticas.isEmpty()) {
                return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(estadisticas, "No hay resultados para la consulta solicitada."));
            }
            return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(estadisticas));
        } catch (Exception e) {
            // System.out.println("Error al obtener estadísticas por obra: " + e.getMessage());
            return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(List.of(), "Ocurrió un error interno al procesar la solicitud."));
        }
    }

    @GetMapping("/estadisticas/mensuales")
    @Operation(summary = "Estadísticas mensuales", description = "Obtiene evolución mensual de costos")
    public ResponseEntity<List<Map<String, Object>>> obtenerEstadisticasMensuales(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var estadisticas = costoService.obtenerEstadisticasMensuales(empresaId);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            System.out.println("Error al obtener estadísticas mensuales: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * REPORTES
     */

    @GetMapping("/reporte/resumen-obras")
    @Operation(summary = "Reporte resumen obras", description = "Obtiene reporte resumido de costos por obra")
    public ResponseEntity<com.rodrigo.construccion.dto.response.ListaConMensajeResponse<Map<String, Object>>> obtenerReporteResumenObras(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var reporte = costoService.obtenerReporteResumenObras(empresaId);
            if (reporte == null || reporte.isEmpty()) {
                return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(reporte, "No hay resultados para la consulta solicitada."));
            }
            return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(reporte));
        } catch (Exception e) {
            // System.out.println("Error al obtener reporte resumen obras: " + e.getMessage());
            return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(List.of(), "Ocurrió un error interno al procesar la solicitud."));
        }
    }

    @GetMapping("/reporte/variaciones-presupuesto")
    @Operation(summary = "Reporte variaciones", description = "Obtiene reporte de variaciones presupuestarias")
    public ResponseEntity<com.rodrigo.construccion.dto.response.ListaConMensajeResponse<Map<String, Object>>> obtenerReporteVariacionesPresupuesto(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var reporte = costoService.obtenerReporteVariacionesPresupuesto(empresaId);
            if (reporte == null || reporte.isEmpty()) {
                return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(reporte, "No hay resultados para la consulta solicitada."));
            }
            return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(reporte));
        } catch (Exception e) {
            System.out.println("Error al obtener reporte variaciones: " + e.getMessage());
            return ResponseEntity.status(500).body(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(List.of(), "Error interno al obtener el reporte de variaciones."));
        }
    }

    @GetMapping("/reporte/top-costos")
    @Operation(summary = "Top costos", description = "Obtiene los costos más altos del período")
        public ResponseEntity<com.rodrigo.construccion.dto.response.ListaConMensajeResponse<Map<String, Object>>> obtenerTopCostos(
                @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                @Parameter(description = "Cantidad de registros") @RequestParam(defaultValue = "10") int limite) {
            try {
                var reporte = costoService.obtenerTopCostos(empresaId, limite);
                if (reporte == null || reporte.isEmpty()) {
                    return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(reporte, "No hay resultados para la consulta solicitada."));
                }
                return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(reporte));
            } catch (Exception e) {
                System.out.println("Error al obtener top costos: " + e.getMessage());
                return ResponseEntity.ok(new com.rodrigo.construccion.dto.response.ListaConMensajeResponse<>(List.of(), "Ocurrió un error interno al procesar la solicitud."));
            }
        }

    /**
     * OPERACIONES ESPECIALES
     */

    @PostMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar costo", description = "Aprueba un costo registrado")
    public ResponseEntity<Costo> aprobarCosto(
            @Parameter(description = "ID del costo") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(description = "Comentarios de aprobación") @RequestParam(required = false) String comentarios) {
        try {
            var costo = costoService.aprobarCosto(id, comentarios, empresaId);
            return ResponseEntity.ok(costo);
        } catch (IllegalArgumentException e) {
            System.out.println("Error al aprobar costo: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar costo", description = "Rechaza un costo registrado")
    public ResponseEntity<Costo> rechazarCosto(
            @Parameter(description = "ID del costo") @PathVariable Long id,
            @Parameter(description = "Motivo del rechazo") @RequestParam String motivoRechazo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var costo = costoService.rechazarCosto(id, motivoRechazo, empresaId);
            return ResponseEntity.ok(costo);
        } catch (IllegalArgumentException e) {
            System.out.println("Error al rechazar costo: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorías", description = "Obtiene lista de categorías de costos disponibles")
    public ResponseEntity<List<String>> listarCategorias(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var categorias = costoService.listarCategorias(empresaId);
            return ResponseEntity.ok(categorias);
        } catch (Exception e) {
            System.out.println("Error al listar categorías: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tipos")
    @Operation(summary = "Listar tipos", description = "Obtiene lista de tipos de costos disponibles")
    public ResponseEntity<List<String>> listarTipos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var tipos = costoService.listarTipos(empresaId);
            return ResponseEntity.ok(tipos);
        } catch (Exception e) {
            System.out.println("Error al listar tipos: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/contar")
    @Operation(summary = "Contar costos", description = "Obtiene el número total de costos")
    public ResponseEntity<Long> contarCostos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        try {
            var total = costoService.contarCostosPorEmpresa(empresaId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            System.out.println("Error al contar costos: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}