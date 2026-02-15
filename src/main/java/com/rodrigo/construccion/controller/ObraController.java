package com.rodrigo.construccion.controller;

import java.util.List;
import java.util.Map;

import com.rodrigo.construccion.dto.request.AsignacionProfesionalRequestDTO;
import com.rodrigo.construccion.dto.request.AsignacionSemanalRequestDTO;
import com.rodrigo.construccion.dto.request.AsignarOtroCostoRequestDTO;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.service.AsignacionProfesionalObraService;
import com.rodrigo.construccion.service.AsignacionSemanalService;
import com.rodrigo.construccion.service.IObraOtroCostoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rodrigo.construccion.dto.request.ObraRequestDTO;
import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.service.IObraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;

@Tag(name = "Obra", description = "Gestión de obras de construcción")
@RestController
@RequestMapping("/api/obras")
@RequiredArgsConstructor
public class ObraController {

    private final IObraService obraService;
    private final AsignacionProfesionalObraService asignacionService;
    private final AsignacionSemanalService asignacionSemanalService;
    private final IObraOtroCostoService obraOtroCostoService;

    @Operation(summary = "Obtener obra por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ObraSimpleDTO> obtenerObraPorId(
            @Parameter(description = "ID de la obra") @PathVariable Long id) {

        ObraSimpleDTO obraDto = obraService.obtenerPorId(id);
        return ResponseEntity.ok(obraDto);
    }

    @Operation(summary = "Obtener obras por cliente")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ObraResponseDTO>> obtenerPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {

        List<ObraResponseDTO> obrasDto = obraService.obtenerPorCliente(clienteId);
        return ResponseEntity.ok(obrasDto);
    }

