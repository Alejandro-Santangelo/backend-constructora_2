package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.ActualizarAsignacionRequest;
import com.rodrigo.construccion.dto.request.AsignarProfesionalRequest;
import com.rodrigo.construccion.dto.request.AsignarProfesionalesBatchRequest;
import com.rodrigo.construccion.dto.response.ListaProfesionalesResponse;
import com.rodrigo.construccion.dto.response.AsignacionProfesionalResponse;
import com.rodrigo.construccion.dto.response.DisponibilidadProfesionalResponse;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.dto.response.ProfesionalObraFinancieroDTO;
import com.rodrigo.construccion.dto.response.ObraPagosDTO;
import com.rodrigo.construccion.dto.response.ProfesionalConsolidadoDTO;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.service.IProfesionalObraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Asignaciones Profesional-Obra
 * 
 * ARQUITECTURA REESTRUCTURADA - SOLO ENDPOINTS ESENCIALES
 * =======================================================
 * Este controlador ha sido completamente reestructurado para eliminar confusión
 * y simplificar las operaciones de asignación de profesionales a obras.
 */
@RestController
@RequestMapping("/api/profesionales-obras")
@RequiredArgsConstructor
@Tag(name = "Profesionales-Obras", description = "Gestión simplificada de asignaciones profesional-obra")
public class ProfesionalObraController {

        private static final Logger log = LoggerFactory.getLogger(ProfesionalObraController.class);
        
        private final IProfesionalObraService profesionalObraService;
        private final com.rodrigo.construccion.service.GastoObraProfesionalService gastoService;

        /**
         * =============================================
         * ENDPOINTS PRINCIPALES - ARQUITECTURA LIMPIA
         * =============================================
         */
        @GetMapping
        @Operation(summary = "Listar asignaciones profesional-obra", description = "Obtiene todas las asignaciones activas de profesionales a obras con información completa. "
                        +
                        "Incluye datos del profesional, obra, fechas de asignación y estado.")
        public ResponseEntity<List<AsignacionProfesionalResponse>> listarAsignaciones(
                        @Parameter(description = "ID de la empresa (opcional para filtrado multi-tenant)")
                        @RequestParam(required = false) Long empresaId) {
                List<AsignacionProfesionalResponse> asignaciones;
                if (empresaId != null) {
                        asignaciones = profesionalObraService.obtenerTodasPorEmpresa(empresaId);
                } else {
                        asignaciones = profesionalObraService.obtenerTodasComoDTO();
                }
                return ResponseEntity.ok(asignaciones);
        }

