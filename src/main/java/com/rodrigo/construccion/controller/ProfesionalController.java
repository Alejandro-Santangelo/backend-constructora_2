
package com.rodrigo.construccion.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.rodrigo.construccion.dto.request.ProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.service.IProfesionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profesionales")
@Tag(name = "Profesionales", description = "Gestión de profesionales")
public class ProfesionalController {

    private final IProfesionalService profesionalService;

    @PostMapping
    @Operation(summary = "Registrar nuevo profesional", description = "Registra un nuevo profesional en el sistema con sus datos básicos, especialidad y tarifa por hora. "
            + "Campos requeridos: nombre, tipoProfesional. Campos opcionales: teléfono, email, especialidad, valorHoraDefault.")
    public ResponseEntity<ProfesionalResponseDTO> crearProfesional(
            @Valid @RequestBody ProfesionalRequestDTO requestDTO) {
        ProfesionalResponseDTO responseDTO = profesionalService.crearProfesional(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    @Operation(summary = "Listar todos los profesionales", description = "Obtiene la lista completa de profesionales registrados en el sistema. "
            + "Incluye información básica, especialidad, tarifa y estadísticas de obras asignadas.")
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerTodosLosProfesionales() {
        return ResponseEntity.ok(profesionalService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar profesional por ID", description = "Obtiene los detalles completos de un profesional específico mediante su ID único. "
            + "Incluye datos personales, especialidad, tarifa y estadísticas de participación en obras.")
    public ResponseEntity<ProfesionalResponseDTO> obtenerProfesionalPorId(@PathVariable Long id) {
        return ResponseEntity.ok().body(profesionalService.obtenerProfesionalPorId(id));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar profesionales por nombre", description = "Busca profesionales cuyo nombre contenga el texto ingresado, ignorando mayúsculas/minúsculas. Devuelve una lista de coincidencias.")
    public ResponseEntity<List<ProfesionalResponseDTO>> buscarPorNombre(
            @Parameter(description = "Nombre o parte del nombre del profesional a buscar", example = "Juan") @RequestParam String nombre) {
        return ResponseEntity.ok(profesionalService.buscarPorNombre(nombre));
    }

    // Para que Rodrigo pueda crear un tipo de profesional, en caso de que no haya uno en el
    // sistema, se debe crear una tabla especifica en la BD para gestionar dichos tipos.
    @GetMapping("/ver/tipos-profesionales")
    @Operation(summary = "Listar los tipos disponibles de profesionales", description = "Obtiene la lista de todos los tipos de profesionales disponibles en el sistema. "
            + "Útil para formularios de selección y validaciones. Incluye: Arquitecto, Ingeniero, Maestro Mayor, " +
            "Albañil, Electricista, Plomero, Pintor, Carpintero, etc.")
    public ResponseEntity<List<String>> obtenerTiposDisponibles() {
        List<String> tipos = profesionalService.obtenerTiposProfesionales();
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/por-tipo")
    @Operation(summary = "Buscar profesionales por especialidad", description = "Busca profesionales filtrados por tipo/especialidad con búsqueda flexible. "
            + "Acepta variaciones de género y capitalización automáticamente. " +
            "Ejemplos válidos: 'Arquitecto', 'Arquitecta', 'arquitecto', 'INGENIERO', 'ingeniera', etc. " +
            "Útil para encontrar profesionales específicos para asignar a obras.")
    public ResponseEntity<List<ProfesionalResponseDTO>> buscarPorTipo(
            @Parameter(description = "Tipo de profesional a buscar. Acepta variaciones de género y capitalización. " +
                    "Ejemplos: 'Arquitecto', 'arquitecta', 'INGENIERO', 'electricista', 'Maestro Mayor', etc.", example = "Arquitecto") @RequestParam String tipoProfesional) {
        List<ProfesionalResponseDTO> profesionalesDto = profesionalService.buscarPorTipo(tipoProfesional);
        return ResponseEntity.ok(profesionalesDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos del profesional", description = "Modifica los datos de un profesional existente. Permite actualizar nombre, tipo, "
            + "datos de contacto, especialidad y tarifa por hora. El ID no puede ser modificado.")
    public ResponseEntity<ProfesionalResponseDTO> actualizarProfesional(@PathVariable Long id,
            @RequestBody ProfesionalRequestDTO requestDTO) {
        ProfesionalResponseDTO responseDTO = profesionalService.actualizar(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar profesional del sistema", description = "Elimina permanentemente un profesional del sistema. ATENCIÓN: Esta acción no se puede deshacer. "
            + "Se recomienda verificar que el profesional no tenga obras asignadas antes de eliminarlo.")
    public ResponseEntity<String> eliminarProfesional(@PathVariable Long id) {
        profesionalService.eliminar(id);
        return ResponseEntity.ok().body("Profesional eliminado exitosamente.");
    }

    /**
     * Actualizar el valorHoraDefault de todos los profesionales por un porcentaje
     */
    @PutMapping("/actualizar-valor-hora-todos")
    @Operation(summary = "Actualizar valorHoraDefault de todos los profesionales por porcentaje", description = "Modifica el valorHoraDefault de todos los profesionales aumentando o disminuyendo según el porcentaje ingresado. Ejemplo: 10 para aumentar un 10%, -5 para disminuir un 5%.")
    public ResponseEntity<String> actualizarValorHoraTodos(@RequestParam("porcentaje") double porcentaje) {
        profesionalService.actualizarValorHoraTodosPorPorcentaje(porcentaje);
        return ResponseEntity.ok().body("El valor por hora de todos los profesionales se ha actualizado con éxito.");
    }

    /**
     * Actualizar el valorHoraDefault de un profesional específico por porcentaje
     */
    @PutMapping("/{id}/actualizar-valor-hora")
    @Operation(summary = "Actualizar valorHoraDefault de un profesional por porcentaje", description = "Modifica el valorHoraDefault de un profesional específico aumentando o disminuyendo según el porcentaje ingresado. Ejemplo: 10 para aumentar un 10%, -5 para disminuir un 5%.")
    public ResponseEntity<String> actualizarValorHoraPorId(@PathVariable Long id,
            @RequestParam("porcentaje") double porcentaje) {
        profesionalService.actualizarValorHoraPorIdPorPorcentaje(id, porcentaje);
        return ResponseEntity.ok().body("El valor por hora del profesional se ha actualizado con éxito.");
    }

    /**
     * Actualizar el porcentajeGanancia de todos los profesionales por un valor
     */
    @PutMapping("/actualizar-porcentaje-ganancia-todos")
    @Operation(summary = "Actualizar porcentajeGanancia de todos los profesionales", description = "Modifica el porcentajeGanancia de todos los profesionales al valor ingresado. Ejemplo: 15 para establecer 15% de ganancia.")
    public ResponseEntity<String> actualizarPorcentajeGananciaTodos(@RequestParam("porcentaje") double porcentaje) {
        profesionalService.actualizarPorcentajeGananciaTodos(porcentaje);
        return ResponseEntity.ok()
                .body("El porcentaje de ganancia de todos los profesionales se ha actualizado con éxito.");

    }

    /**
     * Actualizar el porcentajeGanancia de un profesional específico por valor
     */
    @PutMapping("/{id}/actualizar-porcentaje-ganancia")
    @Operation(summary = "Actualizar porcentajeGanancia de un profesional por valor", description = "Modifica el porcentajeGanancia de un profesional específico al valor ingresado. Ejemplo: 20 para establecer 20% de ganancia.")
    public ResponseEntity<String> actualizarPorcentajeGananciaPorId(@PathVariable Long id,
            @RequestParam("porcentaje") double porcentaje) {
        profesionalService.actualizarPorcentajeGananciaPorId(id, porcentaje);
        return ResponseEntity.ok().body("El porcentaje de ganancia del profesional se ha actualizado con éxito.");
    }

    /**
     * Actualizar el porcentajeGanancia de varios profesionales por lista de IDs
     */
    @PutMapping("/actualizar-porcentaje-ganancia-varios")
    @Operation(summary = "Actualizar porcentajeGanancia de varios profesionales", description = "Modifica el porcentajeGanancia de varios profesionales seleccionados. Body: {\"ids\": [1,2,3], \"porcentaje\": 15}")
    public ResponseEntity<String> actualizarPorcentajeGananciaVarios(@RequestBody java.util.Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");
        double porcentaje = ((Number) request.get("porcentaje")).doubleValue();
        
        profesionalService.actualizarPorcentajeGananciaVarios(ids, porcentaje);
        return ResponseEntity.ok().body("El porcentaje de ganancia de " + ids.size() + " profesionales se ha actualizado con éxito.");
    }
}
