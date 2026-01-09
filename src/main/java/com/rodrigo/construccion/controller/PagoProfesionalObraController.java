package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.PagoProfesionalObraRequestDTO;
import com.rodrigo.construccion.dto.response.PagoProfesionalObraResponseDTO;
import com.rodrigo.construccion.service.IPagoProfesionalObraService;

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
@RequestMapping("/api/v1/pagos-profesional-obra")
@RequiredArgsConstructor
public class PagoProfesionalObraController {

    private final IPagoProfesionalObraService pagoService;

    /**
     * GET /api/v1/pagos-profesional-obra?empresaId=1
     * Listar TODOS los pagos de una empresa
     */
    @GetMapping
    public ResponseEntity<List<PagoProfesionalObraResponseDTO>> listarTodosPorEmpresa(
            @RequestHeader(value = "empresaId", required = false) Long empresaIdHeader,
            @RequestParam(value = "empresaId", required = false) Long empresaIdParam) {
        
        // Priorizar header, si no existe usar param
        Long empresaId = empresaIdHeader != null ? empresaIdHeader : empresaIdParam;
        
        if (empresaId == null) {
            throw new RuntimeException("El parámetro empresaId es obligatorio (header o query param)");
        }
        
        List<PagoProfesionalObraResponseDTO> pagos = pagoService.listarTodosPorEmpresa(empresaId);
        return ResponseEntity.ok(pagos);
    }

