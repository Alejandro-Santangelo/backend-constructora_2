package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.PagoConsolidadoRequestDTO;
import com.rodrigo.construccion.dto.response.PagoConsolidadoBatchResponseDTO;
import com.rodrigo.construccion.dto.response.PagoConsolidadoResponseDTO;
import com.rodrigo.construccion.dto.response.TotalesPagosConsolidadosDTO;
import com.rodrigo.construccion.service.PagoConsolidadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller para gestión de pagos consolidados (materiales, gastos generales, otros).
 * 
 * Endpoints:
 * - POST /v1/pagos-consolidados/batch: Registrar múltiples pagos en batch
 * - GET /v1/pagos-consolidados/presupuesto/{id}: Listar pagos por presupuesto
 * - GET /v1/pagos-consolidados/item/{itemId}: Listar pagos por item
 * - GET /v1/pagos-consolidados/{id}: Obtener pago por ID
 * - PUT /v1/pagos-consolidados/{id}/anular: Anular un pago
 * - GET /v1/pagos-consolidados/totales: Calcular totales consolidados
 * - GET /v1/pagos-consolidados/direccion: Buscar pagos por dirección de obra
 */
@RestController
@RequestMapping("/api/v1/pagos-consolidados")
@RequiredArgsConstructor
@Tag(name = "Pagos Consolidados", description = "Gestión de pagos de materiales, gastos generales y otros conceptos")
public class PagoConsolidadoController {

    private final PagoConsolidadoService pagoConsolidadoService;

    /**
     * GET /v1/pagos-consolidados
     * Listar todos los pagos consolidados de una empresa (materiales + gastos generales).
     */
    @GetMapping
    @Operation(summary = "Listar todos los pagos consolidados", 
               description = "Obtiene todos los pagos consolidados (materiales + gastos generales) de una empresa")
    public ResponseEntity<List<PagoConsolidadoResponseDTO>> listarTodosPorEmpresa(
            @RequestHeader(value = "empresaId", required = false) @Parameter(description = "ID de la empresa (header)") Long empresaIdHeader,
            @RequestParam(value = "empresaId", required = false) @Parameter(description = "ID de la empresa (query param)") Long empresaIdParam) {
        
        // Priorizar header, si no existe usar param
        Long empresaId = empresaIdHeader != null ? empresaIdHeader : empresaIdParam;
        
        if (empresaId == null) {
            throw new RuntimeException("El parámetro empresaId es obligatorio");
        }
        
        List<PagoConsolidadoResponseDTO> pagos = pagoConsolidadoService.listarTodosPorEmpresa(empresaId);
        return ResponseEntity.ok(pagos);
    }

    /**
     * POST /v1/pagos-consolidados/batch
     * Registrar múltiples pagos en una sola transacción.
     * Si alguno falla, se hace rollback completo.
     */
    @PostMapping("/batch")
    @Operation(summary = "Registrar pagos en batch", 
               description = "Registra múltiples pagos consolidados en una sola transacción atómica")
    public ResponseEntity<PagoConsolidadoBatchResponseDTO> registrarBatch(
            @Valid @RequestBody List<PagoConsolidadoRequestDTO> requests,
            @RequestParam @Parameter(description = "ID de la empresa") Long empresaId) {
        
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("La lista de pagos no puede estar vacía");
        }