    @Operation(summary = "Obtener obras por estado")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ObraSimpleDTO>> obtenerPorEstado(
            @Parameter(description = "Estado de la obra", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", allowableValues = {
                    "En planificación", "En revisión", "En obra", "Suspendida", "Finalizada", "Cancelada"
            })) @PathVariable String estado) {

        // Convertimos el String legible a nuestro Enum seguro
        EstadoObra estadoEnum = EstadoObra.fromDisplayName(estado);
        List<ObraSimpleDTO> obrasSimpleDto = obraService.obtenerPorEstado(estadoEnum);
        return ResponseEntity.ok(obrasSimpleDto);
    }

    @Operation(summary = "Obtener obras activas")
    @GetMapping("/activas")
    public ResponseEntity<List<ObraSimpleDTO>> obtenerObrasActivas() {
        List<ObraSimpleDTO> obrasActivasDto = obraService.obtenerActivas();
        return ResponseEntity.ok(obrasActivasDto);
    }

    @Operation(summary = "Obtener todas las obras de una empresa")
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ObraResponseDTO>> obtenerObrasPorEmpresa(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Filtrar solo obras manuales (sin presupuesto previo)") 
            @RequestParam(required = false, defaultValue = "false") Boolean soloManuales) {
        
        List<ObraResponseDTO> obras;
        
        if (Boolean.TRUE.equals(soloManuales)) {
            obras = obraService.obtenerObrasManualesPorEmpresa(empresaId);
        } else {
            obras = obraService.obtenerPorEmpresa(empresaId);
        }
        
        return ResponseEntity.ok(obras);
    }

    @Operation(summary = "Obtener todas las obras")
    @GetMapping("/todas")
    public ResponseEntity<List<ObraResponseDTO>> obtenerTodasObras() {
        return ResponseEntity.ok(obraService.obtenerTodas());
    }

    @Operation(summary = "Crear nueva obra")
    @PostMapping
    public ResponseEntity<ObraResponseDTO> crearObra(@Valid @RequestBody ObraRequestDTO obraRequestDTO) {
        ObraResponseDTO nuevaObra = obraService.crear(obraRequestDTO, obraRequestDTO.getIdCliente());
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaObra);
    }

    @Operation(summary = "Actualizar obra")
    @PutMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> actualizarObra(@Parameter(description = "ID de la obra") @PathVariable Long id,
                                                          @Valid @RequestBody ObraRequestDTO obraRequestDTO) {
        ObraResponseDTO obraActualizada = obraService.actualizar(id, obraRequestDTO);
        return ResponseEntity.ok(obraActualizada);
    }

    @DeleteMapping("/{id}/cascade")
    public ResponseEntity<Map<String, String>> eliminarObraEnCascada(
            @Parameter(description = "ID de la obra a eliminar") @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenancy)", required = true)
            @RequestParam Long empresaId) {
        obraService.eliminarEnCascada(id, empresaId);
        return ResponseEntity.ok(Map.of("mensaje", "Obra y todas sus relaciones eliminadas exitosamente."));
    }

    @Operation(summary = "Cambiar estado de obra")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ObraResponseDTO> cambiarEstado(
            @Parameter(description = "ID de la obra") @PathVariable Long id,
            @Parameter(description = "Nuevo estado", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", allowableValues = {
                    "En planificación", "En revisión", "En obra", "Suspendida", "Finalizada", "Cancelada"
            })) @RequestParam String estado) {
        EstadoObra estadoEnum = EstadoObra.fromDisplayName(estado);
        ObraResponseDTO obraDto = obraService.cambiarEstado(id, estadoEnum);
        return ResponseEntity.ok(obraDto);
    }

    @Operation(summary = "Obtener estadísticas de obras")
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasObraDTO> obtenerEstadisticas() {
        EstadisticasObraDTO estadisticas = obraService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    @Operation(summary = "Obtener estados disponibles")
    @GetMapping("/estados")
    public ResponseEntity<List<String>> obtenerEstados() {
        List<String> estados = Arrays.stream(EstadoObra.values())
                .map(EstadoObra::getDisplayName)
                .toList();
        return ResponseEntity.ok(estados);
    }

    // Para que Rodrigo pueda crear un estado de obra, en caso de que no haya uno en el
    // sistema, se debe crear una tabla especifica en la BD para gestionar dichos estados.
    @GetMapping("/ver/estados-obra")
    @Operation(summary = "Listar todos los estados de obra disponibles", description = "Obtiene la lista de todos los estados que se pueden asignar a una obra. "
            + "Útil para formularios con menu de desplegable al crear o modificar una obra. Incluye: En planificación, En revisión, En obra, Suspendida, Finalizada, Cancelada.")
    public ResponseEntity<List<String>> obtenerRolesDisponibles() {
        List<String> estadosObra = obraService.obtenerEstadosObra();
        return ResponseEntity.ok(estadosObra);
    }

    /* ----------------  ¡¡¡¡  REVISAR ¡¡¡¡ ---------------- */
    /* SON MÉTODOS CUYO SERVICIO NO TIENE LA LÓGICA IMPLEMENTADA, DADO QUE SE DEBEN IMPLEMENTAR EN PROFESIONAL-OBRA */
    @Operation(summary = "Listar profesionales asignados a una obra", description = "Devuelve todos los profesionales asignados a la obra indicada por su ID.")
    @GetMapping("/{id}/profesionales-asignados")
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerProfesionalesAsignados(
            @PathVariable Long id) {
        List<ProfesionalResponseDTO> profesionales = obraService.obtenerProfesionalesAsignados(id);
        return ResponseEntity.ok(profesionales);
    }
    
    @GetMapping("/{obraId}/profesionales")
    @Operation(summary = "Obtener profesionales de una obra para trabajos extra", 
               description = "Obtiene los profesionales asignados a una obra para ser usados en trabajos extra. " +
                             "Requiere empresaId en el header.")
    public ResponseEntity<List<ProfesionalResponseDTO>> obtenerProfesionalesParaTrabajoExtra(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId) {
        
        List<ProfesionalResponseDTO> profesionales = obraService.obtenerProfesionalesAsignados(obraId);
        return ResponseEntity.ok(profesionales);
    }

    @PutMapping("/{id}/actualizar-porcentaje-ganancia-todos")
    @Operation(summary = "Actualizar porcentaje de ganancia de todos los profesionales asignados a una obra", description = "Modifica el porcentaje de ganancia de todos los profesionales asignados a la obra indicada por su ID.")
    public ResponseEntity<List<ProfesionalResponseDTO>> actualizarPorcentajeGananciaTodosAsignados(
            @PathVariable Long id,
            @RequestParam("porcentaje") double porcentaje) {
        List<ProfesionalResponseDTO> profesionalesActualizados = obraService
                .actualizarPorcentajeGananciaTodosAsignados(id, porcentaje);
        return ResponseEntity.ok(profesionalesActualizados);
    }

    @PutMapping("/{id}/actualizar-porcentaje-ganancia-profesional")
    @Operation(summary = "Actualizar porcentaje de ganancia de un profesional asignado a una obra", description = "Modifica el porcentaje de ganancia de un profesional específico asignado a la obra indicada por su ID.")
    public ResponseEntity<ProfesionalResponseDTO> actualizarPorcentajeGananciaProfesionalAsignado(@PathVariable Long id,
                                                                                                  @RequestParam("profesionalId") Long profesionalId, @RequestParam("porcentaje") double porcentaje) {
        ProfesionalResponseDTO profesionalActualizado = obraService.actualizarPorcentajeGananciaProfesionalAsignado(id,
                profesionalId, porcentaje);
        return ResponseEntity.ok(profesionalActualizado);
    }

    /* ================================================================================
     * ENDPOINTS PARA ASIGNACIÓN DE PROFESIONALES A RUBROS DE OBRAS
     * ================================================================================
     */

    @GetMapping("/{obraId}/asignaciones-profesionales")
    @Operation(
        summary = "Obtener asignaciones de profesionales a rubros de obra",
        description = "Retorna todas las asignaciones de profesionales a rubros específicos del presupuesto de la obra. " +
                     "Incluye información de jornales asignados, utilizados y restantes. " +
                     "Requiere empresaId para filtrado multi-tenant."
    )
    public ResponseEntity<List<AsignacionProfesionalObraDTO>> obtenerAsignacionesProfesionales(
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId) {
        
        List<AsignacionProfesionalObraDTO> asignaciones = asignacionService.obtenerAsignacionesPorObra(obraId, empresaId);
        return ResponseEntity.ok(asignaciones);
    }

    @PostMapping("/{obraId}/asignaciones-profesionales")
    @Operation(
        summary = "Crear nueva asignación de profesional a rubro de obra",
        description = "Asigna un profesional a un rubro específico del presupuesto de la obra. " +
                     "Tipos de asignación: " +
                     "- PROFESIONAL: Solo asigna el profesional por su rol (sin consumir jornales). " +
                     "- JORNAL: Asigna jornales específicos, descontándolos del total disponible del profesional. " +
                     "Validaciones automáticas: " +
                     "- Verifica que el profesional esté activo. " +
                     "- Valida jornales disponibles (para tipo JORNAL). " +
                     "- Valida fechas (fin >= inicio). " +
                     "Requiere empresaId para filtrado multi-tenant."
    )
    public ResponseEntity<?> crearAsignacionProfesional(
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId,
            @Valid @RequestBody AsignacionProfesionalRequestDTO request) {
        
        try {
            AsignacionProfesionalObraDTO asignacion = asignacionService.crearAsignacion(obraId, empresaId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(asignacion);
        } catch (com.rodrigo.construccion.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (com.rodrigo.construccion.exception.BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    @DeleteMapping("/{obraId}/asignaciones-profesionales/{asignacionId}")
    @Operation(
        summary = "Eliminar asignación de profesional a rubro de obra",
        description = "Elimina (desactiva) una asignación de profesional a un rubro específico del presupuesto. " +
                     "Realiza soft delete cambiando el estado a INACTIVO. " +
                     "Validaciones: " +
                     "- Verifica que la asignación existe. " +
                     "- Verifica que pertenece a la obra especificada. " +
                     "- Verifica que pertenece a la empresa (multi-tenant). " +
                     "Requiere empresaId como header."
    )
    public ResponseEntity<?> eliminarAsignacionProfesional(
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la asignación a eliminar", required = true)
            @PathVariable Long asignacionId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId) {
        
        try {
            asignacionService.eliminarAsignacion(asignacionId, obraId, empresaId);
            return ResponseEntity.ok(java.util.Map.of("message", "Asignación eliminada exitosamente"));
        } catch (com.rodrigo.construccion.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", e.getMessage()));
        } catch (com.rodrigo.construccion.exception.BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    // =====================================================
    // ENDPOINTS DE ASIGNACIÓN SEMANAL
    // =====================================================

    @PostMapping("/{obraId}/asignaciones-semanales")
    @Operation(
        summary = "Crear asignación semanal de profesionales",
        description = "Asigna profesionales a una obra con dos modalidades: " +
                     "'total' (equipo fijo que trabaja toda la obra con misma cantidad diaria) o " +
                     "'semanal' (asignación diferenciada por semana con cantidades variables). " +
                     "Validaciones: " +
                     "- Obra debe existir y pertenecer a la empresa. " +
                     "- Profesionales deben existir y estar activos. " +
                     "- Si modalidad='total', campo 'profesionales' es obligatorio. " +
                     "- Si modalidad='semanal', campo 'asignacionesPorSemana' es obligatorio. " +
                     "Crea registros en 'asignaciones_profesional_obra' y 'asignacion_profesional_dia'. " +
                     "Calcula automáticamente días hábiles (excluye fines de semana y feriados argentinos). " +
                     "Requiere empresaId como header."
    )
    public ResponseEntity<?> crearAsignacionSemanal(
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId,
            @Parameter(description = "Datos de la asignación semanal", required = true)
            @Valid @RequestBody AsignacionSemanalRequestDTO request) {
        
        try {
            // Validar que obraId del path coincide con el del body
            if (!obraId.equals(request.getObraId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(java.util.Map.of("error", "El ID de obra del path no coincide con el del body"));
            }
            
            AsignacionSemanalCreacionResponseDTO response = asignacionSemanalService.crearAsignacionSemanal(request, empresaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (com.rodrigo.construccion.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (com.rodrigo.construccion.exception.BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @GetMapping("/{obraId}/asignaciones-semanales")
    @Operation(
        summary = "Obtener asignaciones semanales de una obra",
        description = "Retorna todas las asignaciones semanales de profesionales para la obra especificada. " +
                     "Incluye tanto modalidad 'total' como 'semanal'. " +
                     "Filtra por empresa (multi-tenant) y solo asignaciones ACTIVAS. " +
                     "Requiere empresaId como header."
    )
    public ResponseEntity<?> obtenerAsignacionesSemanales(
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId) {
        
        try {
            // Por ahora retornamos las asignaciones existentes usando el servicio de asignación estándar
            List<AsignacionProfesionalObraDTO> asignaciones = asignacionService.obtenerAsignacionesPorObra(obraId, empresaId);
            
            // Filtrar solo las que tienen modalidad
            List<AsignacionProfesionalObraDTO> asignacionesSemanales = asignaciones.stream()
                    .filter(a -> a.getModalidad() != null && !a.getModalidad().isEmpty())
                    .toList();
            
            return ResponseEntity.ok(asignacionesSemanales);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al obtener asignaciones: " + e.getMessage()));
        }
    }

    @Operation(summary = "Asignar otro costo del presupuesto a una obra")
    @PostMapping("/{id}/otros-costos")
    public ResponseEntity<?> asignarOtroCosto(
            @Parameter(description = "ID de la obra") @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId,
            @Parameter(description = "Datos del costo a asignar", required = true)
            @Valid @RequestBody AsignarOtroCostoRequestDTO request) {
        
        System.out.println("=== DEBUG CONTROLADOR ===");
        System.out.println("Request recibido: " + request);
        System.out.println("Semana en request: " + request.getSemana());
        System.out.println("Tipo de semana: " + (request.getSemana() != null ? request.getSemana().getClass().getName() : "null"));
        System.out.println("========================");
        
        try {
            ObraOtroCostoResponseDTO resultado = obraOtroCostoService.asignar(empresaId, request);
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al asignar otro costo: " + e.getMessage()));
        }
    }

    @Operation(summary = "Obtener otros costos asignados a una obra")
    @GetMapping("/{id}/otros-costos")
    public ResponseEntity<?> obtenerOtrosCostos(
            @Parameter(description = "ID de la obra") @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId) {
        
        try {
            List<ObraOtroCostoResponseDTO> otrosCostos = obraOtroCostoService.obtenerPorObra(empresaId, id);
            return ResponseEntity.ok(otrosCostos);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al obtener otros costos: " + e.getMessage()));
        }
    }

    @Operation(summary = "Modificar asignación de otro costo de una obra")
    @PutMapping("/{obraId}/otros-costos/{asignacionId}")
    public ResponseEntity<?> modificarOtroCosto(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "ID de la asignación de costo a modificar") @PathVariable Long asignacionId,
            @Parameter(description = "Datos de la asignación a modificar") @RequestBody AsignarOtroCostoRequestDTO requestDTO,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId) {
        
        try {
            // Verificar que la obra exista
            if (!obraService.existeObra(empresaId, obraId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(java.util.Map.of("error", "Obra no encontrada"));
            }
            
            ObraOtroCostoResponseDTO response = obraOtroCostoService.actualizar(empresaId, asignacionId, requestDTO);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al modificar asignación: " + e.getMessage()));
        }
    }

    @Operation(summary = "Eliminar asignación de otro costo de una obra")
    @DeleteMapping("/{obraId}/otros-costos/{asignacionId}")
    public ResponseEntity<?> eliminarOtroCosto(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "ID de la asignación de costo a eliminar") @PathVariable Long asignacionId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader Long empresaId) {
        
        try {
            obraOtroCostoService.eliminar(empresaId, asignacionId);
            return ResponseEntity.ok(java.util.Map.of("message", "Asignación eliminada exitosamente"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error al eliminar asignación: " + e.getMessage()));
        }
    }
}