    /**
     * POST /api/v1/pagos-profesional-obra
     * Crear un nuevo pago a profesional
     */
    @PostMapping
    public ResponseEntity<PagoProfesionalObraResponseDTO> crearPago(
            @RequestParam(required = false) Long empresaId,
            @Valid @RequestBody PagoProfesionalObraRequestDTO request) {
        
        // Si empresaId viene en el body del request, usarlo (prioridad al body)
        if (request.getEmpresaId() != null) {
            empresaId = request.getEmpresaId();
        }
        // Si empresaId aún es null, usar el del @RequestParam
        // Si ambos son null, la validación del DTO fallará con mensaje claro
        if (empresaId != null && request.getEmpresaId() == null) {
            request.setEmpresaId(empresaId);
        }
        
        PagoProfesionalObraResponseDTO response = pagoService.crearPago(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * PUT /api/v1/pagos-profesional-obra/{id}
     * Actualizar un pago existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<PagoProfesionalObraResponseDTO> actualizarPago(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @Valid @RequestBody PagoProfesionalObraRequestDTO request) {
        PagoProfesionalObraResponseDTO response = pagoService.actualizarPago(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/{id}
     * Obtener un pago por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PagoProfesionalObraResponseDTO> obtenerPagoPorId(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        PagoProfesionalObraResponseDTO response = pagoService.obtenerPagoPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/profesional/{profesionalObraId}
     * GET /api/v1/pagos-profesional-obra/profesional-obra/{profesionalObraId}
     * Obtener todos los pagos de un profesional en una obra
     */
    @GetMapping({"/profesional/{profesionalObraId}", "/profesional-obra/{profesionalObraId}"})
    public ResponseEntity<List<PagoProfesionalObraResponseDTO>> obtenerPagosPorProfesional(
            @PathVariable Long profesionalObraId,
            @RequestParam Long empresaId) {
        List<PagoProfesionalObraResponseDTO> pagos = pagoService.obtenerPagosPorProfesional(profesionalObraId);
        return ResponseEntity.ok(pagos);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/profesional/{profesionalObraId}/tipo/{tipoPago}
     * Obtener pagos por tipo (SEMANAL, ADELANTO, PREMIO, BONO)
     */
    @GetMapping("/profesional/{profesionalObraId}/tipo/{tipoPago}")
    public ResponseEntity<List<PagoProfesionalObraResponseDTO>> obtenerPagosPorTipo(
            @PathVariable Long profesionalObraId,
            @PathVariable String tipoPago,
            @RequestParam Long empresaId) {
        List<PagoProfesionalObraResponseDTO> pagos = pagoService.obtenerPagosPorTipo(profesionalObraId, tipoPago);
        return ResponseEntity.ok(pagos);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/profesional/{profesionalObraId}/adelantos
     * Obtener adelantos de un profesional
     */
    @GetMapping("/profesional/{profesionalObraId}/adelantos")
    public ResponseEntity<List<PagoProfesionalObraResponseDTO>> obtenerAdelantos(
            @PathVariable Long profesionalObraId,
            @RequestParam Long empresaId) {
        List<PagoProfesionalObraResponseDTO> adelantos = pagoService.obtenerAdelantos(profesionalObraId);
        return ResponseEntity.ok(adelantos);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/profesional/{profesionalObraId}/adelantos-pendientes
     * Calcular adelantos pendientes de descontar
     */
    @GetMapping("/profesional/{profesionalObraId}/adelantos-pendientes")
    public ResponseEntity<BigDecimal> calcularAdelantosPendientes(
            @PathVariable Long profesionalObraId,
            @RequestParam Long empresaId) {
        BigDecimal total = pagoService.calcularAdelantosPendientes(profesionalObraId);
        return ResponseEntity.ok(total);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/profesional/{profesionalObraId}/total-pagado
     * Calcular total pagado a un profesional
     */
    @GetMapping("/profesional/{profesionalObraId}/total-pagado")
    public ResponseEntity<BigDecimal> calcularTotalPagado(
            @PathVariable Long profesionalObraId,
            @RequestParam Long empresaId) {
        BigDecimal total = pagoService.calcularTotalPagado(profesionalObraId);
        return ResponseEntity.ok(total);
    }

    /**
     * PATCH /api/v1/pagos-profesional-obra/{id}/anular
     * Anular un pago con motivo
     */
    @PatchMapping("/{id}/anular")
    public ResponseEntity<PagoProfesionalObraResponseDTO> anularPago(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @RequestParam String motivo) {
        PagoProfesionalObraResponseDTO pago = pagoService.anularPago(id, empresaId, motivo);
        return ResponseEntity.ok(pago);
    }

    /**
     * DELETE /api/v1/pagos-profesional-obra/{id}
     * Eliminar un pago
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPago(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        pagoService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/pagos-profesional-obra/fecha-rango
     * Obtener pagos por rango de fechas
     * Ejemplo: /api/v1/pagos-profesional-obra/fecha-rango?empresaId=1&fechaDesde=2025-01-01&fechaHasta=2025-12-31
     */
    @GetMapping("/fecha-rango")
    public ResponseEntity<List<PagoProfesionalObraResponseDTO>> obtenerPagosPorFechas(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta) {
        List<PagoProfesionalObraResponseDTO> pagos = pagoService.obtenerPagosPorFechas(fechaDesde, fechaHasta);
        return ResponseEntity.ok(pagos);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/profesional/{profesionalObraId}/existe-semanal
     * Verificar si existe pago semanal para un período
     * Ejemplo: /api/v1/pagos-profesional-obra/profesional/1/existe-semanal?empresaId=1&desde=2025-01-01&hasta=2025-01-07
     */
    @GetMapping("/profesional/{profesionalObraId}/existe-semanal")
    public ResponseEntity<Boolean> existePagoSemanalEnPeriodo(
            @PathVariable Long profesionalObraId,
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        boolean existe = pagoService.existePagoSemanalEnPeriodo(profesionalObraId, desde, hasta);
        return ResponseEntity.ok(existe);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/profesional/{profesionalObraId}/promedio-presentismo
     * Calcular promedio de presentismo de un profesional
     */
    @GetMapping("/profesional/{profesionalObraId}/promedio-presentismo")
    public ResponseEntity<BigDecimal> calcularPromedioPresentismo(
            @PathVariable Long profesionalObraId,
            @RequestParam Long empresaId) {
        BigDecimal promedio = pagoService.calcularPromedioPresentismo(profesionalObraId);
        return ResponseEntity.ok(promedio);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/profesional/{profesionalObraId}/pendientes
     * Obtener pagos pendientes de un profesional
     */
    @GetMapping("/profesional/{profesionalObraId}/pendientes")
    public ResponseEntity<List<PagoProfesionalObraResponseDTO>> obtenerPagosPendientes(
            @PathVariable Long profesionalObraId,
            @RequestParam Long empresaId) {
        List<PagoProfesionalObraResponseDTO> pagos = pagoService.obtenerPagosPendientes(profesionalObraId);
        return ResponseEntity.ok(pagos);
    }

    /**
     * GET /api/v1/pagos-profesional-obra/tipo/{tipoPago}
     * Obtener pagos por tipo para toda la empresa
     */
    @GetMapping("/tipo/{tipoPago}")
    public ResponseEntity<List<PagoProfesionalObraResponseDTO>> obtenerPagosPorTipoEmpresa(
            @PathVariable String tipoPago,
            @RequestParam Long empresaId) {
        List<PagoProfesionalObraResponseDTO> pagos = pagoService.obtenerPagosPorTipoEmpresa(tipoPago);
        return ResponseEntity.ok(pagos);
    }

    /**
     * PATCH /api/v1/pagos-profesional-obra/{id}/marcar-pagado
     * Marcar un pago como pagado con validación de empresa
     */
    @PatchMapping("/{id}/marcar-pagado")
    public ResponseEntity<PagoProfesionalObraResponseDTO> marcarComoPagado(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPago) {
        PagoProfesionalObraResponseDTO pago = pagoService.marcarComoPagado(id, empresaId, fechaPago);
        return ResponseEntity.ok(pago);
    }
}
