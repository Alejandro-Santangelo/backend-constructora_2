package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.scheduler.ObraEstadoScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de administración para operaciones manuales del sistema
 * 
 * Endpoints útiles para:
 * - Testing de schedulers
 * - Ejecución manual de tareas programadas
 * - Mantenimiento del sistema
 * 
 * @author Sistema Rodrigo
 * @since 2025-12-16
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Endpoints de administración del sistema")
public class AdminController {

    private final ObraEstadoScheduler obraEstadoScheduler;

    /**
     * Ejecuta manualmente el proceso de actualización automática de estados
     * 
     * Este endpoint permite probar el scheduler sin esperar a las 7:00 AM
     * 
     * @return Mensaje de confirmación
     */
    @PostMapping("/ejecutar-actualizacion-estados")
    @Operation(
        summary = "Ejecutar actualización de estados manualmente",
        description = "Ejecuta el proceso de cambio automático de estados de presupuestos y obras. " +
                      "Útil para testing y para forzar actualizaciones fuera del horario programado (7:00 AM)."
    )
    public ResponseEntity<Map<String, Object>> ejecutarActualizacionEstados() {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("🔧 EJECUCIÓN MANUAL solicitada desde endpoint de administración");
        log.info("═══════════════════════════════════════════════════════════════");
        
        try {
            // Ejecutar el scheduler manualmente
            obraEstadoScheduler.ejecutarManualmente();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "✅ Proceso de actualización de estados ejecutado correctamente");
            response.put("timestamp", ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));
            response.put("nota", "Revisa los logs del servidor para ver detalles de los cambios realizados");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error en ejecución manual del scheduler", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "❌ Error al ejecutar el proceso: " + e.getMessage());
            response.put("timestamp", ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Obtiene información sobre los schedulers configurados
     * 
     * @return Información de configuración
     */
    @GetMapping("/info-schedulers")
    @Operation(
        summary = "Obtener información de schedulers",
        description = "Devuelve información sobre las tareas programadas configuradas en el sistema"
    )
    public ResponseEntity<Map<String, Object>> infoSchedulers() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> obraEstadoInfo = new HashMap<>();
        obraEstadoInfo.put("nombre", "ObraEstadoScheduler");
        obraEstadoInfo.put("descripcion", "Actualización automática de estados de presupuestos y obras");
        obraEstadoInfo.put("horario", "7:00 AM diario (Argentina)");
        obraEstadoInfo.put("cron", "0 0 7 * * ?");
        obraEstadoInfo.put("zonaHoraria", "America/Argentina/Buenos_Aires");
        obraEstadoInfo.put("acciones", new String[]{
            "APROBADO → EN_EJECUCION (cuando llega fechaProbableInicio)",
            "EN_EJECUCION → TERMINADO (cuando se cumple el tiempo estimado)"
        });
        
        response.put("scheduler", obraEstadoInfo);
        response.put("horaServidor", ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));
        response.put("endpointEjecucionManual", "/api/admin/ejecutar-actualizacion-estados");
        
        return ResponseEntity.ok(response);
    }
}
