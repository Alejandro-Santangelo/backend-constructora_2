package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.CajaChicaMovimientoDTO;
import com.rodrigo.construccion.dto.response.SaldoCajaChicaDTO;
import com.rodrigo.construccion.model.entity.CajaChicaMovimiento;
import com.rodrigo.construccion.service.CajaChicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controller REST para gestión de caja chica
 * Endpoints para asignar, gastar y consultar movimientos de caja chica
 */
@RestController
@RequestMapping("/v1/caja-chica")
@Tag(name = "Caja Chica", description = "Gestión de asignaciones y gastos de caja chica por profesional")
@Slf4j
public class CajaChicaController {

    @Autowired
    private CajaChicaService cajaChicaService;

    /**
     * POST /api/v1/caja-chica/asignar
     * Asignar caja chica a un profesional
     */
    @PostMapping("/asignar")
    @Operation(
        summary = "Asignar caja chica a un profesional",
        description = "Registra una asignación de dinero de caja chica a un profesional. " +
                     "Guarda en tabla relacional (principal) y en JSONB (backup). " +
                     "Respeta el filtrado multi-tenant automáticamente."
    )
    public ResponseEntity<CajaChicaMovimiento> asignar(
        @Valid @RequestBody CajaChicaMovimientoDTO dto,
        @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
        @RequestParam Long empresaId
    ) {
        log.info("POST /api/v1/caja-chica/asignar - empresaId: {}, profesional: {}, monto: {}", 
            empresaId, dto.getProfesionalNombre(), dto.getMonto());
        CajaChicaMovimiento movimiento = cajaChicaService.asignarCajaChica(dto, empresaId);
        return ResponseEntity.ok(movimiento);
    }

    /**
     * POST /api/v1/caja-chica/registrar-gasto
     * Registrar un gasto de caja chica
     */
    @PostMapping("/registrar-gasto")
    @Operation(
        summary = "Registrar un gasto de caja chica",
        description = "Registra un gasto realizado por un profesional con su caja chica. " +
                     "Valida que el profesional tenga saldo suficiente antes de registrar. " +
                     "Actualiza automáticamente el saldo disponible."
    )
    public ResponseEntity<CajaChicaMovimiento> registrarGasto(
        @Valid @RequestBody CajaChicaMovimientoDTO dto,
        @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
        @RequestParam Long empresaId
    ) {
        log.info("POST /api/v1/caja-chica/registrar-gasto - empresaId: {}, profesional: {}, monto: {}", 
            empresaId, dto.getProfesionalNombre(), dto.getMonto());
        
        try {
            CajaChicaMovimiento movimiento = cajaChicaService.registrarGasto(dto, empresaId);
            return ResponseEntity.ok(movimiento);
        } catch (IllegalArgumentException e) {
            log.error("Error al registrar gasto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/v1/caja-chica/saldo
     * Consultar saldo de un profesional
     */
    @GetMapping("/saldo")
    @Operation(
        summary = "Consultar saldo de un profesional",
        description = "Obtiene el saldo actual de caja chica de un profesional. " +
                     "Incluye total asignado, total gastado y saldo disponible. " +
                     "Cálculo: saldo = total_asignado - total_gastado"
    )
    public ResponseEntity<SaldoCajaChicaDTO> consultarSaldo(
        @Parameter(description = "ID del presupuesto", required = true)
        @RequestParam Long presupuestoId,
        @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
        @RequestParam Long empresaId,
        @Parameter(description = "Nombre completo del profesional", required = true, example = "Ruben García")
        @RequestParam String profesionalNombre,
        @Parameter(description = "Tipo de profesional", required = true, example = "Oficial")
        @RequestParam String profesionalTipo
    ) {
        log.info("GET /api/v1/caja-chica/saldo - presupuesto: {}, profesional: {} ({})", 
            presupuestoId, profesionalNombre, profesionalTipo);
        SaldoCajaChicaDTO saldo = cajaChicaService.consultarSaldo(
            presupuestoId, empresaId, profesionalNombre, profesionalTipo
        );
        return ResponseEntity.ok(saldo);
    }

    /**
     * GET /api/v1/caja-chica/movimientos
     * Listar todos los movimientos de un presupuesto
     */
    @GetMapping("/movimientos")
    @Operation(
        summary = "Listar todos los movimientos de caja chica de un presupuesto",
        description = "Obtiene el historial completo de movimientos (asignaciones y gastos) " +
                     "de todos los profesionales de un presupuesto. " +
                     "Ordenados por fecha descendente (más recientes primero)."
    )
    public ResponseEntity<List<CajaChicaMovimiento>> listarMovimientos(
        @Parameter(description = "ID del presupuesto", required = true)
        @RequestParam Long presupuestoId,
        @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
        @RequestParam Long empresaId
    ) {
        log.info("GET /api/v1/caja-chica/movimientos - presupuesto: {}, empresa: {}", presupuestoId, empresaId);
        List<CajaChicaMovimiento> movimientos = cajaChicaService.listarMovimientos(presupuestoId, empresaId);
        return ResponseEntity.ok(movimientos);
    }

    /**
     * GET /api/v1/caja-chica/movimientos/profesional
     * Listar movimientos de un profesional específico
     */
    @GetMapping("/movimientos/profesional")
    @Operation(
        summary = "Listar movimientos de un profesional específico",
        description = "Obtiene el historial de movimientos (asignaciones y gastos) " +
                     "de un profesional en particular. " +
                     "Útil para auditoría y seguimiento individual."
    )
    public ResponseEntity<List<CajaChicaMovimiento>> listarMovimientosProfesional(
        @Parameter(description = "ID del presupuesto", required = true)
        @RequestParam Long presupuestoId,
        @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
        @RequestParam Long empresaId,
        @Parameter(description = "Nombre completo del profesional", required = true, example = "Ruben García")
        @RequestParam String profesionalNombre,
        @Parameter(description = "Tipo de profesional", required = true, example = "Oficial")
        @RequestParam String profesionalTipo
    ) {
        log.info("GET /api/v1/caja-chica/movimientos/profesional - presupuesto: {}, profesional: {} ({})", 
            presupuestoId, profesionalNombre, profesionalTipo);
        List<CajaChicaMovimiento> movimientos = cajaChicaService.listarMovimientosProfesional(
            presupuestoId, empresaId, profesionalNombre, profesionalTipo
        );
        return ResponseEntity.ok(movimientos);
    }
}
