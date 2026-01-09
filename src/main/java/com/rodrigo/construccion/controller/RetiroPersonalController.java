package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.RetiroPersonalRequestDTO;
import com.rodrigo.construccion.dto.response.RetiroPersonalResponseDTO;
import com.rodrigo.construccion.dto.response.SaldoDisponibleResponseDTO;
import com.rodrigo.construccion.dto.response.TotalesRetirosResponseDTO;
import com.rodrigo.construccion.service.RetiroPersonalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller para gestión de retiros personales del propietario
 */
@RestController
@RequestMapping("/api/v1/retiros-personales")
@RequiredArgsConstructor
public class RetiroPersonalController {

    private final RetiroPersonalService retiroPersonalService;

    /**
     * POST /api/v1/retiros-personales
     * Registrar un nuevo retiro
     */
    @PostMapping
    public ResponseEntity<RetiroPersonalResponseDTO> registrarRetiro(
            @Valid @RequestBody RetiroPersonalRequestDTO request) {
        RetiroPersonalResponseDTO response = retiroPersonalService.registrarRetiro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/retiros-personales
     * Listar retiros con filtros opcionales
     * 
     * Query Params:
     * - empresaId (requerido)
     * - fechaDesde (opcional, formato: yyyy-MM-dd)
     * - fechaHasta (opcional, formato: yyyy-MM-dd)
     * - tipoRetiro (opcional: GANANCIA|PRESTAMO|GASTO_PERSONAL)
     * - estado (opcional: ACTIVO|ANULADO)
     */
    @GetMapping
    public ResponseEntity<List<RetiroPersonalResponseDTO>> listarRetiros(
            @RequestParam Long empresaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String tipoRetiro,
            @RequestParam(required = false) String estado) {
        
        List<RetiroPersonalResponseDTO> retiros = retiroPersonalService.listarRetiros(
            empresaId, fechaDesde, fechaHasta, tipoRetiro, estado
        );
        
        return ResponseEntity.ok(retiros);
    }

    /**
     * GET /api/v1/retiros-personales/{id}
     * Obtener un retiro específico
     */
    @GetMapping("/{id}")
    public ResponseEntity<RetiroPersonalResponseDTO> obtenerRetiro(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        RetiroPersonalResponseDTO retiro = retiroPersonalService.obtenerRetiro(id, empresaId);
        return ResponseEntity.ok(retiro);
    }

    /**
     * PUT /api/v1/retiros-personales/{id}/anular
     * Anular un retiro (cambiar estado a ANULADO)
     * El monto vuelve a estar disponible para nuevos retiros
     */
    @PutMapping("/{id}/anular")
    public ResponseEntity<RetiroPersonalResponseDTO> anularRetiro(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        RetiroPersonalResponseDTO retiro = retiroPersonalService.anularRetiro(id, empresaId);
        return ResponseEntity.ok(retiro);
    }

    /**
     * DELETE /api/v1/retiros-personales/{id}
     * Eliminar físicamente un retiro
     * Solo se pueden eliminar retiros en estado ACTIVO
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRetiro(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        retiroPersonalService.eliminarRetiro(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/retiros-personales/totales
     * Obtener totales de retiros
     * 
     * Response incluye:
     * - Total general de retiros
     * - Cantidad de retiros
     * - Totales por tipo (GANANCIA, PRESTAMO, GASTO_PERSONAL)
     * - Totales por mes
     */
    @GetMapping("/totales")
    public ResponseEntity<TotalesRetirosResponseDTO> obtenerTotales(
            @RequestParam Long empresaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        
        TotalesRetirosResponseDTO totales = retiroPersonalService.obtenerTotales(
            empresaId, fechaDesde, fechaHasta
        );
        
        return ResponseEntity.ok(totales);
    }

    /**
     * GET /api/v1/retiros-personales/saldo-disponible
     * Calcular saldo disponible para retiros
     * 
     * ENDPOINT CRÍTICO
     * 
     * Fórmula: totalCobrado - totalAsignado - totalRetirado
     * 
     * Response incluye:
     * - Saldo disponible
     * - Totales de cobros, asignaciones y retiros
     * - Desglose detallado por estados
     */
    @GetMapping("/saldo-disponible")
    public ResponseEntity<SaldoDisponibleResponseDTO> obtenerSaldoDisponible(
            @RequestParam Long empresaId) {
        SaldoDisponibleResponseDTO saldo = retiroPersonalService.obtenerSaldoDisponibleCompleto(empresaId);
        return ResponseEntity.ok(saldo);
    }
}
