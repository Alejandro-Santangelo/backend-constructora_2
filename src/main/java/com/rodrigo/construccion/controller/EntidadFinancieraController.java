package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.CobroEntidadRequestDTO;
import com.rodrigo.construccion.dto.request.EstadisticasMultiplesRequestDTO;
import com.rodrigo.construccion.dto.request.SincronizarEntidadFinancieraRequestDTO;
import com.rodrigo.construccion.dto.response.CobroEntidadResponseDTO;
import com.rodrigo.construccion.dto.response.EntidadFinancieraResponseDTO;
import com.rodrigo.construccion.dto.response.EstadisticasEntidadResponseDTO;
import com.rodrigo.construccion.service.EntidadFinancieraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Controlador REST para el sistema unificado de entidades financieras.
 *
 * <p>Permite gestionar cobros y estadísticas para CUALQUIER tipo de entidad
 * (obras principales, obras independientes, trabajos extra, trabajos adicionales)
 * sin modificar las tablas existentes.</p>
 *
 * <p>Base path: {@code /api/v1/entidades-financieras}</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/entidades-financieras")
@RequiredArgsConstructor
@Tag(name = "Entidades Financieras",
     description = "Sistema unificado de cobros y estadísticas para obras principales, " +
                   "obras independientes, trabajos extra y trabajos adicionales")
public class EntidadFinancieraController {

    private final EntidadFinancieraService entidadFinancieraService;

    // =========================================================================
    // SINCRONIZACIÓN
    // =========================================================================

    @PostMapping("/sincronizar")
    @Operation(
        summary = "Sincronizar entidad financiera",
        description = "Crea o actualiza el registro de una entidad en el sistema financiero unificado. " +
                      "Idempotente: si ya existe, actualiza; si no, crea. " +
                      "Llamar al crear/actualizar cualquier obra, trabajo extra o trabajo adicional."
    )
    public ResponseEntity<EntidadFinancieraResponseDTO> sincronizar(
            @Valid @RequestBody SincronizarEntidadFinancieraRequestDTO request) {

        log.info("POST /sincronizar - tipo={}, entidadId={}, empresa={}",
                request.getTipoEntidad(), request.getEntidadId(), request.getEmpresaId());

        EntidadFinancieraResponseDTO response = entidadFinancieraService.sincronizar(request);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // COBROS
    // =========================================================================

    @PostMapping("/cobros")
    @Operation(
        summary = "Registrar cobro",
        description = "Registra un cobro contra CUALQUIER tipo de entidad financiera."
    )
    public ResponseEntity<CobroEntidadResponseDTO> registrarCobro(
            @Valid @RequestBody CobroEntidadRequestDTO request) {

        log.info("POST /cobros - entidadFinancieraId={}, monto={}", 
                request.getEntidadFinancieraId(), request.getMonto());

        CobroEntidadResponseDTO response = entidadFinancieraService.registrarCobro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{entidadFinancieraId}/cobros")
    @Operation(
        summary = "Obtener cobros de una entidad",
        description = "Lista todos los cobros registrados para una entidad financiera, ordenados por fecha desc."
    )
    public ResponseEntity<List<CobroEntidadResponseDTO>> obtenerCobros(
            @Parameter(description = "ID de la entidad financiera") 
            @PathVariable Long entidadFinancieraId,
            @Parameter(description = "ID de la empresa (validación de pertenencia)", required = true)
            @RequestParam Long empresaId) {

        List<CobroEntidadResponseDTO> cobros =
                entidadFinancieraService.obtenerCobros(entidadFinancieraId, empresaId);
        return ResponseEntity.ok(cobros);
    }

    @GetMapping("/{entidadFinancieraId}/total-cobrado")
    @Operation(
        summary = "Obtener total cobrado",
        description = "Retorna la suma de todos los cobros de una entidad financiera."
    )
    public ResponseEntity<BigDecimal> obtenerTotalCobrado(
            @PathVariable Long entidadFinancieraId,
            @RequestParam Long empresaId) {

        BigDecimal total = entidadFinancieraService.obtenerTotalCobrado(entidadFinancieraId, empresaId);
        return ResponseEntity.ok(total);
    }

    // =========================================================================
    // ESTADÍSTICAS MÚLTIPLES (endpoint crítico para el frontend)
    // =========================================================================

    @PostMapping("/estadisticas-multiples")
    @Operation(
        summary = "Estadísticas para múltiples entidades (CRÍTICO)",
        description = "Calcula totalCobrado, totalGastos y saldo para una lista de entidades financieras " +
                      "de distintos tipos. El frontend usa este endpoint para mostrar el resumen financiero " +
                      "de una selección mixta de obras principales, obras independientes, trabajos extra " +
                      "y trabajos adicionales sin recibir errores 404."
    )
    public ResponseEntity<List<EstadisticasEntidadResponseDTO>> estadisticasMultiples(
            @Valid @RequestBody EstadisticasMultiplesRequestDTO request) {

        log.info("POST /estadisticas-multiples - {} entidades, empresa={}",
                request.getEntidadesFinancierasIds().size(), request.getEmpresaId());

        List<EstadisticasEntidadResponseDTO> estadisticas =
                entidadFinancieraService.calcularEstadisticasMultiples(request);

        return ResponseEntity.ok(estadisticas);
    }

    // =========================================================================
    // CONSULTAS
    // =========================================================================

    @GetMapping("/{id}")
    @Operation(summary = "Obtener entidad financiera por ID")
    public ResponseEntity<EntidadFinancieraResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam Long empresaId) {

        return ResponseEntity.ok(entidadFinancieraService.obtenerPorId(id, empresaId));
    }

    @GetMapping
    @Operation(
        summary = "Listar entidades financieras de una empresa",
        description = "Retorna todas las entidades financieras activas de la empresa."
    )
    public ResponseEntity<List<EntidadFinancieraResponseDTO>> listar(
            @RequestParam Long empresaId) {

        return ResponseEntity.ok(entidadFinancieraService.listarPorEmpresa(empresaId));
    }

    // =========================================================================
    // MIGRACIÓN / ADMINISTRACIÓN
    // =========================================================================

    @PostMapping("/migrar")
    @Operation(
        summary = "Migrar entidades existentes",
        description = "Puebla entidades_financieras con todas las obras, trabajos extra y trabajos " +
                      "adicionales existentes. Idempotente. Ejecutar una vez al desplegar esta feature. " +
                      "empresaId es opcional: null = migrar todas las empresas."
    )
    public ResponseEntity<Map<String, Object>> migrar(
            @RequestParam(required = false) Long empresaId) {

        log.info("POST /migrar - empresa={}", empresaId != null ? empresaId : "todas");

        int total = entidadFinancieraService.migrarEntidadesExistentes(empresaId);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Migración completada exitosamente",
                "totalProcesados", total,
                "empresaId", empresaId != null ? empresaId : "todas"
        ));
    }
}
