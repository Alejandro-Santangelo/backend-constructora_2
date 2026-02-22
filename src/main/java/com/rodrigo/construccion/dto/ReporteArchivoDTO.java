package com.rodrigo.construccion.dto;

import java.time.LocalDateTime;

/**
 * DTO para representar archivos de reportes de sistema (auditorías y backups)
 */
public class ReporteArchivoDTO {
    
    private String nombre;
    private String tipo; // "AUDITORIA" o "BACKUP"
    private Long tamanoBytes;
    private String tamanoLegible;
    private LocalDateTime fechaCreacion;
    private String rutaRelativa;
    
    public ReporteArchivoDTO() {
    }
    
    public ReporteArchivoDTO(String nombre, String tipo, Long tamanoBytes, 
                             String tamanoLegible, LocalDateTime fechaCreacion, 
                             String rutaRelativa) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tamanoBytes = tamanoBytes;
        this.tamanoLegible = tamanoLegible;
        this.fechaCreacion = fechaCreacion;
        this.rutaRelativa = rutaRelativa;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public String getTamanoLegible() {
        return tamanoLegible;
    }

    public void setTamanoLegible(String tamanoLegible) {
        this.tamanoLegible = tamanoLegible;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getRutaRelativa() {
        return rutaRelativa;
    }

    public void setRutaRelativa(String rutaRelativa) {
        this.rutaRelativa = rutaRelativa;
    }
}