        @PostMapping("/asignar")
        @Operation(summary = "Asignar profesional a obra", description = "Asigna un profesional específico a una obra mediante validación por tipo y nombre. "
                        +
                        "Valida que el profesional exista en la tabla Profesionales y crea la asignación en Profesionales-Obras. "
                        +
                        "Campos requeridos: empresaId, obraId, profesional (tipo), nombre del profesional.")
        public ResponseEntity<AsignacionProfesionalResponse> asignarProfesionalPorTipo(
                        @Valid @RequestBody AsignarProfesionalRequest request) {
                AsignacionProfesionalResponse asignacionCreada = profesionalObraService
                                .asignarProfesionalPorTipoComoDTO(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(asignacionCreada);
        }

        @PostMapping("/asignar-multiples")
        @Operation(summary = "Asignar varios profesionales a una obra", description = "Permite asignar una lista de profesionales a una obra en una sola llamada. Valida que no estén ya asignados.")
        public ResponseEntity<List<AsignacionProfesionalResponse>> asignarMultiplesProfesionales(
                        @Valid @RequestBody AsignarProfesionalesBatchRequest request) {
                List<AsignacionProfesionalResponse> asignaciones = profesionalObraService
                                .asignarMultiplesProfesionales(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(asignaciones);
        }

        @GetMapping("/tipo/{tipoProfesional}")
        @Operation(summary = "Consultar asignaciones por especialidad", description = "Busca todas las asignaciones activas filtradas por tipo de profesional específico. "
                        + "Útil para ver qué obras tienen asignados profesionales de una especialidad particular. " +
                        "Acepta búsqueda flexible con variaciones de género y capitalización.")
        public ResponseEntity<List<AsignacionProfesionalResponse>> obtenerAsignacionesPorTipo(
                        @Parameter(description = "Tipo de profesional (ej: 'Oficial Albañil', 'Arquitecto', etc.)") @PathVariable String tipoProfesional,
                        @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

                List<AsignacionProfesionalResponse> asignaciones = profesionalObraService
                                .obtenerAsignacionesPorTipo(tipoProfesional, empresaId);
                return ResponseEntity.ok(asignaciones);
        }

        @GetMapping("/disponibilidad/{tipoProfesional}")
        @Operation(summary = "Consultar disponibilidad por tipo de profesional", description = "Muestra qué profesionales de un tipo específico están disponibles "
                        + "y cuáles están asignados a obras")
        public ResponseEntity<List<DisponibilidadProfesionalResponse>> consultarDisponibilidadPorTipo(
                        @Parameter(description = "Tipo de profesional (ej: 'Oficial Albañil', 'Arquitecto', etc.)") @PathVariable String tipoProfesional,
                        @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {

                // Se pasa el empresaId al servicio para una búsqueda segura y multi-tenant.
                List<DisponibilidadProfesionalResponse> disponibilidad = profesionalObraService
                                .obtenerDisponibilidadPorTipo(tipoProfesional, empresaId);

                return ResponseEntity.ok(disponibilidad);
        }

        @GetMapping("/profesionales/tipo/{tipoProfesional}")
        @Operation(summary = "Listar profesionales por tipo", description = "Obtiene todos los profesionales de un tipo específico con su información básica "
                        + "para facilitar la selección en asignaciones")
        public ResponseEntity<ListaProfesionalesResponse> listarProfesionalesPorTipo(
                        @Parameter(description = "Tipo de profesional (ej: 'Oficial Albañil', 'Arquitecto', etc.)") @PathVariable String tipoProfesional,
                        @Parameter(description = "ID de la empresa") @RequestParam(required = false) Long empresaId) {
                ListaProfesionalesResponse respuesta = profesionalObraService
                                .obtenerDisponibilidadProfesionalesPorTipo(tipoProfesional, empresaId);

                return ResponseEntity.ok(respuesta);
        }

        @PutMapping("/{asignacionId}")
        @Operation(summary = "Actualizar asignación existente", description = "Permite modificar uno o todos los campos de una asignación existente. "
                        + "Todos los campos son opcionales - solo se actualizarán los campos proporcionados. Requiere obraId.")
        public ResponseEntity<AsignacionProfesionalResponse> actualizarAsignacion(
                        @Parameter(description = "ID de la asignación a actualizar") @PathVariable Long asignacionId,
                        @RequestBody ActualizarAsignacionRequest request,
                        @Parameter(description = "ID de la empresa") @RequestParam(required = false) Long empresaId,
                        @Parameter(description = "ID de la obra asociada") @RequestParam Long obraId) {
                AsignacionProfesionalResponse asignacionActualizada = profesionalObraService
                                .actualizarAsignacionComoDTO(asignacionId, request, empresaId);
                return ResponseEntity.ok(asignacionActualizada);
        }

        @DeleteMapping("/{asignacionId}")
        @Operation(summary = "Desactivar asignación", description = "Desactiva una asignación (no la elimina físicamente, solo la marca como inactiva). Requiere empresaId, obraId y asignacionId.")
        public ResponseEntity<AsignacionProfesionalResponse> desactivarAsignacion(
                        @Parameter(description = "ID de la asignación a desactivar") @PathVariable Long asignacionId,
                        @Parameter(description = "ID de la empresa") @RequestParam(required = false) Long empresaId,
                        @Parameter(description = "ID de la obra") @RequestParam(required = false) Long obraId) {
                return ResponseEntity.ok(profesionalObraService.desactivarAsignacion(asignacionId, empresaId, obraId));
        }

        @GetMapping("/profesionales-por-obra")
        @Operation(summary = "Listar profesionales asignados a una obra de una empresa", description = "Devuelve todos los profesionales asignados a una obra específica de una empresa. Requiere empresaId y obraId.")
        public ResponseEntity<List<ProfesionalResponseDTO>> obtenerProfesionalesPorObraYEmpresa(
                        @RequestParam Long empresaId,
                        @RequestParam Long obraId) {
                List<ProfesionalResponseDTO> profesionales = profesionalObraService
                                .obtenerProfesionalesPorObraYEmpresa(empresaId, obraId);
                return ResponseEntity.ok(profesionales);
        }

        @GetMapping("/profesionales-por-obra/financiero")
        @Operation(summary = "Listar profesionales con datos financieros completos", 
                   description = "RECOMENDADO PARA SISTEMA DE ADELANTOS: Devuelve profesionales asignados a una obra con datos financieros completos: " +
                                "ID asignación, nombre, tipo, precio total, cantidad jornales, precio jornal, totales pagados, adelantos y saldo pendiente. " +
                                "Requiere empresaId y obraId.")
        public ResponseEntity<List<ProfesionalObraFinancieroDTO>> obtenerProfesionalesConDatosFinancieros(
                        @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId,
                        @Parameter(description = "ID de la obra", required = true) @RequestParam Long obraId) {
                List<ProfesionalObraFinancieroDTO> profesionales = profesionalObraService
                                .obtenerProfesionalesConDatosFinancieros(empresaId, obraId);
                return ResponseEntity.ok(profesionales);
        }

        @GetMapping("/profesionales-empresa/financiero")
        @Operation(summary = "Listar TODOS los profesionales de la empresa con datos financieros", 
                   description = "RECOMENDADO PARA GESTIÓN DE PAGOS CONSOLIDADA: Devuelve todos los profesionales asignados a TODAS las obras activas de la empresa " +
                                "con datos financieros completos agrupados por obra. Incluye: ID asignación, nombre profesional, tipo, obra asignada, " +
                                "precio total, cantidad jornales, precio jornal, totales pagados, adelantos y saldo pendiente. " +
                                "Útil para tener una vista consolidada de todos los profesionales y sus pagos pendientes. " +
                                "Solo requiere empresaId.")
        public ResponseEntity<List<ProfesionalObraFinancieroDTO>> obtenerTodosProfesionalesEmpresaConDatosFinancieros(
                        @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId) {
                List<ProfesionalObraFinancieroDTO> profesionales = profesionalObraService
                                .obtenerTodosProfesionalesEmpresaConDatosFinancieros(empresaId);
                return ResponseEntity.ok(profesionales);
        }

        @GetMapping("/obras-rubros-profesionales")
        @Operation(summary = "Listar obras agrupadas por rubro con profesionales asignados", 
                   description = "RECOMENDADO PARA GESTIÓN DE PAGOS POR RUBRO: Devuelve estructura jerárquica: Obra → Rubro → Profesionales. " +
                                "Cada obra contiene sus rubros del presupuesto aprobado, y cada rubro contiene los profesionales asignados " +
                                "con sus datos financieros completos (jornales asignados, utilizados, importes, saldos pendientes). " +
                                "Incluye totales consolidados por rubro y por obra. " +
                                "Solo requiere empresaId.")
        public ResponseEntity<List<ObraPagosDTO>> obtenerObrasPorRubroConProfesionales(
                        @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId) {
                List<ObraPagosDTO> obras = profesionalObraService
                                .obtenerObrasPorRubroConProfesionales(empresaId);
                return ResponseEntity.ok(obras);
        }

        @GetMapping("/profesionales-consolidados")
        @Operation(summary = "Listar profesionales con sus asignaciones consolidadas", 
                   description = "RECOMENDADO PARA GESTIÓN DE PAGOS POR PROFESIONAL: Devuelve estructura jerárquica: Profesional → Obras → Asignaciones. " +
                                "Cada profesional contiene todas las obras donde está asignado, y cada obra contiene las asignaciones específicas " +
                                "con datos financieros completos (jornales asignados, utilizados, importes, saldos pendientes por rubro). " +
                                "Incluye totales consolidados por obra y por profesional (saldo total pendiente). " +
                                "Ideal para ver cuánto se le debe a cada profesional en total. " +
                                "Solo requiere empresaId.")
        public ResponseEntity<List<ProfesionalConsolidadoDTO>> obtenerProfesionalesConsolidados(
                        @Parameter(description = "ID de la empresa", required = true) @RequestParam Long empresaId) {
                List<ProfesionalConsolidadoDTO> profesionales = profesionalObraService
                                .obtenerProfesionalesConsolidados(empresaId);
                return ResponseEntity.ok(profesionales);
        }

        /**
         * ============================================
         * ENDPOINTS DE DEPURACIÓN (TEMPORALES)
         * ============================================
         */
        /**
         * DEBUG: Ver todos los tipos de profesionales
         * @deprecated VIOLACIÓN MULTI-TENANCY: No filtra por empresaId, expone datos de todas las empresas
         */
        @Deprecated
        @GetMapping("/debug/tipos-profesionales")
        @Operation(summary = "🚫 DEPRECATED: Ver todos los tipos de profesionales", description = "Endpoint deprecated - violaba multi-tenancy al no filtrar por empresaId")
        public ResponseEntity<Map<String, Object>> debugTiposProfesionales() {
                log.warn("⚠️ SEGURIDAD: /debug/tipos-profesionales llamado - endpoint deprecated que exponía datos de todas las empresas");
                return ResponseEntity.ok(Map.of(
                        "error", "Este endpoint ha sido deshabilitado por seguridad",
                        "razon", "Exponía datos de todas las empresas sin filtrar por empresaId"
                ));
        }

        /**
         * ============================================
         * ENDPOINTS DE GESTIÓN DE CAJA CHICA
         * ============================================
         */
        
        @PutMapping("/{id}/asignar-caja-chica")
        @Operation(
                summary = "Asignar caja chica a profesional",
                description = "Asigna un monto de caja chica a un profesional en una obra. " +
                             "El saldo disponible se establece igual al monto asignado."
        )
        public ResponseEntity<?> asignarCajaChica(
                        @Parameter(description = "ID de la asignación profesional-obra", required = true)
                        @PathVariable Long id,
                        @Valid @RequestBody com.rodrigo.construccion.dto.request.AsignarCajaChicaRequest request) {
                try {
                        ProfesionalObra actualizado = profesionalObraService.asignarCajaChica(
                                id, request.getMonto(), request.getEmpresaId()
                        );
                        return ResponseEntity.ok(actualizado);
                } catch (com.rodrigo.construccion.exception.ResourceNotFoundException e) {
                        return ResponseEntity.notFound().build();
                } catch (SecurityException e) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
        }

        @GetMapping("/{id}/saldo-caja-chica")
        @Operation(
                summary = "Consultar saldo de caja chica",
                description = "Obtiene el saldo actual de caja chica de un profesional: " +
                             "monto asignado, saldo disponible, gastado y cantidad de gastos."
        )
        public ResponseEntity<?> obtenerSaldoCajaChica(
                        @Parameter(description = "ID de la asignación profesional-obra", required = true)
                        @PathVariable Long id,
                        @Parameter(description = "ID de la empresa", required = true)
                        @RequestParam Long empresaId) {
                try {
                        com.rodrigo.construccion.dto.response.SaldoCajaChicaResponse saldo = 
                                gastoService.obtenerSaldoCajaChica(id, empresaId);
                        return ResponseEntity.ok(saldo);
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.notFound().build();
                }
        }

}