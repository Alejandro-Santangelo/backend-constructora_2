package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.ItemCalculadoraPresupuesto;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.dto.request.PresupuestoNoClienteRequestDTO;
import com.rodrigo.construccion.service.PresupuestoNoClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping({"/api/presupuestos-no-cliente", "/api/v1/presupuestos-no-cliente"})
@Tag(name = "Presupuesto No Cliente", description = "CRUD para presupuestos no asociados a clientes")
public class PresupuestoNoClienteController {
    private static final Logger log = LoggerFactory.getLogger(PresupuestoNoClienteController.class);
    
    private final PresupuestoNoClienteService service;
    private final com.rodrigo.construccion.security.PermisoService permisoService;
    private final com.rodrigo.construccion.repository.HonorarioPorRubroRepository honorarioPorRubroRepository;

    public PresupuestoNoClienteController(
            PresupuestoNoClienteService service,
            com.rodrigo.construccion.security.PermisoService permisoService,
            com.rodrigo.construccion.repository.HonorarioPorRubroRepository honorarioPorRubroRepository) {
        this.service = service;
        this.permisoService = permisoService;
        this.honorarioPorRubroRepository = honorarioPorRubroRepository;
    }


    @GetMapping
    @Operation(
        summary = "Listar presupuestos por empresa o por obra", 
        description = "Lista todos los presupuestos no cliente de una empresa específica o de una obra específica. " +
                     "Requiere empresaId como parámetro obligatorio. Si se envía obraId, filtra por obra. " +
                     "Devuelve todos los registros sin paginación."
    )
    public ResponseEntity<List<PresupuestoNoCliente>> listarTodos(
            @Parameter(description = "ID de la empresa (obligatorio)", required = true)
            @RequestParam Long empresaId,
            @Parameter(description = "ID de la obra (opcional)")
            @RequestParam(required = false) Long obraId) {
        log.info("🔍 GET /presupuestos-no-cliente?empresaId={}&obraId={}", empresaId, obraId);
        
        try {
            List<PresupuestoNoCliente> presupuestos;
            if (obraId != null) {
                presupuestos = service.findAllByObraId(obraId);
                log.info("✅ Devolviendo {} presupuestos para obraId {}", presupuestos.size(), obraId);
            } else {
                presupuestos = service.findAllByEmpresaId(empresaId);
                log.info("✅ Devolviendo {} presupuestos para empresaId {}", presupuestos.size(), empresaId);
            }
            return ResponseEntity.ok(presupuestos);
        } catch (Exception e) {
            log.error("❌ Error obteniendo presupuestos para empresaId {}: {}", empresaId, e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/por-obra/{obraId}")
    @Operation(
        summary = "Obtener TODAS las versiones de presupuestos por obra",
        description = "Obtiene TODOS los presupuestos vinculados a una obra específica, independientemente de su estado. " +
                     "Retorna TODAS las versiones del presupuesto (v1, v2, v3, etc.) ordenadas por versión descendente " +
                     "(versión más reciente primero). El frontend es responsable de filtrar y seleccionar la versión " +
                     "APROBADA más reciente si es necesario. Si no hay presupuestos vinculados, retorna array vacío. " +
                     "Requiere empresaId para filtrado multi-tenant."
    )
    public ResponseEntity<List<PresupuestoNoCliente>> obtenerPresupuestoPorObra(
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader("empresaId") Long empresaId) {
        
        log.info("🔍 GET /presupuestos-no-cliente/por-obra/{}?empresaId={}", obraId, empresaId);

        List<PresupuestoNoCliente> presupuestos = service.findAllByObraId(obraId);
        
        log.info("✅ Encontrados {} presupuestos para obra {} (todas las versiones)", presupuestos.size(), obraId);
        
        return ResponseEntity.ok(presupuestos);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener por ID", 
        description = "Obtiene un presupuesto no cliente por su ID sin importar su estado. " +
                     "Devuelve presupuestos en cualquier estado (PENDIENTE, APROBADO, RECHAZADO). " +
                     "Requiere empresaId para filtrado multi-tenant."
    )
        public ResponseEntity<PresupuestoNoCliente> obtenerPorId(
            @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestParam Long empresaId) {
        log.info("📥 GET /presupuestos-no-cliente/{}?empresaId={}", id, empresaId);

        PresupuestoNoCliente pnc = service.obtenerPorId(id);

        log.info("🔍 DEBUG GET - honorariosJornales en entidad antes de respuesta: activo={}, tipo={}, valor={}",
            pnc.getHonorariosJornalesActivo(),
            pnc.getHonorariosJornalesTipo(),
            pnc.getHonorariosJornalesValor());
        
        log.info("🔍 DEBUG GET - DESCUENTOS en entidad antes de respuesta: explicacion={}, jornales(activo={}, tipo={}, valor={}), materiales(activo={}, tipo={}, valor={})",
            pnc.getDescuentosExplicacion(),
            pnc.getDescuentosJornalesActivo(), pnc.getDescuentosJornalesTipo(), pnc.getDescuentosJornalesValor(),
            pnc.getDescuentosMaterialesActivo(), pnc.getDescuentosMaterialesTipo(), pnc.getDescuentosMaterialesValor());

        log.info("📤 Response: ID={}, itemsCalculadora={}",
            pnc.getId(),
            pnc.getItemsCalculadora() != null ? pnc.getItemsCalculadora().size() : 0);
        // LEGACY: profesionales y materiales ahora están en items_calculadora

        // DEBUG: Verificar valores de incluirEnCalculoDias antes de serializar
        for (ItemCalculadoraPresupuesto item : pnc.getItemsCalculadora()) {
            log.info("🐛 CONTROLLER FINAL - ITEM {} ({}): incluirEnCalculoDias={}", 
                item.getId(), item.getTipoProfesional(), item.getIncluirEnCalculoDias());
        }

        return ResponseEntity.ok(pnc);
    }

    @PostMapping
    @Operation(summary = "Crear presupuesto no cliente", description = "Crea un nuevo presupuesto no cliente. La fecha de emisión se asigna automáticamente.")
    public ResponseEntity<PresupuestoNoCliente> crear(@Valid @RequestBody PresupuestoNoClienteRequestDTO dto, @RequestHeader(value = "X-Empresa-Id", required = false) Long empresaHeader) {
        // Si el DTO no trae idEmpresa, usar header X-Empresa-Id
        if (dto.getIdEmpresa() == null && empresaHeader != null) {
            dto.setIdEmpresa(empresaHeader);
        }
        PresupuestoNoCliente creado = service.crear(dto);
        return ResponseEntity.created(java.net.URI.create("/api/v1/presupuestos-no-cliente/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar presupuesto sin crear nueva versión",
        description = "Actualiza el presupuesto existente identificado por ID sin incrementar numeroVersion. " +
                     "Útil para cambios administrativos que no requieren versionado (ej: correcciones, datos de contacto, fechas). " +
                     "Para cambios críticos que requieren versionado (ej: precios, materiales, profesionales), " +
                     "usar PUT /presupuestos-no-cliente sin /{id} con parámetros de dirección. " +
                     "Respeta el filtrado multi-tenant automáticamente. " +
                     "⚠️ REQUIERE ROL SUPER_ADMIN - CONTRATISTAS NO PUEDEN MODIFICAR"
    )
    public ResponseEntity<PresupuestoNoCliente> actualizarPorId(
            @Parameter(description = "ID del presupuesto a actualizar", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PresupuestoNoClienteRequestDTO dto,
            @RequestHeader(value = "X-User-Rol", required = false) String rol) {
        
        // ✅ VALIDAR PERMISOS
        if (!permisoService.puedeModificarPresupuestos(rol)) {
            log.warn("❌ ACCESO DENEGADO: Usuario con rol {} intentó modificar presupuesto {}", rol, id);
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }
        
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @PutMapping
    @Operation(
        summary = "Actualizar presupuesto creando nueva versión",
        description = "Crea una NUEVA VERSIÓN del presupuesto buscando por dirección de obra. " +
                     "Incrementa automáticamente numeroVersion (+1) y mantiene el mismo numeroPresupuesto. " +
                     "Asigna nueva fechaEmision (fecha actual). " +
                     "Útil para cambios críticos que requieren historial de versiones (ej: cambios de precios, materiales, profesionales). " +
                     "Si no se especifica numeroVersion, toma la versión más reciente como base. " +
                     "El registro anterior NO se modifica, se crea uno nuevo. " +
                     "Respeta el filtrado multi-tenant automáticamente. " +
                     "⚠️ REQUIERE ROL SUPER_ADMIN - CONTRATISTAS NO PUEDEN MODIFICAR"
    )
    public ResponseEntity<?> actualizarPorDireccion(
            @Parameter(description = "Calle de la obra", required = true, example = "Av. Libertador")
            @RequestParam String direccionObraCalle,
            @Parameter(description = "Altura/número de la obra", required = true, example = "1234")
            @RequestParam String direccionObraAltura,
            @Parameter(description = "Piso de la obra (opcional)", example = "4")
            @RequestParam(required = false) String direccionObraPiso,
            @Parameter(description = "Departamento de la obra (opcional)", example = "A")
            @RequestParam(required = false) String direccionObraDepartamento,
            @Parameter(description = "Número de versión base (opcional, por defecto usa la última versión)", example = "1")
            @RequestParam(required = false) Integer numeroVersion,
            @Valid @RequestBody PresupuestoNoClienteRequestDTO dto,
            @RequestHeader(value = "X-User-Rol", required = false) String rol) {
        
        // ✅ VALIDAR PERMISOS
        if (!permisoService.puedeModificarPresupuestos(rol)) {
            log.warn("❌ ACCESO DENEGADO: Usuario con rol {} intentó crear nueva versión de presupuesto en {}, {}", 
                rol, direccionObraCalle, direccionObraAltura);
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }
        
        return ResponseEntity.ok(service.actualizarPorDireccion(
            direccionObraCalle, 
            direccionObraAltura, 
            direccionObraPiso, 
            direccionObraDepartamento, 
            numeroVersion, 
            dto));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar versión de presupuesto", 
        description = "Elimina un presupuesto específico. No renumera las versiones restantes. " +
                     "Si el presupuesto está APROBADO y tiene obra asociada, devuelve 409 Conflict. " +
                     "⚠️ REQUIERE ROL SUPER_ADMIN - CONTRATISTAS NO PUEDEN ELIMINAR"
    )
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            @RequestParam(required = true) 
            @Parameter(description = "ID de la empresa", required = true) 
            Long empresaId,
            @RequestHeader(value = "X-User-Rol", required = false) String rol) {
        
        // ✅ VALIDAR PERMISOS
        if (!permisoService.puedeEliminarPresupuestos(rol)) {
            log.warn("❌ ACCESO DENEGADO: Usuario con rol {} intentó eliminar presupuesto {}", rol, id);
            return ResponseEntity.status(403)
                .body(new ErrorResponse("No tiene permisos para eliminar presupuestos"));
        }
        
        log.info("🗑️ DELETE /presupuestos-no-cliente/{} - empresaId recibido: {}", id, empresaId);
        
        try {
            PresupuestoNoClienteService.EliminarPresupuestoResponse response = service.eliminar(id, empresaId);
            log.info("✅ Presupuesto {} eliminado correctamente", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error 400 al eliminar presupuesto {}: {}", id, e.getMessage());
            // 400 Bad Request o 404 Not Found
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(404)
                    .body(new ErrorResponse(e.getMessage()));
            }
            return ResponseEntity.status(400)
                .body(new ErrorResponse(e.getMessage()));
                
        } catch (IllegalStateException e) {
            log.error("❌ Error 409 al eliminar presupuesto {}: {}", id, e.getMessage());
            // 409 Conflict - presupuesto aprobado con obra
            // Extraer obraId del mensaje
            Long obraId = null;
            if (e.getMessage().contains("Obra ID: ")) {
                try {
                    String obraIdStr = e.getMessage()
                        .substring(e.getMessage().indexOf("Obra ID: ") + 9)
                        .split("\\)")[0];
                    obraId = Long.parseLong(obraIdStr);
                } catch (Exception ex) {
                    // Si falla la extracción, continuar sin obraId
                }
            }
            
            return ResponseEntity.status(409)
                .body(new ConflictResponse(e.getMessage(), obraId));
        }
    }
    
    // DTOs para respuestas de error
    private static class ErrorResponse {
        private final String mensaje;
        
        public ErrorResponse(String mensaje) {
            this.mensaje = mensaje;
        }
        
        public String getMensaje() { return mensaje; }
    }
    
    private static class ConflictResponse {
        private final String mensaje;
        private final Long obraId;
        
        public ConflictResponse(String mensaje, Long obraId) {
            this.mensaje = mensaje;
            this.obraId = obraId;
        }
        
        public String getMensaje() { return mensaje; }
        public Long getObraId() { return obraId; }
    }

    @PutMapping("/{id}/aprobar")
    @Operation(
        summary = "Aprobar presupuesto", 
        description = "Cambia el estado del presupuesto a APROBADO sin crear una nueva versión. " +
                     "Solo actualiza el campo 'estado' del registro existente. " +
                     "Respeta el filtrado multi-tenant automáticamente."
    )
    public ResponseEntity<PresupuestoNoCliente> aprobar(
            @PathVariable Long id,
            @RequestParam(required = false) Long empresaId) {
        try {
            PresupuestoNoCliente aprobado = service.aprobar(id);
            return ResponseEntity.ok(aprobado);
        } catch (IllegalArgumentException e) {
            // 400 Bad Request si ya está aprobado o validación falla
            if (e.getMessage().contains("ya está en estado APROBADO")) {
                return ResponseEntity.badRequest().build();
            }
            // 404 Not Found si no existe o no pertenece a la empresa
            if (e.getMessage().contains("no encontrado") || e.getMessage().contains("no pertenece")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    @PostMapping("/{id}/aprobar-y-crear-obra")
    @Operation(
        summary = "Aprobar presupuesto y crear obra",
        description = "Aprueba el presupuesto y crea automáticamente una obra asociada. " +
                     "Parámetros opcionales (mutuamente excluyentes): " +
                     "- obraReferenciaId: Reutiliza el cliente de una obra existente. " +
                     "- clienteReferenciaId: Reutiliza un cliente existente directamente. " +
                     "Si no se proporciona ninguno, busca o crea un cliente desde los datos del presupuesto. " +
                     "Útil cuando el mismo cliente solicita múltiples trabajos. " +
                     "Respeta el filtrado multi-tenant automáticamente."
    )
    public ResponseEntity<?> aprobarYCrearObra(
            @Parameter(description = "ID del presupuesto a aprobar", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la obra de referencia para reutilizar su cliente (opcional, excluyente con clienteReferenciaId)")
            @RequestParam(required = false) Long obraReferenciaId,
            @Parameter(description = "ID del cliente a reutilizar directamente (opcional, excluyente con obraReferenciaId)")
            @RequestParam(required = false) Long clienteReferenciaId) {
        try {
            log.info("📋 POST /presupuestos-no-cliente/{}/aprobar-y-crear-obra?obraReferenciaId={}&clienteReferenciaId={}", 
                id, obraReferenciaId, clienteReferenciaId);
            
            AprobarPresupuestoResponse response = service.aprobarYCrearObra(id, obraReferenciaId, clienteReferenciaId);
            log.info("✅ {}", response.getMensaje());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 400 Bad Request para errores de validación
            log.error("❌ Error de validación: {}", e.getMessage());
            
            // Mapear errores específicos a códigos HTTP apropiados
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
            }
            if (e.getMessage().contains("no pertenece")) {
                return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
            }
            
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            // 409 Conflict si ya está aprobado con obra
            log.error("❌ Conflicto: {}", e.getMessage());
            return ResponseEntity.status(409).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // 500 Internal Server Error para otros errores
            log.error("❌ Error inesperado al aprobar presupuesto y crear obra: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error al aprobar presupuesto y crear obra: " + e.getMessage()));
        }
    }

    @PutMapping("/obras/{obraId}/reactivar")
    @Operation(
        summary = "Reactivar obra suspendida/cancelada con nuevo presupuesto",
        description = "Permite reactivar una obra que está en estado SUSPENDIDA o CANCELADO vinculándola a un nuevo presupuesto APROBADO. " +
                     "El presupuesto anterior se desvincula (obra_id = null) y el nuevo se vincula. " +
                     "El estado de la obra se sincroniza automáticamente según el estado del nuevo presupuesto. " +
                     "El presupuesto estimado de la obra se actualiza con el total del nuevo presupuesto. " +
                     "Requiere empresaId para validación multi-tenant. " +
                     "⚠️ REQUIERE ROL SUPER_ADMIN - CONTRATISTAS NO PUEDEN MODIFICAR OBRAS"
    )
    public ResponseEntity<?> reactivarObraConNuevoPresupuesto(
            @Parameter(description = "ID de la obra a reactivar", required = true)
            @PathVariable Long obraId,
            @Parameter(description = "ID del nuevo presupuesto APROBADO", required = true)
            @RequestParam Long nuevoPresupuestoId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestParam Long empresaId,
            @RequestHeader(value = "X-User-Rol", required = false) String rol) {
        
        // ✅ VALIDAR PERMISOS
        if (!permisoService.puedeModificarObras(rol)) {
            log.warn("❌ ACCESO DENEGADO: Usuario con rol {} intentó reactivar obra {}", rol, obraId);
            return ResponseEntity.status(403)
                .body(new ErrorResponse("No tiene permisos para modificar obras"));
        }
        
        try {
            log.info("🔄 PUT /presupuestos-no-cliente/obras/{}/reactivar?nuevoPresupuestoId={}&empresaId={}", 
                obraId, nuevoPresupuestoId, empresaId);
            
            PresupuestoNoClienteService.ReactivarObraResponse response = 
                service.reactivarObraConNuevoPresupuesto(obraId, nuevoPresupuestoId, empresaId);
            
            log.info("✅ Obra {} reactivada - Estado: {} → {} | Presupuesto: {} → {}", 
                obraId, 
                response.getEstadoAnterior(), 
                response.getEstadoNuevo(),
                response.getPresupuestoAnteriorId(),
                response.getNuevoPresupuestoId());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al reactivar obra {}: {}", obraId, e.getMessage());
            
            // 404 Not Found si no existe la obra o el presupuesto
            if (e.getMessage().contains("no encontrada") || e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
            }
            // 403 Forbidden si no pertenece a la empresa
            if (e.getMessage().contains("no pertenece")) {
                return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
            }
            // 400 Bad Request para otros errores de validación
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            
        } catch (IllegalStateException e) {
            // 409 Conflict si el estado no permite reactivación
            log.error("❌ Conflicto al reactivar obra {}: {}", obraId, e.getMessage());
            return ResponseEntity.status(409).body(new ErrorResponse(e.getMessage()));
            
        } catch (Exception e) {
            // 500 Internal Server Error para otros errores
            log.error("❌ Error inesperado al reactivar obra {}: {}", obraId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al reactivar obra: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    @Operation(
        summary = "Actualizar estado",
        description = "Actualiza SOLO el campo estado de un presupuesto existente sin crear una nueva versión. " +
                     "Útil para marcar versiones anteriores como 'Modificado' cuando se crea una nueva versión. " +
                     "Requiere empresaId para validación multi-tenant."
    )
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @RequestBody java.util.Map<String, String> body) {
        try {
            String nuevoEstado = body.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("El campo 'estado' es requerido"));
            }
            PresupuestoNoCliente actualizado = service.actualizarEstado(id, empresaId, nuevoEstado);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            // 404 Not Found si no existe o no pertenece a la empresa
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // 500 Internal Server Error para otros errores
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error al actualizar estado: " + e.getMessage()));
        }
    }

    @GetMapping("/busqueda-avanzada")
    @Operation(
        summary = "Búsqueda avanzada de presupuestos",
        description = "Permite buscar presupuestos con múltiples filtros combinables. " +
                     "Todos los filtros son opcionales excepto empresaId (multi-tenant). " +
                     "Los filtros de texto son case-insensitive y usan búsqueda parcial (LIKE). " +
                     "Los filtros de rango permiten especificar mínimo, máximo o ambos. " +
                     "Ahora incluye filtros para barrio y torre en la dirección de obra."
    )
    public ResponseEntity<?> busquedaAvanzada(
            @RequestParam(required = true) Long empresaId,
            // Filtros de dirección de obra (en orden: barrio, calle, altura, torre, piso, depto)
            @RequestParam(required = false) String direccionObraBarrio,
            @RequestParam(required = false) String direccionObraCalle,
            @RequestParam(required = false) String direccionObraAltura,
            @RequestParam(required = false) String direccionObraTorre,
            @RequestParam(required = false) String direccionObraPiso,
            @RequestParam(required = false) String direccionObraDepartamento,
            // Filtros de datos del solicitante
            @RequestParam(required = false) String nombreSolicitante,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String mail,
            @RequestParam(required = false) String direccionParticular,
            // Filtros de información del presupuesto
            @RequestParam(required = false) Long numeroPresupuesto,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer numeroVersion,
            @RequestParam(required = false) String descripcion,
            // Filtros de fechas (rangos)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmisionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmisionHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCreacionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCreacionHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaProbableInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaProbableInicioHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimientoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vencimientoHasta,
            // Filtros de montos (rangos)
            @RequestParam(required = false) BigDecimal totalGeneralMinimo,
            @RequestParam(required = false) BigDecimal totalGeneralMaximo,
            @RequestParam(required = false) BigDecimal totalProfesionalesMinimo,
            @RequestParam(required = false) BigDecimal totalProfesionalesMaximo,
            @RequestParam(required = false) BigDecimal totalMaterialesMinimo,
            @RequestParam(required = false) BigDecimal totalMaterialesMaximo,
            // Filtros de configuración
            @RequestParam(required = false) String tipoProfesionalPresupuesto,
            @RequestParam(required = false) String modoPresupuesto
    ) {
        try {
            List<PresupuestoNoCliente> resultados = service.busquedaAvanzada(
                empresaId,
                direccionObraBarrio,
                direccionObraCalle,
                direccionObraAltura,
                direccionObraTorre,
                direccionObraPiso,
                direccionObraDepartamento,
                nombreSolicitante,
                telefono,
                mail,
                direccionParticular,
                numeroPresupuesto,
                estado,
                numeroVersion,
                descripcion,
                fechaEmisionDesde,
                fechaEmisionHasta,
                fechaCreacionDesde,
                fechaCreacionHasta,
                fechaProbableInicioDesde,
                fechaProbableInicioHasta,
                vencimientoDesde,
                vencimientoHasta,
                totalGeneralMinimo,
                totalGeneralMaximo,
                totalProfesionalesMinimo,
                totalProfesionalesMaximo,
                totalMaterialesMinimo,
                totalMaterialesMaximo,
                tipoProfesionalPresupuesto,
                modoPresupuesto
            );
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error en búsqueda avanzada: " + e.getMessage()));
        }
    }

    @GetMapping("/buscar-por-tipo-profesional")
    @Operation(
        summary = "Buscar presupuestos por tipo de profesional en JSON",
        description = "Busca presupuestos que contengan un tipo específico de profesional dentro del JSON profesionales_json. " +
                     "Utiliza operador @> de PostgreSQL JSONB para búsqueda eficiente. " +
                     "Respeta el filtrado multi-tenant automáticamente."
    )
    public ResponseEntity<?> buscarPorTipoProfesional(
            @Parameter(description = "Tipo de profesional a buscar (ej: ARQUITECTO, MAESTRO_MAYOR, etc.)", required = true)
            @RequestParam String tipoProfesional,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestParam Long empresaId) {
        try {
            List<PresupuestoNoCliente> resultados = service.buscarPorTipoProfesional(tipoProfesional, empresaId);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error al buscar por tipo de profesional: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/duplicar")
    @Operation(
        summary = "Duplicar presupuesto",
        description = "Crea una copia exacta de un presupuesto existente en estado BORRADOR. " +
                     "Incrementa el número de versión en 1 y mantiene el mismo numeroPresupuesto para agrupar versiones. " +
                     "Copia todos los datos, items de calculadora, profesionales, materiales y gastos generales. " +
                     "NO copia la asociación con obra (obraId). " +
                     "Útil cuando el usuario necesita modificar un presupuesto que ya fue enviado/aprobado."
    )
    public ResponseEntity<?> duplicarPresupuesto(
            @Parameter(description = "ID del presupuesto a duplicar", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la empresa (validación multi-tenant)", required = true)
            @RequestParam Long empresaId) {
        try {
            log.info("🔄 POST /presupuestos-no-cliente/{}/duplicar?empresaId={}", id, empresaId);
            PresupuestoNoCliente duplicado = service.duplicarPresupuesto(id, empresaId);
            log.info("✅ Presupuesto duplicado exitosamente. Nuevo ID: {}, versión: {}", 
                duplicado.getId(), duplicado.getNumeroVersion());
            return ResponseEntity.ok(duplicado);
        } catch (IllegalArgumentException e) {
            // 404 Not Found si no existe o no pertenece a la empresa
            log.error("❌ Presupuesto no encontrado o no pertenece a la empresa: {}", e.getMessage());
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            // 403 Forbidden si no pertenece a la empresa
            if (e.getMessage().contains("no pertenece a la empresa")) {
                log.error("❌ Acceso denegado: {}", e.getMessage());
                return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
            }
            // 500 Internal Server Error para otros errores
            log.error("❌ Error al duplicar presupuesto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error al duplicar presupuesto: " + e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error inesperado al duplicar presupuesto: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ErrorResponse("Error inesperado al duplicar presupuesto: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/fechas")
    @Operation(
        summary = "Actualizar solo fechas de presupuestos APROBADOS o EN_EJECUCION",
        description = "Actualiza ÚNICAMENTE fechaProbableInicio y tiempoEstimadoTerminacion " +
                     "de presupuestos en estado APROBADO o EN_EJECUCION. " +
                     "NO modifica el numeroVersion, NO cambia el estado, NO afecta otros campos. " +
                     "Útil para ajustar planificación sin crear nueva versión. " +
                     "Respeta el filtrado multi-tenant automáticamente."
    )
    public ResponseEntity<?> actualizarSoloFechas(
            @Parameter(description = "ID del presupuesto a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestParam Long empresaId,
            @Valid @RequestBody com.rodrigo.construccion.dto.request.ActualizarFechasDTO dto) {
        try {
            log.info("📅 PATCH /presupuestos-no-cliente/{}/fechas?empresaId={}", id, empresaId);
            log.info("   Nueva fecha inicio: {}, Nuevos días: {}", 
                dto.getFechaProbableInicio(), dto.getTiempoEstimadoTerminacion());
            
            PresupuestoNoCliente actualizado = service.actualizarSoloFechas(id, empresaId, dto);
            
            log.info("✅ Fechas actualizadas exitosamente - Estado: {} (sin cambio), Versión: {} (sin cambio)", 
                actualizado.getEstado(), actualizado.getNumeroVersion());
            
            return ResponseEntity.ok(actualizado);
            
        } catch (IllegalArgumentException e) {
            // 404 Not Found si no existe o no pertenece a la empresa
            if (e.getMessage().contains("no encontrado") || e.getMessage().contains("no pertenece")) {
                log.error("❌ Presupuesto no encontrado: {}", e.getMessage());
                return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
            }
            // 400 Bad Request para validaciones de estado
            log.error("❌ Validación fallida: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            
        } catch (Exception e) {
            log.error("❌ Error inesperado al actualizar fechas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al actualizar fechas: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/materiales")
    @Operation(
        summary = "Obtener materiales de un presupuesto",
        description = "Obtiene todos los materiales asociados a un presupuesto específico. " +
                     "Requiere empresaId en el header para validación multi-tenant."
    )
    public ResponseEntity<?> obtenerMaterialesPresupuesto(
            @Parameter(description = "ID del presupuesto", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader("empresaId") Long empresaId) {
        
        log.info("🔍 GET /presupuestos-no-cliente/{}/materiales con empresaId={}", id, empresaId);
        
        try {
            var materiales = service.obtenerMaterialesPresupuesto(id, empresaId);
            log.info("✅ Devolviendo {} materiales para presupuesto {}", materiales.size(), id);
            return ResponseEntity.ok(materiales);
        } catch (Exception e) {
            log.error("❌ Error obteniendo materiales del presupuesto {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al obtener materiales: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/materiales-con-stock")
    @Operation(
        summary = "Obtener materiales de un presupuesto con información de stock",
        description = "Obtiene todos los materiales de un presupuesto con información de stock disponible. " +
                     "Incluye cantidad disponible, asignada y restante. " +
                     "Requiere empresaId en el header y opcionalmente obraId como parámetro."
    )
    public ResponseEntity<?> obtenerMaterialesConStock(
            @Parameter(description = "ID del presupuesto", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader("empresaId") Long empresaId,
            @Parameter(description = "ID de la obra (opcional, para determinar ubicación)")
            @RequestParam(required = false) Long obraId) {
        
        log.info("🔍 GET /presupuestos-no-cliente/{}/materiales-con-stock con empresaId={}, obraId={}", 
                 id, empresaId, obraId);
        
        try {
            var materialesConStock = service.obtenerMaterialesConStock(id, empresaId, obraId);
            log.info("✅ Devolviendo {} materiales con stock para presupuesto {}", 
                     materialesConStock.size(), id);
            return ResponseEntity.ok(materialesConStock);
        } catch (Exception e) {
            log.error("❌ Error obteniendo materiales con stock del presupuesto {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al obtener materiales con stock: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/gastos-generales")
    @Operation(
        summary = "Obtener gastos generales de un presupuesto",
        description = "Obtiene todos los gastos generales asociados a un presupuesto específico. " +
                     "Devuelve la misma estructura que otros-costos para compatibilidad frontend. " +
                     "Requiere empresaId en el header para validación multi-tenant."
    )
    public ResponseEntity<?> obtenerGastosGeneralesPresupuesto(
            @Parameter(description = "ID del presupuesto", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader("empresaId") Long empresaId) {
        
        log.info("🔍 GET /presupuestos-no-cliente/{}/gastos-generales con empresaId={}", id, empresaId);
        
        try {
            // Usar el mismo método que otros-costos para compatibilidad
            var gastosGenerales = service.obtenerOtrosCostosPresupuesto(id, empresaId);
            log.info("✅ Devolviendo {} gastos generales para presupuesto {}", gastosGenerales.size(), id);
            return ResponseEntity.ok(gastosGenerales);
        } catch (Exception e) {
            log.error("❌ Error obteniendo gastos generales del presupuesto {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al obtener gastos generales: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/otros-costos")
    @Operation(
        summary = "Obtener otros costos de un presupuesto",
        description = "Obtiene todos los otros costos asociados a un presupuesto específico. " +
                     "Requiere empresaId en el header para validación multi-tenant."
    )
    public ResponseEntity<?> obtenerOtrosCostosPresupuesto(
            @Parameter(description = "ID del presupuesto", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestHeader("empresaId") Long empresaId) {
        
        log.info("🔍 GET /presupuestos-no-cliente/{}/otros-costos con empresaId={}", id, empresaId);
        
        try {
            var otrosCostos = service.obtenerOtrosCostosPresupuesto(id, empresaId);
            log.info("✅ Devolviendo {} otros costos para presupuesto {}", otrosCostos.size(), id);
            return ResponseEntity.ok(otrosCostos);
        } catch (Exception e) {
            log.error("❌ Error obteniendo otros costos del presupuesto {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al obtener otros costos: " + e.getMessage()));
        }
    }

    @GetMapping("/debug/obra-presupuesto-sync/{obraId}")
    @Operation(
        summary = "DEBUG: Verificar sincronización obra-presupuesto",
        description = "Endpoint de testing para verificar el estado actual de la sincronización " +
                     "entre una obra y sus presupuestos. Muestra cuál es el presupuesto vinculado " +
                     "y cuál debería ser según las versiones existentes."
    )
    public ResponseEntity<?> verificarSincronizacionObraPresupuesto(
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId) {
        
        log.info("🔍 DEBUG GET /presupuestos-no-cliente/debug/obra-presupuesto-sync/{}", obraId);
        
        try {
            // Obtener todos los presupuestos de la obra
            List<PresupuestoNoCliente> presupuestos = service.findAllByObraId(obraId);
            
            if (presupuestos.isEmpty()) {
                return ResponseEntity.ok(java.util.Map.of(
                    "obraId", obraId,
                    "mensaje", "No hay presupuestos asociados a esta obra",
                    "presupuestosEncontrados", 0
                ));
            }
            
            // El primer presupuesto debe ser el de mayor versión (están ordenados DESC)
            PresupuestoNoCliente masReciente = presupuestos.get(0);
            
            // Llamar al servicio de verificación
            service.getPresupuestoObraSyncService().verificarVinculoActual(obraId);
            
            var response = java.util.Map.of(
                "obraId", obraId,
                "presupuestosEncontrados", presupuestos.size(),
                "presupuestoMasReciente", java.util.Map.of(
                    "id", masReciente.getId(),
                    "version", masReciente.getNumeroVersion(),
                    "estado", masReciente.getEstado().toString(),
                    "total", masReciente.getTotalPresupuesto()
                ),
                "todosLosPresupuestos", presupuestos.stream()
                    .map(p -> java.util.Map.of(
                        "id", p.getId(),
                        "version", p.getNumeroVersion(),
                        "estado", p.getEstado().toString(),
                        "fechaCreacion", p.getFechaCreacion()
                    ))
                    .toList(),
                "mensaje", "Ver logs del servidor para detalles de sincronización"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error verificando sincronización obra {}: {}", obraId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(java.util.Map.of(
                    "error", "Error al verificar sincronización: " + e.getMessage()
                ));
        }
    }

    @PostMapping("/debug/obra-presupuesto-sync/{obraId}/forzar")
    @Operation(
        summary = "DEBUG: Forzar sincronización obra-presupuesto",
        description = "Endpoint de testing para forzar la sincronización entre una obra y sus presupuestos. " +
                     "Útil para corregir inconsistencias o probar la funcionalidad."
    )
    public ResponseEntity<?> forzarSincronizacionObraPresupuesto(
            @Parameter(description = "ID de la obra", required = true)
            @PathVariable Long obraId) {
        
        log.info("🔧 DEBUG POST /presupuestos-no-cliente/debug/obra-presupuesto-sync/{}/forzar", obraId);
        
        try {
            service.getPresupuestoObraSyncService().vincularPresupuestoMasReciente(obraId);
            
            return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Sincronización forzada ejecutada exitosamente",
                "obraId", obraId,
                "instrucciones", "Revise los logs del servidor para ver los detalles de la operación"
            ));
            
        } catch (Exception e) {
            log.error("❌ Error forzando sincronización obra {}: {}", obraId, e.getMessage());
            return ResponseEntity.internalServerError()
                .body(java.util.Map.of(
                    "error", "Error al forzar sincronización: " + e.getMessage()
                ));
        }
    }

    /**
     * POST /presupuestos-no-cliente/{id}/pdf
     * Endpoint para subir/enviar PDF del presupuesto
     */
    @PostMapping("/{id}/pdf")
    public ResponseEntity<?> enviarPdf(
            @PathVariable Long id,
            @RequestParam Long empresaId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("nombre_archivo") String nombreArchivo,
            @RequestParam("version_presupuesto") Integer versionPresupuesto,
            @RequestParam("incluye_honorarios") String incluyeHonorariosStr,
            @RequestParam("incluye_configuracion") String incluyeConfiguracionStr) {

        log.info("📤 POST /presupuestos-no-cliente/{}/pdf - empresaId={}", id, empresaId);

        try {
            // Validar que el presupuesto existe
            PresupuestoNoCliente presupuesto = service.obtenerPorId(id);
            
            if (presupuesto == null) {
                return ResponseEntity.status(404)
                    .body(java.util.Map.of("error", "Presupuesto no encontrado"));
            }

            // Validar empresa
            if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
                return ResponseEntity.status(403)
                    .body(java.util.Map.of("error", "No autorizado"));
            }

            // Validar archivo
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "El archivo está vacío"));
            }

            if (!"application/pdf".equals(file.getContentType())) {
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "El archivo debe ser PDF"));
            }

            if (file.getSize() > 50 * 1024 * 1024) { // 50MB
                return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", "El archivo excede el tamaño máximo (50MB)"));
            }

            // Convertir strings a boolean
            Boolean incluyeHonorarios = Boolean.parseBoolean(incluyeHonorariosStr);
            Boolean incluyeConfiguracion = Boolean.parseBoolean(incluyeConfiguracionStr);

            log.info("✅ PDF recibido correctamente: nombre={}, tamaño={} bytes", nombreArchivo, file.getSize());

            // Retornar éxito
            return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of(
                "mensaje", "PDF enviado correctamente",
                "nombreArchivo", nombreArchivo,
                "tamanio", file.getSize(),
                "presupuestoId", id
            ));

        } catch (Exception e) {
            log.error("❌ Error al enviar PDF para presupuesto {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(java.util.Map.of(
                    "error", "Error al enviar PDF",
                    "message", e.getMessage()
                ));
        }
    }

    @GetMapping("/honorarios-por-obras")
    @Operation(
        summary = "Obtener honorarios de presupuestos por obras",
        description = "Retorna la información de honorarios del último presupuesto (versión más reciente) " +
                     "vinculado a cada obra especificada. " +
                     "Acepta una lista de IDs de obras y devuelve solo los datos relevantes de honorarios, " +
                     "optimizado para mostrar en el frontend. " +
                     "Incluye: totales (base, honorarios, final), configuración de honorarios por categoría, " +
                     "y honorarios de dirección de obra. " +
                     "Requiere empresaId para validación multi-tenant. " +
                     "Si una obra no tiene presupuestos o no pertenece a la empresa, se omite del resultado."
    )
    public ResponseEntity<?> obtenerHonorariosPorObras(
            @Parameter(description = "IDs de las obras separados por coma (ej: 1,2,3)", required = true)
            @RequestParam List<Long> obraIds,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestParam Long empresaId) {
        
        log.info("🔍 GET /presupuestos-no-cliente/honorarios-por-obras?obraIds={}&empresaId={}", 
            obraIds, empresaId);
        
        try {
            if (obraIds == null || obraIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("El parámetro obraIds es requerido y no puede estar vacío"));
            }
            
            List<com.rodrigo.construccion.dto.response.HonorariosPresupuestoObraDTO> honorarios = 
                service.obtenerHonorariosPorObras(obraIds, empresaId);
            
            log.info("✅ Devolviendo honorarios de {} obras", honorarios.size());
            
            return ResponseEntity.ok(honorarios);
            
        } catch (Exception e) {
            log.error("❌ Error obteniendo honorarios por obras: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al obtener honorarios: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener profesionales con datos financieros de un presupuesto.
     * Diseñado específicamente para el sistema de adelantos.
     * 
     * Cuando un presupuesto está vinculado a una obra (presupuesto global),
     * los profesionales están en asignaciones_profesional_obra, no en el presupuesto.
     * Este endpoint resuelve automáticamente esa búsqueda y devuelve datos financieros completos.
     */
    @GetMapping("/{presupuestoId}/profesionales-financieros")
    @Operation(summary = "Obtener profesionales con datos financieros de un presupuesto", 
               description = "ESPECÍFICO PARA SISTEMA DE ADELANTOS: Obtiene profesionales asignados a la obra " +
                            "del presupuesto con datos financieros completos (totales pagados, adelantos, saldos). " +
                            "Si el presupuesto es global, busca los profesionales en asignaciones_profesional_obra. " +
                            "Devuelve: ID asignación, nombre, tipo, precio total, jornales, totales pagados, " +
                            "adelantos activos, saldo pendiente, límite de adelanto y disponibilidad.")
    public ResponseEntity<?> obtenerProfesionalesFinancierosPorPresupuesto(
            @PathVariable @Parameter(description = "ID del presupuesto", required = true) Long presupuestoId,
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true)
            @RequestParam Long empresaId) {
        
        log.info("🔍 GET /presupuestos-no-cliente/{}/profesionales-financieros?empresaId={}", 
            presupuestoId, empresaId);
        
        try {
            List<com.rodrigo.construccion.dto.response.ProfesionalObraFinancieroDTO> profesionales = 
                service.obtenerProfesionalesFinancierosPorPresupuesto(presupuestoId, empresaId);
            
            log.info("✅ Devolviendo {} profesionales con datos financieros", profesionales.size());
            
            return ResponseEntity.ok(profesionales);
            
        } catch (ResourceNotFoundException e) {
            log.warn("⚠️ {}", e.getMessage());
            return ResponseEntity.status(404)
                .body(new ErrorResponse(e.getMessage()));
                
        } catch (Exception e) {
            log.error("❌ Error obteniendo profesionales financieros: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al obtener profesionales: " + e.getMessage()));
        }
    }

    @GetMapping("/por-obra/{obraId}/rubros-activos")
    @Operation(
        summary = "Obtener rubros activos del presupuesto aprobado de una obra",
        description = "Retorna los honorarios por rubro marcados como activos del presupuesto APROBADO más reciente de la obra. " +
                     "Usado para el selector de rubros en el registro de jornales diarios."
    )
    public ResponseEntity<?> obtenerRubrosActivosPorObra(
            @PathVariable Long obraId,
            @RequestHeader("empresaId") Long empresaId) {
        log.info("🔍 GET /presupuestos-no-cliente/por-obra/{}/rubros-activos (empresaId={})", obraId, empresaId);
        try {
            List<com.rodrigo.construccion.model.entity.HonorarioPorRubro> rubros =
                honorarioPorRubroRepository.findRubrosActivosByObraId(obraId);
            log.info("✅ {} rubros activos encontrados para obra {}", rubros.size(), obraId);
            // Mapear a un DTO simple para no serializar toda la entidad con lazy refs
            List<java.util.Map<String, Object>> resultado = rubros.stream()
                .map(r -> {
                    java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", r.getId());
                    m.put("nombreRubro", r.getNombreRubro());
                    return m;
                })
                .toList();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("❌ Error obteniendo rubros para obra {}: {}", obraId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al obtener rubros: " + e.getMessage()));
        }
    }

    /**
     * PATCH /presupuestos-no-cliente/pool-mano-obra
     * Actualizar el pool (subtotal) de mano de obra de un rubro en una obra
     * Usado por el modal de gestión de pagos para ajustar pools de rubros
     */
    @PatchMapping("/pool-mano-obra")
    @Operation(
        summary = "Actualizar pool de mano de obra de un rubro en una obra",
        description = "Actualiza el subtotal de mano de obra (pool) de un rubro específico en una obra. " +
                     "Busca automáticamente el item de calculadora correspondiente usando obraId + tipoProfesional. " +
                     "Requiere empresaId para validación multi-tenant."
    )
    public ResponseEntity<?> actualizarPoolManoObra(
            @RequestParam 
            @Parameter(description = "ID de la obra", required = true) 
            Long obraId,
            @RequestParam 
            @Parameter(description = "Nombre del rubro/tipo profesional", required = true) 
            String tipoProfesional,
            @RequestParam 
            @Parameter(description = "Nuevo subtotal de mano de obra", required = true) 
            BigDecimal nuevoSubtotal,
            @RequestParam 
            @Parameter(description = "ID de la empresa (multi-tenant)", required = true) 
            Long empresaId) {
        
        log.info("🔄 PATCH /presupuestos-no-cliente/pool-mano-obra - obraId={}, tipoProfesional={}, nuevoSubtotal={}, empresaId={}", 
            obraId, tipoProfesional, nuevoSubtotal, empresaId);
        
        try {
            service.actualizarPoolManoObra(obraId, tipoProfesional, nuevoSubtotal, empresaId);
            
            log.info("✅ Pool de mano de obra actualizado para obra {} - rubro {}", obraId, tipoProfesional);
            
            return ResponseEntity.ok(java.util.Map.of(
                "mensaje", "Pool de mano de obra actualizado exitosamente",
                "obraId", obraId,
                "tipoProfesional", tipoProfesional,
                "nuevoSubtotal", nuevoSubtotal
            ));
            
        } catch (ResourceNotFoundException e) {
            log.error("❌ No encontrado: {}", e.getMessage());
            return ResponseEntity.status(404)
                .body(new ErrorResponse(e.getMessage()));
                
        } catch (IllegalArgumentException e) {
            log.error("❌ Validación fallida: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
                
        } catch (Exception e) {
            log.error("❌ Error actualizando pool: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Error al actualizar pool: " + e.getMessage()));
        }
    }
}
