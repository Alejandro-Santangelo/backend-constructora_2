package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.RegistrarGastoRequest;
import com.rodrigo.construccion.dto.response.GastoObraProfesionalResponse;
import com.rodrigo.construccion.service.IGastoObraProfesionalService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/gastos-obra-profesional")
@RequiredArgsConstructor
public class GastoObraProfesionalController {

    private final IGastoObraProfesionalService gastoService;

    /* Registrar un nuevo gasto de caja chica */
    @PostMapping
    public ResponseEntity<GastoObraProfesionalResponse> registrarGasto(@Valid @RequestBody RegistrarGastoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gastoService.registrarGasto(request));
    }

    /* Listar gastos de un profesional */
    @GetMapping("/profesional/{profesionalObraId}")
    public ResponseEntity<List<GastoObraProfesionalResponse>> listarGastosPorProfesional(@Parameter(description = "ID de la asignación profesional-obra", required = true)
                                                                                         @PathVariable Long profesionalObraId,
                                                                                         @Parameter(description = "ID de la empresa", required = true)
                                                                                         @RequestParam Long empresaId) {
        return ResponseEntity.ok(gastoService.listarGastosPorProfesional(profesionalObraId, empresaId));
    }

    /* Obtener detalle de un gasto específico */
    @GetMapping("/{id}")
    public ResponseEntity<GastoObraProfesionalResponse> obtenerGasto(@Parameter(description = "ID del gasto", required = true)
                                                                     @PathVariable Long id,
                                                                     @Parameter(description = "ID de la empresa", required = true)
                                                                     @RequestParam Long empresaId) {
        return ResponseEntity.ok(gastoService.obtenerGasto(id, empresaId));
    }

    /* ESTE METODO NO ESTÁ SIENDO USADO POR EL FRONTEND */
    /* Listar gastos de una obra (por dirección) */
    @GetMapping("/obra")
    public ResponseEntity<List<GastoObraProfesionalResponse>> listarGastosPorObra(@Parameter(description = "Calle de la obra", required = true)
                                                                                  @RequestParam String direccionObraCalle,
                                                                                  @Parameter(description = "Altura de la obra", required = true)
                                                                                  @RequestParam String direccionObraAltura,
                                                                                  @Parameter(description = "Piso de la obra")
                                                                                  @RequestParam(required = false) String direccionObraPiso,
                                                                                  @Parameter(description = "Departamento de la obra")
                                                                                  @RequestParam(required = false) String direccionObraDepartamento,
                                                                                  @Parameter(description = "ID de la empresa", required = true)
                                                                                  @RequestParam Long empresaId) {
        List<GastoObraProfesionalResponse> gastos = gastoService.listarGastosPorObra(direccionObraCalle, direccionObraAltura, direccionObraPiso,
                direccionObraDepartamento,
                empresaId
        );
        return ResponseEntity.ok(gastos);
    }
}
