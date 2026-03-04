package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.AdelantoRequestDTO;
import com.rodrigo.construccion.dto.response.AdelantoResponseDTO;
import com.rodrigo.construccion.service.IAdelantoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Adelantos
 * 
 * CRUD completo para adelantos a profesionales en obras
 */
@RestController
@RequestMapping("/api/adelantos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Adelantos", description = "Gestión completa de adelantos a profesionales en obras")
public class AdelantoController {

    private final IAdelantoService adelantoService;

    /**
     * ========================================
     * ENDPOINTS CRUD PRINCIPALES
     * ========================================
     */

    @PostMapping
    @Operation(
        summary = "Crear adelanto",
        description = "Crea un nuevo adelanto para un profesional asignado a una obra. " +
                     "Valida automáticamente: saldo disponible, límite máximo de adelantos (50% del total), " +
                     "y estado activo del profesional. Requiere: profesionalObraId, empresaId y monto."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Adelanto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o validaciones de negocio fallidas"),
        @ApiResponse(responseCode = "404", description = "Profesional-obra o empresa no encontrados")
    })
    public ResponseEntity<AdelantoResponseDTO> crearAdelanto(
            @Valid @RequestBody AdelantoRequestDTO request) {
        
        log.info("POST /api/adelantos - Creando adelanto para profesional-obra: {}, monto: {}", 
                request.getProfesionalObraId(), request.getMonto());
        
        AdelantoResponseDTO adelanto = adelantoService.crearAdelanto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(adelanto);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener adelanto por ID",
        description = "Retorna los detalles completos de un adelanto específico. " +
                     "Incluye información del profesional, obra, montos, saldo pendiente y porcentaje descontado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Adelanto encontrado"),
        @ApiResponse(responseCode = "404", description = "Adelanto no encontrado"),
        @ApiResponse(responseCode = "400", description = "El pago no es un adelanto o no pertenece a la empresa")
    })
    public ResponseEntity<AdelantoResponseDTO> obtenerAdelantoPorId(
            @Parameter(description = "ID del adelanto", required = true) @PathVariable Long id,
            @Parameter(description = "ID de la empresa (validación multi-tenant)", required = true) 
            @RequestParam Long empresaId) {
        
        log.debug("GET /api/adelantos/{} - Empresa: {}", id, empresaId);
        
        AdelantoResponseDTO adelanto = adelantoService.obtenerAdelantoPorId(id, empresaId);
        return ResponseEntity.ok(adelanto);
    }

    @GetMapping
    @Operation(
        summary = "Listar adelantos de la empresa",
        description = "Retorna todos los adelantos de una empresa, incluyendo activos, completados y cancelados. " +
                     "Útil para reportes generales y auditoría."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de adelantos retornada exitosamente")
    })
    public ResponseEntity<List<AdelantoResponseDTO>> listarAdelantosPorEmpresa(
            @Parameter(description = "ID de la empresa", required = true) 
            @RequestParam Long empresaId) {
        
        log.debug("GET /api/adelantos - Empresa: {}", empresaId);
        
        List<AdelantoResponseDTO> adelantos = adelantoService.listarAdelantosPorEmpresa(empresaId);
        return ResponseEntity.ok(adelantos);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar adelanto",
        description = "Permite actualizar campos específicos de un adelanto: observaciones, motivo, " +
                     "método de pago, número de comprobante y aprobado por. " +
                     "NO permite modificar el monto una vez creado. " +
                     "Solo se pueden actualizar adelantos en estado ACTIVO."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Adelanto actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Adelanto no encontrado"),
        @ApiResponse(responseCode = "400", description = "Adelanto completado o cancelado (no modificable)")
    })
    public ResponseEntity<AdelantoResponseDTO> actualizarAdelanto(
            @Parameter(description = "ID del adelanto", required = true) @PathVariable Long id,
            @Valid @RequestBody AdelantoRequestDTO request,
            @Parameter(description = "ID de la empresa", required = true) 
            @RequestParam Long empresaId) {
        
        log.info("PUT /api/adelantos/{} - Empresa: {}", id, empresaId);
        
        AdelantoResponseDTO adelanto = adelantoService.actualizarAdelanto(id, request, empresaId);
        return ResponseEntity.ok(adelanto);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Anular adelanto",
        description = "Anula un adelanto (cambia estado a CANCELADO). " +
                     "Solo se pueden anular adelantos que NO tengan descuentos aplicados. " +
                     "Si ya se descontó parte del adelanto en pagos semanales, NO se puede anular. " +
                     "Requiere especificar un motivo de anulación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Adelanto anulado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Adelanto no encontrado"),
        @ApiResponse(responseCode = "400", description = "Adelanto ya tiene descuentos aplicados (no se puede anular)")
    })
    public ResponseEntity<AdelantoResponseDTO> anularAdelanto(
            @Parameter(description = "ID del adelanto", required = true) @PathVariable Long id,
            @Parameter(description = "ID de la empresa", required = true) 
            @RequestParam Long empresaId,
            @Parameter(description = "Motivo de la anulación", required = true) 
            @RequestParam String motivo) {
        
        log.info("DELETE /api/adelantos/{} - Empresa: {}, Motivo: {}", id, empresaId, motivo);
        
        AdelantoResponseDTO adelanto = adelantoService.anularAdelanto(id, empresaId, motivo);
        return ResponseEntity.ok(adelanto);
    }

    /**
     * ========================================
     * ENDPOINTS DE CONSULTA ESPECÍFICOS
     * ========================================
     */

    @GetMapping("/profesional/{profesionalObraId}")
    @Operation(
        summary = "Listar adelantos de un profesional",
        description = "Retorna todos los adelantos (activos, completados y cancelados) de un profesional " +
                     "específico en una obra. Útil para ver el historial completo de adelantos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de adelantos del profesional"),
        @ApiResponse(responseCode = "404", description = "Profesional-obra no encontrado")
    })
    public ResponseEntity<List<AdelantoResponseDTO>> listarAdelantosPorProfesional(
            @Parameter(description = "ID de la asignación profesional-obra", required = true) 
            @PathVariable Long profesionalObraId,
            @Parameter(description = "ID de la empresa", required = true) 
            @RequestParam Long empresaId) {
        
        log.debug("GET /api/adelantos/profesional/{} - Empresa: {}", profesionalObraId, empresaId);
        
        List<AdelantoResponseDTO> adelantos = adelantoService.listarAdelantosPorProfesional(profesionalObraId, empresaId);
        return ResponseEntity.ok(adelantos);
    }

    @GetMapping("/profesional/{profesionalObraId}/pendientes")
    @Operation(
        summary = "Listar adelantos pendientes de descontar",
        description = "Retorna SOLO los adelantos que tienen saldo pendiente de descontar (estado ACTIVO). " +
                     "Estos son los adelantos que se descontarán automáticamente en los próximos pagos semanales. " +
                     "Ordenados por fecha (FIFO - First In, First Out)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de adelantos pendientes"),
        @ApiResponse(responseCode = "404", description = "Profesional-obra no encontrado")
    })
    public ResponseEntity<List<AdelantoResponseDTO>> listarAdelantosPendientes(
            @Parameter(description = "ID de la asignación profesional-obra", required = true) 
            @PathVariable Long profesionalObraId,
            @Parameter(description = "ID de la empresa", required = true) 
            @RequestParam Long empresaId) {
        
        log.debug("GET /api/adelantos/profesional/{}/pendientes - Empresa: {}", profesionalObraId, empresaId);
        
        List<AdelantoResponseDTO> adelantos = adelantoService.listarAdelantosPendientes(profesionalObraId, empresaId);
        return ResponseEntity.ok(adelantos);
    }

    @GetMapping("/profesional/{profesionalObraId}/historial")
    @Operation(
        summary = "Obtener historial de adelantos",
        description = "Retorna todos los adelantos de un profesional ordenados por fecha (más reciente primero). " +
                     "Incluye todos los estados: ACTIVO, COMPLETADO y CANCELADO."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historial de adelantos"),
        @ApiResponse(responseCode = "404", description = "Profesional-obra no encontrado")
    })
    public ResponseEntity<List<AdelantoResponseDTO>> obtenerHistorialAdelantos(
            @Parameter(description = "ID de la asignación profesional-obra", required = true) 
            @PathVariable Long profesionalObraId,
            @Parameter(description = "ID de la empresa", required = true) 
            @RequestParam Long empresaId) {
        
        log.debug("GET /api/adelantos/profesional/{}/historial - Empresa: {}", profesionalObraId, empresaId);
        
        List<AdelantoResponseDTO> adelantos = adelantoService.obtenerHistorialAdelantos(profesionalObraId, empresaId);
        return ResponseEntity.ok(adelantos);
    }

    @GetMapping("/obra/{obraId}")
    @Operation(
        summary = "Listar adelantos de una obra",
        description = "Retorna todos los adelantos otorgados a profesionales asignados a una obra específica."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de adelantos de la obra")
    })
    public ResponseEntity<List<AdelantoResponseDTO>> listarAdelantosPorObra(
            @Parameter(description = "ID de la obra", required = true) 
            @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa", required = true) 
            @RequestParam Long empresaId) {
        
        log.debug("GET /api/adelantos/obra/{} - Empresa: {}", obraId, empresaId);
        
        List<AdelantoResponseDTO> adelantos = adelantoService.listarAdelantosPorObra(obraId, empresaId);
        return ResponseEntity.ok(adelantos);
    }

    /**
     * ========================================
     * ENDPOINTS DE CÁLCULOS Y VALIDACIONES
     * ========================================
     */

    @GetMapping("/profesional/{profesionalObraId}/total-pendiente")
    @Operation(
        summary = "Calcular total de adelantos pendientes",
        description = "Retorna la suma total de todos los adelantos activos pendientes de descontar " +
                     "de un profesional. Este valor se usa para validar nuevos adelantos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Total calculado exitosamente")
    })
    public ResponseEntity<Map<String, Object>> calcularTotalPendiente(
            @Parameter(description = "ID de la asignación profesional-obra", required = true) 
            @PathVariable Long profesionalObraId) {
        
        log.debug("GET /api/adelantos/profesional/{}/total-pendiente", profesionalObraId);
        
        BigDecimal total = adelantoService.calcularTotalAdelantosPendientes(profesionalObraId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("profesionalObraId", profesionalObraId);
        response.put("totalPendiente", total);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profesional/{profesionalObraId}/validar-disponibilidad")
    @Operation(
        summary = "Validar disponibilidad de adelanto",
        description = "Valida si se puede otorgar un adelanto de un monto específico a un profesional. " +
                     "Verifica: saldo disponible, límite máximo (50% del total asignado) y estado activo. " +
                     "Retorna true si es válido, o un mensaje de error descriptivo si no lo es."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Validación completada"),
        @ApiResponse(responseCode = "400", description = "Validación fallida - ver mensaje de error")
    })
    public ResponseEntity<Map<String, Object>> validarDisponibilidad(
            @Parameter(description = "ID de la asignación profesional-obra", required = true) 
            @PathVariable Long profesionalObraId,
            @Parameter(description = "Monto del adelanto a validar", required = true) 
            @RequestParam BigDecimal monto) {
        
        log.debug("POST /api/adelantos/profesional/{}/validar-disponibilidad - Monto: {}", 
                profesionalObraId, monto);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean esValido = adelantoService.validarAdelantoDisponible(profesionalObraId, monto);
            BigDecimal totalPendiente = adelantoService.calcularTotalAdelantosPendientes(profesionalObraId);
            
            response.put("valido", esValido);
            response.put("monto", monto);
            response.put("totalPendiente", totalPendiente);
            response.put("mensaje", "El adelanto es válido y puede ser otorgado");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("valido", false);
            response.put("monto", monto);
            response.put("mensaje", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