        PagoConsolidadoBatchResponseDTO response = pagoConsolidadoService
            .registrarPagosEnBatch(requests, empresaId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /v1/pagos-consolidados
     * Registrar un pago consolidado individual (materiales, gastos generales, otros).
     */
    @PostMapping
    @Operation(summary = "Registrar pago consolidado individual", 
               description = "Registra un pago consolidado de material, gasto general u otro concepto")
    public ResponseEntity<PagoConsolidadoResponseDTO> registrarPago(
            @Valid @RequestBody PagoConsolidadoRequestDTO request,
            @RequestHeader(value = "empresaId", required = false) @Parameter(description = "ID de la empresa") Long empresaIdHeader) {
        
        // Obtener empresaId del header o del body
        Long empresaId = empresaIdHeader != null ? empresaIdHeader : request.getEmpresaId();
        
        PagoConsolidadoResponseDTO response = pagoConsolidadoService
            .registrarPago(request, empresaId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /v1/pagos-consolidados/presupuesto/{id}
     * Obtener todos los pagos de un presupuesto específico.
     */
    @GetMapping("/presupuesto/{id}")
    @Operation(summary = "Listar pagos por presupuesto", 
               description = "Obtiene todos los pagos consolidados de un presupuesto específico")
    public ResponseEntity<List<PagoConsolidadoResponseDTO>> listarPorPresupuesto(
            @PathVariable @Parameter(description = "ID del presupuesto") Long id,
            @RequestParam @Parameter(description = "ID de la empresa") Long empresaId) {
        
        List<PagoConsolidadoResponseDTO> pagos = pagoConsolidadoService
            .listarPorPresupuesto(id, empresaId);
        
        return ResponseEntity.ok(pagos);
    }

    /**
     * GET /v1/pagos-consolidados/item/{itemId}
     * Obtener todos los pagos de un item de calculadora específico.
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "Listar pagos por item", 
               description = "Obtiene todos los pagos consolidados de un item de calculadora específico")
    public ResponseEntity<List<PagoConsolidadoResponseDTO>> listarPorItem(
            @PathVariable @Parameter(description = "ID del item de calculadora") Long itemId,
            @RequestParam @Parameter(description = "ID de la empresa") Long empresaId) {
        
        List<PagoConsolidadoResponseDTO> pagos = pagoConsolidadoService
            .listarPorItem(itemId, empresaId);
        
        return ResponseEntity.ok(pagos);
    }

    /**
     * GET /v1/pagos-consolidados/{id}
     * Obtener un pago específico por su ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID", 
               description = "Obtiene los detalles de un pago consolidado específico")
    public ResponseEntity<PagoConsolidadoResponseDTO> obtenerPorId(
            @PathVariable @Parameter(description = "ID del pago") Long id,
            @RequestParam @Parameter(description = "ID de la empresa") Long empresaId) {
        
        PagoConsolidadoResponseDTO pago = pagoConsolidadoService.obtenerPorId(id, empresaId);
        return ResponseEntity.ok(pago);
    }

    /**
     * PUT /v1/pagos-consolidados/{id}/anular
     * Anular un pago (cambia estado a ANULADO).
     */
    @PutMapping("/{id}/anular")
    @Operation(summary = "Anular pago", 
               description = "Cambia el estado de un pago a ANULADO")
    public ResponseEntity<PagoConsolidadoResponseDTO> anular(
            @PathVariable @Parameter(description = "ID del pago a anular") Long id,
            @RequestParam @Parameter(description = "ID de la empresa") Long empresaId) {
        
        PagoConsolidadoResponseDTO pago = pagoConsolidadoService.anular(id, empresaId);
        return ResponseEntity.ok(pago);
    }

    /**
     * DELETE /v1/pagos-consolidados/{id}
     * Eliminar un pago consolidado (materiales o gastos generales).
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pago consolidado", 
               description = "Elimina permanentemente un pago de materiales o gastos generales")
    public ResponseEntity<Void> eliminarPago(
            @PathVariable @Parameter(description = "ID del pago a eliminar") Long id,
            @RequestParam @Parameter(description = "ID de la empresa") Long empresaId) {
        
        pagoConsolidadoService.eliminarPago(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /v1/pagos-consolidados/totales
     * Calcular totales consolidados con filtros opcionales.
     * 
     * Filtros disponibles:
     * - presupuestoId: filtrar por presupuesto específico
     * - fechaDesde/fechaHasta: filtrar por rango de fechas
     */
    @GetMapping("/totales")
    @Operation(summary = "Calcular totales consolidados", 
               description = "Calcula totales agrupados por tipo de pago con filtros opcionales")
    public ResponseEntity<TotalesPagosConsolidadosDTO> calcularTotales(
            @RequestParam @Parameter(description = "ID de la empresa") Long empresaId,
            @RequestParam(required = false) @Parameter(description = "ID del presupuesto (opcional)") Long presupuestoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            @Parameter(description = "Fecha desde (opcional, formato: YYYY-MM-DD)") LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            @Parameter(description = "Fecha hasta (opcional, formato: YYYY-MM-DD)") LocalDate fechaHasta) {
        
        TotalesPagosConsolidadosDTO totales = pagoConsolidadoService
            .calcularTotales(empresaId, presupuestoId, fechaDesde, fechaHasta);
        
        return ResponseEntity.ok(totales);
    }

    /**
     * GET /v1/pagos-consolidados/direccion
     * Buscar pagos por dirección de obra (calle + altura).
     */
    @GetMapping("/direccion")
    @Operation(summary = "Buscar pagos por dirección", 
               description = "Busca pagos consolidados según la dirección de obra del presupuesto")
    public ResponseEntity<List<PagoConsolidadoResponseDTO>> buscarPorDireccion(
            @RequestParam @Parameter(description = "Nombre de la calle") String calle,
            @RequestParam @Parameter(description = "Número de altura") String altura,
            @RequestParam @Parameter(description = "ID de la empresa") Long empresaId) {
        
        List<PagoConsolidadoResponseDTO> pagos = pagoConsolidadoService
            .buscarPorDireccion(calle, altura, empresaId);
        
        return ResponseEntity.ok(pagos);
    }
}
