package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.PagoCuentaRequestDTO;
import com.rodrigo.construccion.dto.response.PagoCuentaResponseDTO;
import com.rodrigo.construccion.service.IPagoCuentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Pagos a Cuenta sobre rubros del presupuesto
 * 
 * Permite registrar y consultar pagos parciales sobre items (jornales, materiales, gastos generales)
 * de los rubros definidos en el presupuesto
 */
@RestController
@RequestMapping("/api/pagos-cuenta")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pagos a Cuenta", description = "Gestión de pagos parciales sobre items de rubros del presupuesto")
public class PagoCuentaController {

    private final IPagoCuentaService pagoCuentaService;

    /**
     * ========================================
     * ENDPOINTS CRUD PRINCIPALES
     * ========================================
     */

    @PostMapping
    @Operation(
        summary = "Crear pago a cuenta",
        description = "Registra un pago parcial sobre un item específico (jornales/materiales/gastos) de un rubro del presupuesto. " +
                     "Valida automáticamente que el monto no exceda el saldo pendiente del item. " +
                     "Requiere: presupuestoId, empresaId, nombreRubro, tipoItem y monto."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pago a cuenta creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o monto excede saldo pendiente"),
        @ApiResponse(responseCode = "404", description = "Presupuesto o rubro no encontrado")
    })
    public ResponseEntity<PagoCuentaResponseDTO> crearPagoCuenta(
            @Valid @RequestBody PagoCuentaRequestDTO request) {
        
        log.info("POST /api/pagos-cuenta - Presupuesto: {}, Rubro: {}, Tipo: {}, Monto: {}", 
                request.getPresupuestoId(), request.getNombreRubro(), request.getTipoItem(), request.getMonto());
        
        PagoCuentaResponseDTO pago = pagoCuentaService.crearPagoCuenta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pago);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener pago por ID",
        description = "Retorna los detalles completos de un pago a cuenta específico, " +
                     "incluyendo montos totales, pagado y saldo pendiente del item."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pago encontrado"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
        @ApiResponse(responseCode = "400", description = "El pago no pertenece a la empresa")
    })
    public ResponseEntity<PagoCuentaResponseDTO> obtenerPagoPorId(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id,
            @Parameter(description = "ID de la empresa (validación multi-tenant)", required = true) 
            @RequestParam Long empresaId) {
        
        log.debug("GET /api/pagos-cuenta/{} - Empresa: {}", id, empresaId);
        
        PagoCuentaResponseDTO pago = pagoCuentaService.obtenerPagoPorId(id, empresaId);
        return ResponseEntity.ok(pago);
    }

    @GetMapping
    @Operation(
        summary = "Listar pagos por presupuesto",
        description = "Retorna todos los pagos a cuenta registrados para un presupuesto específico."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pagos retornada exitosamente")
    })
    public ResponseEntity<List<PagoCuentaResponseDTO>> listarPagosPorPresupuesto(
            @Parameter(description = "ID del presupuesto", required = true) 
            @RequestParam Long presupuestoId,
            @Parameter(description = "ID de la empresa", required = true) 
            @RequestParam Long empresaId) {
        
        log.debug("GET /api/pagos-cuenta?presupuestoId={}&empresaId={}", presupuestoId, empresaId);
        
        List<PagoCuentaResponseDTO> pagos = pagoCuentaService.listarPagosPorPresupuesto(presupuestoId, empresaId);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/rubro")
    @Operation(
        summary = "Listar pagos por rubro",
        description = "Retorna todos los pagos a cuenta de un rubro específico del presupuesto."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pagos por rubro retornada")
    })
    public ResponseEntity<List<PagoCuentaResponseDTO>> listarPagosPorRubro(
            @Parameter(description = "ID del presupuesto", required = true) @RequestParam Long presupuestoId,
            @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId,
            @Parameter(description = "Nombre del rubro", required = true, example = "Albañilería") 
            @RequestParam String nombreRubro) {
        
        log.debug("GET /api/pagos-cuenta/rubro?presupuestoId={}&nombreRubro={}", presupuestoId, nombreRubro);
        
        List<PagoCuentaResponseDTO> pagos = pagoCuentaService.listarPagosPorRubro(presupuestoId, empresaId, nombreRubro);
        return ResponseEntity.ok(pagos);
    }

    @GetMapping("/item")
    @Operation(
        summary = "Listar pagos por item específico",
        description = "Retorna todos los pagos a cuenta de un item específico (rubro + tipo de item)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de pagos por item retornada")
    })
    public ResponseEntity<List<PagoCuentaResponseDTO>> listarPagosPorItem(
            @Parameter(description = "ID del presupuesto", required = true) @RequestParam Long presupuestoId,
            @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId,
            @Parameter(description = "Nombre del rubro", required = true, example = "Albañilería") 
            @RequestParam String nombreRubro,
            @Parameter(description = "Tipo de item", required = true, example = "JORNALES") 
            @RequestParam String tipoItem) {
        
        log.debug("GET /api/pagos-cuenta/item?rubro={}&tipo={}", nombreRubro, tipoItem);
        
        List<PagoCuentaResponseDTO> pagos = pagoCuentaService.listarPagosPorItem(
                presupuestoId, empresaId, nombreRubro, tipoItem);
        return ResponseEntity.ok(pagos);
    }

    /**
     * ========================================
     * ENDPOINTS DE RESUMEN Y TOTALES
     * ========================================
     */

    @GetMapping("/resumen")
    @Operation(
        summary = "Obtener resumen de pagos del presupuesto",
        description = "Retorna un resumen completo con totales presupuestados, pagados y pendientes " +
                     "por cada rubro e item del presupuesto."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resumen generado exitosamente")
    })
    public ResponseEntity<Map<String, Map<String, Map<String, BigDecimal>>>> obtenerResumenPagos(
            @Parameter(description = "ID del presupuesto", required = true) @RequestParam Long presupuestoId,
            @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId) {
        
        log.debug("GET /api/pagos-cuenta/resumen?presupuestoId={}", presupuestoId);
        
        Map<String, Map<String, Map<String, BigDecimal>>> resumen = 
                pagoCuentaService.obtenerResumenPagos(presupuestoId, empresaId);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/totales-item")
    @Operation(
        summary = "Calcular totales de un item específico",
        description = "Retorna los totales (presupuestado, pagado, pendiente y % pagado) de un item específico."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Totales calculados exitosamente")
    })
    public ResponseEntity<Map<String, BigDecimal>> calcularTotalesItem(
            @Parameter(description = "ID del presupuesto", required = true) @RequestParam Long presupuestoId,
            @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId,
            @Parameter(description = "Nombre del rubro", required = true, example = "Albañilería") 
            @RequestParam String nombreRubro,
            @Parameter(description = "Tipo de item", required = true, example = "JORNALES") 
            @RequestParam String tipoItem) {
        
        log.debug("GET /api/pagos-cuenta/totales-item?rubro={}&tipo={}", nombreRubro, tipoItem);
        
        Map<String, BigDecimal> totales = pagoCuentaService.calcularTotalesItem(
                presupuestoId, empresaId, nombreRubro, tipoItem);
        return ResponseEntity.ok(totales);
    }

    /**
     * ========================================
     * ENDPOINT DE ELIMINACIÓN
     * ========================================
     */

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar pago a cuenta",
        description = "Elimina un pago a cuenta existente. Útil para corregir errores de registro."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pago eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
        @ApiResponse(responseCode = "400", description = "El pago no pertenece a la empresa")
    })
    public ResponseEntity<Void> eliminarPago(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id,
            @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId) {
        
        log.info("DELETE /api/pagos-cuenta/{} - Empresa: {}", id, empresaId);
        
        pagoCuentaService.eliminarPago(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
