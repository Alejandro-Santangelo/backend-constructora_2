package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "presupuesto_pdf")
public class PresupuestoPdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "presupuesto_id", nullable = false)
    private Long presupuestoId;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "contenido_pdf", nullable = false, columnDefinition = "BYTEA")
    private byte[] contenidoPdf;

    @Column(name = "tamanio_bytes", nullable = false)
    private Long tamanioBytes;

    @Column(name = "fecha_generacion")
    private LocalDateTime fechaGeneracion;

    @Column(name = "generado_por", length = 100)
    private String generadoPor;

    @Column(name = "version_presupuesto")
    private Integer versionPresupuesto;

    @Column(name = "incluye_honorarios")
    private Boolean incluyeHonorarios;

    @Column(name = "incluye_configuracion")
    private Boolean incluyeConfiguracion;

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

    public byte[] getContenidoPdf() {
        return contenidoPdf;
    }

    public void setContenidoPdf(byte[] contenidoPdf) {
        this.contenidoPdf = contenidoPdf;
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
