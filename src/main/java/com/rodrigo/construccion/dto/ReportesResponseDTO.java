package com.rodrigo.construccion.dto;

import java.util.List;

/**
 * DTO para respuesta de listado de reportes del sistema
 */
public class ReportesResponseDTO {
    
    private List<ReporteArchivoDTO> auditorias;
    private List<ReporteArchivoDTO> backups;
    private Integer totalAuditorias;
    private Integer totalBackups;
    
    public ReportesResponseDTO() {
    }
    
    public ReportesResponseDTO(List<ReporteArchivoDTO> auditorias, 
                               List<ReporteArchivoDTO> backups) {
        this.auditorias = auditorias;
        this.backups = backups;
        this.totalAuditorias = auditorias != null ? auditorias.size() : 0;
        this.totalBackups = backups != null ? backups.size() : 0;
    }

    // Getters y Setters
    public List<ReporteArchivoDTO> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<ReporteArchivoDTO> auditorias) {
        this.auditorias = auditorias;
        this.totalAuditorias = auditorias != null ? auditorias.size() : 0;
    }

    public List<ReporteArchivoDTO> getBackups() {
        return backups;
    }

    public void setBackups(List<ReporteArchivoDTO> backups) {
        this.backups = backups;
        this.totalBackups = backups != null ? backups.size() : 0;
    }

    public Integer getTotalAuditorias() {
        return totalAuditorias;
    }

    public void setTotalAuditorias(Integer totalAuditorias) {
        this.totalAuditorias = totalAuditorias;
    }

    public Integer getTotalBackups() {
        return totalBackups;
    }

    public void setTotalBackups(Integer totalBackups) {
        this.totalBackups = totalBackups;
    }
}
