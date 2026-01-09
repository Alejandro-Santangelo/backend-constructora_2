package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.CobroObraRequestDTO;
import com.rodrigo.construccion.dto.response.CobroObraResponseDTO;
import com.rodrigo.construccion.service.ICobroObraService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cobros-obra")
@RequiredArgsConstructor
public class CobroObraController {

    private final ICobroObraService cobroObraService;

    /**
     * GET /api/v1/cobros-obra
     * Obtener todos los cobros de una empresa
     */
    @GetMapping
    public ResponseEntity<List<CobroObraResponseDTO>> obtenerCobrosPorEmpresa(
            @RequestParam Long empresaId) {
        List<CobroObraResponseDTO> cobros = cobroObraService.obtenerCobrosPorEmpresa(empresaId);
        return ResponseEntity.ok(cobros);
    }

    /**
     * POST /api/v1/cobros-obra
     * Crear un nuevo cobro
     */
    @PostMapping
    public ResponseEntity<CobroObraResponseDTO> crearCobro(
            @RequestParam Long empresaId,
            @Valid @RequestBody CobroObraRequestDTO request) {
        request.setEmpresaId(empresaId);
        CobroObraResponseDTO response = cobroObraService.crearCobro(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/cobros-obra/{id}
     * Actualizar un cobro existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<CobroObraResponseDTO> actualizarCobro(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @Valid @RequestBody CobroObraRequestDTO request) {
        request.setEmpresaId(empresaId);
        CobroObraResponseDTO response = cobroObraService.actualizarCobro(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/cobros-obra/{id}
     * Obtener un cobro por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CobroObraResponseDTO> obtenerCobroPorId(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        CobroObraResponseDTO response = cobroObraService.obtenerCobroPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/cobros-obra/obra/{obraId}
     * Obtener todos los cobros de una obra
     */
    @GetMapping("/obra/{obraId}")
    public ResponseEntity<List<CobroObraResponseDTO>> obtenerCobrosPorObra(@PathVariable Long obraId) {
        List<CobroObraResponseDTO> cobros = cobroObraService.obtenerCobrosPorObra(obraId);
        return ResponseEntity.ok(cobros);
    }

    /**
     * GET /api/cobros-obra/obra/{obraId}/pendientes
     * Obtener cobros pendientes de una obra
     */
    @GetMapping("/obra/{obraId}/pendientes")
    public ResponseEntity<List<CobroObraResponseDTO>> obtenerCobrosPendientes(@PathVariable Long obraId) {
        List<CobroObraResponseDTO> cobros = cobroObraService.obtenerCobrosPendientes(obraId);
        return ResponseEntity.ok(cobros);
    }

    /**
     * GET /api/cobros-obra/vencidos
     * Obtener todos los cobros vencidos
     */
    @GetMapping("/vencidos")
    public ResponseEntity<List<CobroObraResponseDTO>> obtenerCobrosVencidos() {
        List<CobroObraResponseDTO> cobros = cobroObraService.obtenerCobrosVencidos();
        return ResponseEntity.ok(cobros);
    }

    /**
     * PATCH /api/cobros-obra/{id}/cobrar
     * Marcar un cobro como cobrado (versión simple - mantener para compatibilidad)
     */
    @PatchMapping("/{id}/cobrar")
    public ResponseEntity<CobroObraResponseDTO> marcarComoCobrado(@PathVariable Long id) {
        CobroObraResponseDTO response = cobroObraService.marcarComoCobrado(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/cobros-obra/{id}/marcar-cobrado
     * Marcar un cobro como cobrado con fecha específica
     */
    @PatchMapping("/{id}/marcar-cobrado")
    public ResponseEntity<CobroObraResponseDTO> marcarComoCobrado(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCobro) {
        CobroObraResponseDTO cobro = cobroObraService.marcarComoCobrado(id, empresaId, fechaCobro);
        return ResponseEntity.ok(cobro);
    }

    /**
     * PATCH /api/cobros-obra/{id}/anular
     * Anular un cobro (versión simple - mantener para compatibilidad)
     */
    @PatchMapping("/{id}/anular")
    public ResponseEntity<Void> anularCobro(@PathVariable Long id) {
        cobroObraService.anularCobro(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/v1/cobros-obra/{id}/anular-cobro
     * Anular un cobro con motivo
     */
    @PatchMapping("/{id}/anular-cobro")
    public ResponseEntity<CobroObraResponseDTO> anularCobro(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @RequestParam String motivo) {
        CobroObraResponseDTO cobro = cobroObraService.anularCobro(id, empresaId, motivo);
        return ResponseEntity.ok(cobro);
    }

    /**
     * DELETE /api/cobros-obra/{id}
     * Eliminar un cobro
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCobro(@PathVariable Long id) {
        cobroObraService.eliminarCobro(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/cobros-obra/obra/{obraId}/total-cobrado
     * Calcular total cobrado de una obra
     */
    @GetMapping("/obra/{obraId}/total-cobrado")
    public ResponseEntity<BigDecimal> calcularTotalCobrado(@PathVariable Long obraId) {
        BigDecimal total = cobroObraService.calcularTotalCobrado(obraId);
        return ResponseEntity.ok(total);
    }

    /**
     * GET /api/cobros-obra/obra/{obraId}/total-pendiente
     * Calcular total pendiente de cobro de una obra
     */
    @GetMapping("/obra/{obraId}/total-pendiente")
    public ResponseEntity<BigDecimal> calcularTotalPendiente(@PathVariable Long obraId) {
        BigDecimal total = cobroObraService.calcularTotalPendiente(obraId);
        return ResponseEntity.ok(total);
    }

    /**
     * GET /api/cobros-obra/{id}/saldo-disponible
     * Calcular saldo disponible de un cobro (para asignaciones futuras)
     */
    @GetMapping("/{id}/saldo-disponible")
    public ResponseEntity<BigDecimal> calcularSaldoDisponible(@PathVariable Long id) {
        BigDecimal saldo = cobroObraService.calcularSaldoDisponible(id);
        return ResponseEntity.ok(saldo);
    }

    /**
     * GET /api/cobros-obra/fechas
     * Obtener cobros por rango de fechas
     * Ejemplo: /api/cobros-obra/fechas?desde=2025-01-01&hasta=2025-12-31
     */
    @GetMapping("/fechas")
    public ResponseEntity<List<CobroObraResponseDTO>> obtenerCobrosPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<CobroObraResponseDTO> cobros = cobroObraService.obtenerCobrosPorFechas(desde, hasta);
        return ResponseEntity.ok(cobros);
    }

    /**
     * POST /api/cobros-obra/actualizar-vencidos
     * Actualizar estado de cobros vencidos
     */
    @PostMapping("/actualizar-vencidos")
    public ResponseEntity<Void> actualizarCobrosVencidos() {
        cobroObraService.actualizarCobrosVencidos();
        return ResponseEntity.ok().build();
    }

    // ========== ENDPOINTS POR DIRECCIÓN ==========

    /**
     * GET /api/v1/cobros-obra/direccion
     * Obtener cobros por dirección completa
     * Ejemplo: /api/v1/cobros-obra/direccion?empresaId=1&presupuestoNoClienteId=1&calle=San Martin&altura=123&barrio=Centro
     */
    @GetMapping("/direccion")
    public ResponseEntity<List<CobroObraResponseDTO>> obtenerCobrosPorDireccion(
            @RequestParam Long empresaId,
            @RequestParam Long presupuestoNoClienteId,
            @RequestParam String calle,
            @RequestParam String altura,
            @RequestParam(required = false) String barrio,
            @RequestParam(required = false) String torre,
            @RequestParam(required = false) String piso,
            @RequestParam(required = false) String depto) {
        List<CobroObraResponseDTO> cobros = cobroObraService.obtenerCobrosPorDireccion(
                presupuestoNoClienteId, calle, altura, barrio, torre, piso, depto);
        return ResponseEntity.ok(cobros);
    }

    /**
     * GET /api/v1/cobros-obra/direccion/pendientes
     * Obtener cobros pendientes por dirección completa
     */
    @GetMapping("/direccion/pendientes")
    public ResponseEntity<List<CobroObraResponseDTO>> obtenerCobrosPendientesPorDireccion(
            @RequestParam Long empresaId,
            @RequestParam Long presupuestoNoClienteId,
            @RequestParam String calle,
            @RequestParam String altura,
            @RequestParam(required = false) String barrio,
            @RequestParam(required = false) String torre,
            @RequestParam(required = false) String piso,
            @RequestParam(required = false) String depto) {
        List<CobroObraResponseDTO> cobros = cobroObraService.obtenerCobrosPendientesPorDireccion(
                presupuestoNoClienteId, calle, altura, barrio, torre, piso, depto);
        return ResponseEntity.ok(cobros);
    }

    /**
     * GET /api/v1/cobros-obra/direccion/total-cobrado
     * Calcular total cobrado por dirección
     */
    @GetMapping("/direccion/total-cobrado")
    public ResponseEntity<BigDecimal> calcularTotalCobradoPorDireccion(
            @RequestParam Long empresaId,
            @RequestParam Long presupuestoNoClienteId,
            @RequestParam String calle,
            @RequestParam String altura,
            @RequestParam(required = false) String barrio,
            @RequestParam(required = false) String torre,
            @RequestParam(required = false) String piso,
            @RequestParam(required = false) String depto) {
        BigDecimal total = cobroObraService.calcularTotalCobradoPorDireccion(
                presupuestoNoClienteId, calle, altura, barrio, torre, piso, depto);
        return ResponseEntity.ok(total);
    }

    /**
     * GET /api/v1/cobros-obra/direccion/total-pendiente
     * Calcular total pendiente por dirección
     */
    @GetMapping("/direccion/total-pendiente")
    public ResponseEntity<BigDecimal> calcularTotalPendientePorDireccion(
            @RequestParam Long empresaId,
            @RequestParam Long presupuestoNoClienteId,
            @RequestParam String calle,
            @RequestParam String altura,
            @RequestParam(required = false) String barrio,
            @RequestParam(required = false) String torre,
            @RequestParam(required = false) String piso,
            @RequestParam(required = false) String depto) {
        BigDecimal total = cobroObraService.calcularTotalPendientePorDireccion(
                presupuestoNoClienteId, calle, altura, barrio, torre, piso, depto);
        return ResponseEntity.ok(total);
    }

    /**
     * PATCH /api/cobros-obra/{id}/marcar-vencido
     * Marcar un cobro como vencido manualmente (versión simple - mantener para compatibilidad)
     */
    @PatchMapping("/{id}/marcar-vencido")
    public ResponseEntity<CobroObraResponseDTO> marcarComoVencido(@PathVariable Long id) {
        CobroObraResponseDTO response = cobroObraService.marcarComoVencido(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/cobros-obra/{id}/marcar-vencido-empresa
     * Marcar un cobro como vencido validando empresa
     */
    @PatchMapping("/{id}/marcar-vencido-empresa")
    public ResponseEntity<CobroObraResponseDTO> marcarComoVencido(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        CobroObraResponseDTO cobro = cobroObraService.marcarComoVencido(id, empresaId);
        return ResponseEntity.ok(cobro);
    }
}
