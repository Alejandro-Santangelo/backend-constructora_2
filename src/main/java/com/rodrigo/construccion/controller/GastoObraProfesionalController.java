package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.RegistrarGastoRequest;
import com.rodrigo.construccion.dto.response.GastoObraProfesionalResponse;
import com.rodrigo.construccion.service.GastoObraProfesionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar gastos de obra profesional con caja chica
 */
@RestController
@RequestMapping("/v1/gastos-obra-profesional")
@RequiredArgsConstructor
@Tag(name = "Gastos Obra Profesional", description = "Gestión de gastos de caja chica de profesionales en obras")
public class GastoObraProfesionalController {

    private final GastoObraProfesionalService gastoService;

    /**
     * Registrar un nuevo gasto de caja chica
     */
    @PostMapping
    @Operation(
        summary = "Registrar gasto de caja chica",
        description = "Registra un nuevo gasto del profesional usando caja chica. " +
                     "Valida saldo disponible, descuenta del saldo y crea automáticamente " +
                     "un registro en Otros Costos del presupuesto de la obra."
    )
    public ResponseEntity<GastoObraProfesionalResponse> registrarGasto(
            @Valid @RequestBody RegistrarGastoRequest request) {
        try {
            GastoObraProfesionalResponse response = gastoService.registrarGasto(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (com.rodrigo.construccion.exception.SaldoInsuficienteException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Listar gastos de un profesional
     */
    @GetMapping("/profesional/{profesionalObraId}")
    @Operation(
        summary = "Listar gastos por profesional",
        description = "Obtiene todos los gastos registrados de un profesional específico"
    )
    public ResponseEntity<List<GastoObraProfesionalResponse>> listarGastosPorProfesional(
            @Parameter(description = "ID de la asignación profesional-obra", required = true)
            @PathVariable Long profesionalObraId,
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        List<GastoObraProfesionalResponse> gastos = gastoService.listarGastosPorProfesional(
            profesionalObraId, empresaId
        );
        return ResponseEntity.ok(gastos);
    }

    /**
     * Listar gastos de una obra (por dirección)
     */
    @GetMapping("/obra")
    @Operation(
        summary = "Listar gastos por obra (dirección)",
        description = "Obtiene todos los gastos registrados en una obra específica " +
                     "identificada por sus 4 campos de dirección"
    )
    public ResponseEntity<List<GastoObraProfesionalResponse>> listarGastosPorObra(
            @Parameter(description = "Calle de la obra", required = true)
            @RequestParam String direccionObraCalle,
            @Parameter(description = "Altura de la obra", required = true)
            @RequestParam String direccionObraAltura,
            @Parameter(description = "Piso de la obra")
            @RequestParam(required = false) String direccionObraPiso,
            @Parameter(description = "Departamento de la obra")
            @RequestParam(required = false) String direccionObraDepartamento,
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        List<GastoObraProfesionalResponse> gastos = gastoService.listarGastosPorObra(
            direccionObraCalle,
            direccionObraAltura,
            direccionObraPiso,
            direccionObraDepartamento,
            empresaId
        );
        return ResponseEntity.ok(gastos);
    }

    /**
     * Obtener detalle de un gasto específico
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener detalle de gasto",
        description = "Obtiene el detalle completo de un gasto específico incluyendo foto del ticket"
    )
    public ResponseEntity<GastoObraProfesionalResponse> obtenerGasto(
            @Parameter(description = "ID del gasto", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la empresa", required = true)
            @RequestParam Long empresaId) {
        try {
            GastoObraProfesionalResponse gasto = gastoService.obtenerGasto(id, empresaId);
            return ResponseEntity.ok(gasto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
