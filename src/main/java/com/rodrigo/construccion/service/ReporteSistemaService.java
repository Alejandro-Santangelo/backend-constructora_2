package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.ReporteArchivoDTO;
import com.rodrigo.construccion.dto.ReportesResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar reportes de auditoría y backups de la base de datos
 * Permite listar y descargar archivos desde el frontend
 */
@Service
public class ReporteSistemaService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReporteSistemaService.class);
    
    // Directorios configurables desde application.properties
    @Value("${reportes.auditoria.directorio:reportes-auditoria}")
    private String directorioAuditorias;
    
    @Value("${reportes.backup.directorio:db-backups}")
    private String directorioBackups;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;
    
    /**
     * Obtiene todos los reportes disponibles (auditorías y backups)
     */
    public ReportesResponseDTO obtenerTodosLosReportes() {
        List<ReporteArchivoDTO> auditorias = listarArchivos(directorioAuditorias, "AUDITORIA", "*.html");
        List<ReporteArchivoDTO> backups = listarArchivos(directorioBackups, "BACKUP", "*.sql");
        
        return new ReportesResponseDTO(auditorias, backups);
    }
    
    /**
     * Lista archivos de un directorio específico
     */
    private List<ReporteArchivoDTO> listarArchivos(String directorio, String tipo, String patron) {
        List<ReporteArchivoDTO> reportes = new ArrayList<>();
        
        try {
            Path dirPath = Paths.get(directorio);
            
            // Verificar si el directorio existe
            if (!Files.exists(dirPath)) {
                logger.warn("Directorio no existe: {}", directorio);
                return reportes;
            }
            
            // Crear matcher para el patrón
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + patron);
            
            // Listar archivos (usando try-with-resources para cerrar el Stream)
            List<Path> archivos;
            try (java.util.stream.Stream<Path> stream = Files.list(dirPath)) {
                archivos = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .sorted(Comparator.comparing((Path path) -> {
                            try {
                                return Files.getLastModifiedTime(path);
                            } catch (IOException e) {
                                return FileTime.fromMillis(0);
                            }
                        }).reversed()) // Más recientes primero
                        .collect(Collectors.toList());
            }
            
            // Convertir a DTOs
            for (Path archivo : archivos) {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(archivo, BasicFileAttributes.class);
                    long tamanoBytes = attrs.size();
                    LocalDateTime fechaCreacion = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(attrs.lastModifiedTime().toMillis()),
                        ZoneId.systemDefault()
                    );
                    
                    ReporteArchivoDTO dto = new ReporteArchivoDTO(
                        archivo.getFileName().toString(),
                        tipo,
                        tamanoBytes,
                        formatearTamano(tamanoBytes),
                        fechaCreacion,
                        dirPath.relativize(archivo).toString()
                    );
                    
                    reportes.add(dto);
                } catch (IOException e) {
                    logger.error("Error al leer atributos del archivo: {}", archivo, e);
                }
            }
            
        } catch (IOException e) {
            logger.error("Error al listar archivos del directorio: {}", directorio, e);
        }
        
        return reportes;
    }
    
    /**
     * Obtiene un archivo específico para descarga
     */
    public Resource obtenerArchivo(String tipo, String nombreArchivo) throws IOException {
        String directorio = tipo.equals("AUDITORIA") ? directorioAuditorias : directorioBackups;
        Path archivoPath = Paths.get(directorio).resolve(nombreArchivo).normalize();
        
        // Validar que el archivo esté dentro del directorio permitido
        Path directorioPath = Paths.get(directorio).normalize();
        if (!archivoPath.startsWith(directorioPath)) {
            throw new SecurityException("Acceso denegado al archivo: " + nombreArchivo);
        }
        
        // Verificar que el archivo existe
        if (!Files.exists(archivoPath)) {
            throw new IOException("Archivo no encontrado: " + nombreArchivo);
        }
        
        Resource resource = new UrlResource(archivoPath.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("No se puede leer el archivo: " + nombreArchivo);
        }
    }
    
    /**
     * Formatea el tamaño en bytes a formato legible (KB, MB, GB)
     */
    private String formatearTamano(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Obtiene solo las auditorías
     */
    public List<ReporteArchivoDTO> obtenerAuditorias() {
        return listarArchivos(directorioAuditorias, "AUDITORIA", "*.html");
    }
    
    /**
     * Obtiene solo los backups
     */
    public List<ReporteArchivoDTO> obtenerBackups() {
        return listarArchivos(directorioBackups, "BACKUP", "*.sql");
    }
    
    /**
     * Elimina reportes antiguos manteniendo solo los N más recientes
     */
    public void limpiarReportesAntiguos(int mantenerUltimos) {
        limpiarDirectorio(directorioAuditorias, "*.html", mantenerUltimos);
        limpiarDirectorio(directorioBackups, "*.sql", mantenerUltimos);
    }
    
    private void limpiarDirectorio(String directorio, String patron, int mantenerUltimos) {
        try {
            Path dirPath = Paths.get(directorio);
            
            if (!Files.exists(dirPath)) {
                return;
            }
            
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + patron);
            
            List<Path> archivos;
            try (java.util.stream.Stream<Path> stream = Files.list(dirPath)) {
                archivos = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .sorted(Comparator.comparing((Path path) -> {
                            try {
                                return Files.getLastModifiedTime(path);
                            } catch (IOException e) {
                                return FileTime.fromMillis(0);
                            }
                        }).reversed())
                        .collect(Collectors.toList());
            }
            
            // Eliminar archivos antiguos
            if (archivos.size() > mantenerUltimos) {
                for (int i = mantenerUltimos; i < archivos.size(); i++) {
                    try {
                        Files.delete(archivos.get(i));
                        logger.info("Archivo antiguo eliminado: {}", archivos.get(i).getFileName());
                    } catch (IOException e) {
                        logger.error("Error al eliminar archivo: {}", archivos.get(i), e);
                    }
                }
            }
            
        } catch (IOException e) {
            logger.error("Error al limpiar directorio: {}", directorio, e);
        }
    }
    
    /**
     * Ejecuta manualmente el script de auditoría de PowerShell
     */
    public String ejecutarAuditoria() throws IOException, InterruptedException {
        logger.info("Ejecutando script de auditoría manualmente");
        
        ProcessBuilder pb = new ProcessBuilder(
            "powershell.exe",
            "-ExecutionPolicy", "Bypass",
            "-File", ".\\ejecutar_auditoria_semanal.ps1"
        );
        
        // Configurar variable de entorno para la contraseña de PostgreSQL
        pb.environment().put("DB_PASSWORD", dbPassword);
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        // Leer output del proceso
        StringBuilder output = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.info("Script output: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode == 0) {
            logger.info("Auditoría ejecutada exitosamente");
            return "Auditoría ejecutada correctamente. Refresque la lista para ver el nuevo reporte.";
        } else {
            logger.error("Error al ejecutar auditoría. Exit code: {}", exitCode);
            throw new RuntimeException("Error al ejecutar script de auditoría. Código de salida: " + exitCode);
        }
    }
    
    /**
     * Ejecuta manualmente el script de backup de PowerShell
     */
    public String ejecutarBackup() throws IOException, InterruptedException {
        logger.info("Ejecutando script de backup manualmente");
        
        ProcessBuilder pb = new ProcessBuilder(
            "powershell.exe",
            "-ExecutionPolicy", "Bypass",
            "-File", ".\\ejecutar_backup_semanal.ps1"
        );
        
        // Configurar variable de entorno para la contraseña de PostgreSQL
        pb.environment().put("DB_PASSWORD", dbPassword);
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        // Leer output del proceso
        StringBuilder output = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                logger.info("Script output: {}", line);
            }
        }
        
        int exitCode = process.waitFor();
        
        if (exitCode == 0) {
            logger.info("Backup ejecutado exitosamente");
            return "Backup ejecutado correctamente. Refresque la lista para ver el nuevo archivo.";
        } else {
            logger.error("Error al ejecutar backup. Exit code: {}", exitCode);
            throw new RuntimeException("Error al ejecutar script de backup. Código de salida: " + exitCode);
        }
    }
}
