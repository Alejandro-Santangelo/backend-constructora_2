package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.ReporteArchivoDTO;
import com.rodrigo.construccion.dto.ReportesResponseDTO;
import com.rodrigo.construccion.service.ReporteSistemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Controlador REST para gestionar reportes del sistema
 * Permite al cliente ver y descargar reportes de auditoría y backups
 */
@RestController
@RequestMapping("/api/reportes-sistema")
public class ReporteSistemaController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReporteSistemaController.class);
    
    @Autowired
    private ReporteSistemaService reporteService;
    
    /**
     * Obtiene todos los reportes disponibles (auditorías y backups)
     * GET /api/reportes-sistema
     */
    @GetMapping
    public ResponseEntity<ReportesResponseDTO> obtenerTodosLosReportes() {
        try {
            logger.info("Obteniendo listado de todos los reportes del sistema");
            ReportesResponseDTO reportes = reporteService.obtenerTodosLosReportes();
            return ResponseEntity.ok(reportes);
        } catch (Exception e) {
            logger.error("Error al obtener reportes del sistema", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene solo los reportes de auditoría
     * GET /api/reportes-sistema/auditorias
     */
    @GetMapping("/auditorias")
    public ResponseEntity<List<ReporteArchivoDTO>> obtenerAuditorias() {
        try {
            logger.info("Obteniendo listado de auditorías");
            List<ReporteArchivoDTO> auditorias = reporteService.obtenerAuditorias();
            return ResponseEntity.ok(auditorias);
        } catch (Exception e) {
            logger.error("Error al obtener auditorías", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene solo los backups de base de datos
     * GET /api/reportes-sistema/backups
     */
    @GetMapping("/backups")
    public ResponseEntity<List<ReporteArchivoDTO>> obtenerBackups() {
        try {
            logger.info("Obteniendo listado de backups");
            List<ReporteArchivoDTO> backups = reporteService.obtenerBackups();
            return ResponseEntity.ok(backups);
        } catch (Exception e) {
            logger.error("Error al obtener backups", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Descarga un archivo específico (auditoría o backup)
     * GET /api/reportes-sistema/descargar/{tipo}/{nombreArchivo}
     * 
     * @param tipo AUDITORIA o BACKUP
     * @param nombreArchivo nombre del archivo a descargar
     */
    @GetMapping("/descargar/{tipo}/{nombreArchivo:.+}")
    public ResponseEntity<Resource> descargarArchivo(
            @PathVariable String tipo,
            @PathVariable String nombreArchivo) {
        try {
            logger.info("Descargando archivo {} del tipo {}", nombreArchivo, tipo);
            
            // Validar tipo
            if (!tipo.equals("AUDITORIA") && !tipo.equals("BACKUP")) {
                logger.error("Tipo de archivo inválido: {}", tipo);
                return ResponseEntity.badRequest().build();
            }
            
            Resource archivo = reporteService.obtenerArchivo(tipo, nombreArchivo);
            
            // Determinar content type según extensión
            String contentType = nombreArchivo.endsWith(".html") 
                ? MediaType.TEXT_HTML_VALUE 
                : "application/sql";
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + archivo.getFilename() + "\"")
                    .body(archivo);
                    
        } catch (SecurityException e) {
            logger.error("Intento de acceso no autorizado al archivo: {}", nombreArchivo, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IOException e) {
            logger.error("Error al descargar archivo: {}", nombreArchivo, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error inesperado al descargar archivo: {}", nombreArchivo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Visualiza un reporte de auditoría en el navegador (sin forzar descarga)
     * GET /api/reportes-sistema/ver/auditoria/{nombreArchivo}
     */
    @GetMapping("/ver/auditoria/{nombreArchivo:.+}")
    public ResponseEntity<Resource> verAuditoria(@PathVariable String nombreArchivo) {
        try {
            logger.info("Visualizando auditoría: {}", nombreArchivo);
            
            Resource archivo = reporteService.obtenerArchivo("AUDITORIA", nombreArchivo);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + archivo.getFilename() + "\"")
                    .body(archivo);
                    
        } catch (IOException e) {
            logger.error("Error al visualizar auditoría: {}", nombreArchivo, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    /**
     * Endpoint para limpiar reportes antiguos (mantener solo los últimos N)
     * POST /api/reportes-sistema/limpiar?mantener=12
     * 
     * Requeriría autenticación de administrador en producción
     */
    @PostMapping("/limpiar")
    public ResponseEntity<String> limpiarReportesAntiguos(
            @RequestParam(defaultValue = "12") int mantener) {
        try {
            logger.info("Limpiando reportes antiguos, manteniendo últimos {}", mantener);
            reporteService.limpiarReportesAntiguos(mantener);
            return ResponseEntity.ok("Reportes antiguos eliminados correctamente");
        } catch (Exception e) {
            logger.error("Error al limpiar reportes antiguos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al limpiar reportes: " + e.getMessage());
        }
    }    
    /**
     * Ejecuta manualmente el script de auditoría
     * POST /api/reportes-sistema/ejecutar/auditoria
     */
    @PostMapping("/ejecutar/auditoria")
    public ResponseEntity<String> ejecutarAuditoria() {
        try {
            logger.info("Ejecutando auditoría manualmente");
            String resultado = reporteService.ejecutarAuditoria();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("Error al ejecutar auditoría", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al ejecutar auditoría: " + e.getMessage());
        }
    }
    
    /**
     * Ejecuta manualmente el script de backup
     * POST /api/reportes-sistema/ejecutar/backup
     */
    @PostMapping("/ejecutar/backup")
    public ResponseEntity<String> ejecutarBackup() {
        try {
            logger.info("Ejecutando backup manualmente");
            String resultado = reporteService.ejecutarBackup();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("Error al ejecutar backup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al ejecutar backup: " + e.getMessage());
        }
    }}
