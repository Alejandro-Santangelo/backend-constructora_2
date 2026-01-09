package com.rodrigo.construccion.dto.response;

import java.time.LocalDateTime;

public class PresupuestoPdfResponseDTO {

    private Long id;
    private Long presupuestoId;
    private String nombreArchivo;
    private Long tamanioBytes;
    private LocalDateTime fechaGeneracion;
    private String generadoPor;
    private Integer versionPresupuesto;
    private Boolean incluyeHonorarios;
    private Boolean incluyeConfiguracion;

    // Constructores
    public PresupuestoPdfResponseDTO() {
    }

    public PresupuestoPdfResponseDTO(Long id, Long presupuestoId, String nombreArchivo, Long tamanioBytes,
                                     LocalDateTime fechaGeneracion, String generadoPor, Integer versionPresupuesto,
                                     Boolean incluyeHonorarios, Boolean incluyeConfiguracion) {
        this.id = id;
        this.presupuestoId = presupuestoId;
        this.nombreArchivo = nombreArchivo;
        this.tamanioBytes = tamanioBytes;
        this.fechaGeneracion = fechaGeneracion;
        this.generadoPor = generadoPor;
        this.versionPresupuesto = versionPresupuesto;
        this.incluyeHonorarios = incluyeHonorarios;
        this.incluyeConfiguracion = incluyeConfiguracion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(Long presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public Long getTamanioBytes() {
        return tamanioBytes;
    }

    public void setTamanioBytes(Long tamanioBytes) {
        this.tamanioBytes = tamanioBytes;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getGeneradoPor() {
        return generadoPor;
    }

    public void setGeneradoPor(String generadoPor) {
        this.generadoPor = generadoPor;
    }

    public Integer getVersionPresupuesto() {
        return versionPresupuesto;
    }

    public void setVersionPresupuesto(Integer versionPresupuesto) {
        this.versionPresupuesto = versionPresupuesto;
    }

    public Boolean getIncluyeHonorarios() {
        return incluyeHonorarios;
    }

    public void setIncluyeHonorarios(Boolean incluyeHonorarios) {
        this.incluyeHonorarios = incluyeHonorarios;
    }

    public Boolean getIncluyeConfiguracion() {
        return incluyeConfiguracion;
    }

    public void setIncluyeConfiguracion(Boolean incluyeConfiguracion) {
        this.incluyeConfiguracion = incluyeConfiguracion;
    }
}
