package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.model.entity.MovimientoMaterial;
import com.rodrigo.construccion.service.MovimientoMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Movimientos de Material
 * 
 * Maneja todas las operaciones CRUD para movimientos de inventario.
 * Incluye operaciones especializadas para gestión de stock y trazabilidad.
 */
@RestController
@RequestMapping("/movimientos-material")
@Tag(name = "Movimientos Material", description = "Operaciones de gestión de movimientos de inventario")
@Tag(name = "Movimientos Material", description = "Operaciones de gestión de movimientos de inventario")
public class MovimientoMaterialController {
    private final MovimientoMaterialService movimientoMaterialService;

    public MovimientoMaterialController(MovimientoMaterialService movimientoMaterialService) {
        this.movimientoMaterialService = movimientoMaterialService;
    }

    /**
     * OPERACIONES CRUD BÁSICAS
     */

    @GetMapping
    @Operation(summary = "Listar movimientos", description = "Obtiene todos los movimientos con paginación opcional")
    public ResponseEntity<Page<MovimientoMaterial>> listarMovimientos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        
                System.out.println("Listando movimientos para empresa: " + empresaId + " con paginación: " + pageable);
        Page<MovimientoMaterial> movimientos = movimientoMaterialService.obtenerTodosPaginados(pageable);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento", description = "Obtiene un movimiento específico por su ID")
    public ResponseEntity<MovimientoMaterial> obtenerMovimiento(
            @Parameter(description = "ID del movimiento") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
        System.out.println("Obteniendo movimiento ID: " + id + " para empresa: " + empresaId);
        return movimientoMaterialService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear movimiento", description = "Crea un nuevo movimiento en el sistema")
    public ResponseEntity<MovimientoMaterial> crearMovimiento(
            @RequestBody MovimientoMaterial movimiento,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Creando nuevo movimiento para empresa: " + empresaId);
        MovimientoMaterial movimientoCreado = movimientoMaterialService.crear(movimiento);
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoCreado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento", description = "Actualiza un movimiento existente")
    public ResponseEntity<MovimientoMaterial> actualizarMovimiento(
            @Parameter(description = "ID del movimiento") @PathVariable Long id,
            @RequestBody MovimientoMaterial movimiento,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
            System.out.println("Actualizando movimiento ID: " + id + " para empresa: " + empresaId);
        try {
            MovimientoMaterial movimientoActualizado = movimientoMaterialService.actualizar(id, movimiento);
            return ResponseEntity.ok(movimientoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento", description = "Elimina un movimiento del sistema")
    public ResponseEntity<Void> eliminarMovimiento(
            @Parameter(description = "ID del movimiento") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
            System.out.println("Eliminando movimiento ID: " + id + " para empresa: " + empresaId);
        try {
            movimientoMaterialService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * OPERACIONES ESPECIALIZADAS
     */

    @GetMapping("/por-empresa")
    @Operation(summary = "Movimientos por empresa", description = "Obtiene movimientos de una empresa específica")
    public ResponseEntity<List<MovimientoMaterial>> obtenerMovimientosPorEmpresa(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Obteniendo movimientos para empresa: " + empresaId);
        List<MovimientoMaterial> movimientos = movimientoMaterialService.buscarPorEmpresa(empresaId);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/por-obra")
    @Operation(summary = "Movimientos por obra", description = "Obtiene movimientos de una obra específica")
    public ResponseEntity<List<MovimientoMaterial>> obtenerMovimientosPorObra(
            @Parameter(description = "ID de la obra") @RequestParam Long obraId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Obteniendo movimientos para obra: " + obraId + " de empresa: " + empresaId);
        List<MovimientoMaterial> movimientos = movimientoMaterialService.buscarPorObra(obraId);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/por-tipo")
    @Operation(summary = "Movimientos por tipo", description = "Obtiene movimientos de un tipo específico")
    public ResponseEntity<List<MovimientoMaterial>> obtenerMovimientosPorTipo(
            @Parameter(description = "Tipo de movimiento (ENTRADA, SALIDA, TRANSFERENCIA, AJUSTE)") @RequestParam String tipoMovimiento,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Obteniendo movimientos tipo: " + tipoMovimiento + " para empresa: " + empresaId);
        List<MovimientoMaterial> movimientos = movimientoMaterialService.buscarPorTipo(tipoMovimiento);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/por-fecha")
    @Operation(summary = "Movimientos por rango de fechas", description = "Obtiene movimientos dentro de un rango de fechas")
    public ResponseEntity<List<MovimientoMaterial>> obtenerMovimientosPorRangoFechas(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DDTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DDTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Obteniendo movimientos entre " + fechaInicio + " y " + fechaFin + " para empresa: " + empresaId);
        List<MovimientoMaterial> movimientos = movimientoMaterialService.buscarPorRangoFechasYEmpresa(empresaId, fechaInicio, fechaFin);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/mes-actual")
    @Operation(summary = "Movimientos del mes actual", description = "Obtiene movimientos del mes en curso")
    public ResponseEntity<List<MovimientoMaterial>> obtenerMovimientosMesActual(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Obteniendo movimientos del mes actual para empresa: " + empresaId);
        List<MovimientoMaterial> movimientos = movimientoMaterialService.obtenerMovimientosMesActualPorEmpresa(empresaId);
        return ResponseEntity.ok(movimientos);
    }

    /**
     * OPERACIONES DE ESTADÍSTICAS Y REPORTES
     */

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de movimientos", description = "Obtiene estadísticas generales de movimientos")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Obteniendo estadísticas de movimientos para empresa: " + empresaId);
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalMovimientos", movimientoMaterialService.contarTotal());
        
        // Movimientos del mes actual
        List<MovimientoMaterial> movimientosMes = movimientoMaterialService.obtenerMovimientosMesActual();
        estadisticas.put("movimientosMesActual", movimientosMes.size());
        
        // Valor total del mes
        BigDecimal valorTotalMes = movimientoMaterialService.calcularValorTotal(movimientosMes);
        estadisticas.put("valorTotalMesActual", valorTotalMes);
        
        // Separar por tipos
        long entradas = movimientosMes.stream().mapToLong(m -> m.esEntrada() ? 1 : 0).sum();
        long salidas = movimientosMes.stream().mapToLong(m -> m.esSalida() ? 1 : 0).sum();
        long transferencias = movimientosMes.stream().mapToLong(m -> m.esTransferencia() ? 1 : 0).sum();
        long ajustes = movimientosMes.stream().mapToLong(m -> m.esAjuste() ? 1 : 0).sum();
        
        estadisticas.put("entradasMes", entradas);
        estadisticas.put("salidasMes", salidas);
        estadisticas.put("transferenciasMes", transferencias);
        estadisticas.put("ajustesMes", ajustes);
        estadisticas.put("fechaConsulta", LocalDateTime.now());
        
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/resumen-periodo")
    @Operation(summary = "Resumen por período", description = "Obtiene resumen de movimientos en un período específico")
    public ResponseEntity<Map<String, Object>> obtenerResumenPeriodo(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DDTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DDTHH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Obteniendo resumen de movimientos entre " + fechaInicio + " y " + fechaFin + " para empresa: " + empresaId);
        
        List<MovimientoMaterial> movimientosPeriodo = movimientoMaterialService.buscarPorRangoFechasYEmpresa(empresaId, fechaInicio, fechaFin);
        BigDecimal valorTotal = movimientoMaterialService.calcularValorTotal(movimientosPeriodo);
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("periodo", fechaInicio + " - " + fechaFin);
        resumen.put("cantidadMovimientos", movimientosPeriodo.size());
        resumen.put("valorTotal", valorTotal);
        
        // Separar por tipos
        long entradas = movimientosPeriodo.stream().mapToLong(m -> m.esEntrada() ? 1 : 0).sum();
        long salidas = movimientosPeriodo.stream().mapToLong(m -> m.esSalida() ? 1 : 0).sum();
        long transferencias = movimientosPeriodo.stream().mapToLong(m -> m.esTransferencia() ? 1 : 0).sum();
        long ajustes = movimientosPeriodo.stream().mapToLong(m -> m.esAjuste() ? 1 : 0).sum();
        
        resumen.put("entradas", entradas);
        resumen.put("salidas", salidas);
        resumen.put("transferencias", transferencias);
        resumen.put("ajustes", ajustes);
        
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/todos-simple")
    @Operation(summary = "Lista simple de movimientos", description = "Obtiene todos los movimientos sin paginación")
    public ResponseEntity<List<MovimientoMaterial>> obtenerTodosMovimientos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
              System.out.println("Obteniendo todos los movimientos para empresa: " + empresaId);
        List<MovimientoMaterial> movimientos = movimientoMaterialService.obtenerTodos();
        return ResponseEntity.ok(movimientos);
    }
}