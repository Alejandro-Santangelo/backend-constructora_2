
package com.rodrigo.construccion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import com.rodrigo.construccion.dto.request.ProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.service.IProfesionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profesionales")
public class ProfesionalController {

    private final IProfesionalService profesionalService;

    @PostMapping
    public ResponseEntity<ProfesionalResponseDTO> crearProfesional(@Valid @RequestBody ProfesionalRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profesionalService.crearProfesional(requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerTodosLosProfesionales() {
        return ResponseEntity.ok(profesionalService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfesionalResponseDTO> obtenerProfesionalPorId(@PathVariable Long id) {
        return ResponseEntity.ok().body(profesionalService.obtenerProfesionalPorId(id));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProfesionalResponseDTO>> buscarPorNombre(
            @Parameter(description = "Nombre o parte del nombre del profesional a buscar", example = "Juan") @RequestParam String nombre) {
        return ResponseEntity.ok(profesionalService.buscarPorNombre(nombre));
    }

    @GetMapping("/por-tipo")
    public ResponseEntity<List<ProfesionalResponseDTO>> buscarPorTipo(@RequestParam String tipoProfesional) {
        return ResponseEntity.ok(profesionalService.buscarPorTipo(tipoProfesional));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfesionalResponseDTO> actualizarProfesional(@PathVariable Long id,
                                                                        @RequestBody ProfesionalRequestDTO requestDTO) {
        return ResponseEntity.ok(profesionalService.actualizar(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarProfesional(@PathVariable Long id) {
        profesionalService.eliminar(id);
        return ResponseEntity.ok().body("Profesional eliminado exitosamente.");
    }

    /* Actualizar el valorHoraDefault de todos los profesionales por un porcentaje */
    @PutMapping("/actualizar-valor-hora-todos")
    public ResponseEntity<String> actualizarValorHoraTodos(@RequestParam("porcentaje") double porcentaje) {
        profesionalService.actualizarValorHoraTodosPorPorcentaje(porcentaje);
        return ResponseEntity.ok().body("El valor por hora de todos los profesionales se ha actualizado con éxito.");
    }

    /* Actualizar el valorHoraDefault de un profesional específico por porcentaje */
    @PutMapping("/{id}/actualizar-valor-hora")
    public ResponseEntity<String> actualizarValorHoraPorId(@PathVariable Long id, @RequestParam("porcentaje") double porcentaje) {
        profesionalService.actualizarValorHoraPorIdPorPorcentaje(id, porcentaje);
        return ResponseEntity.ok().body("El valor por hora del profesional se ha actualizado con éxito.");
    }

    /* Actualizar el porcentajeGanancia de todos los profesionales por un valor */
    @PutMapping("/actualizar-porcentaje-ganancia-todos")
    public ResponseEntity<String> actualizarPorcentajeGananciaTodos(@RequestParam("porcentaje") double porcentaje) {
        profesionalService.actualizarPorcentajeGananciaTodos(porcentaje);
        return ResponseEntity.ok()
                .body("El porcentaje de ganancia de todos los profesionales se ha actualizado con éxito.");
    }

    /* Actualizar el porcentajeGanancia de un profesional específico por valor */
    @PutMapping("/{id}/actualizar-porcentaje-ganancia")
    public ResponseEntity<String> actualizarPorcentajeGananciaPorId(@PathVariable Long id,
                                                                    @RequestParam("porcentaje") double porcentaje) {
        profesionalService.actualizarPorcentajeGananciaPorId(id, porcentaje);
        return ResponseEntity.ok().body("El porcentaje de ganancia del profesional se ha actualizado con éxito.");
    }

}
