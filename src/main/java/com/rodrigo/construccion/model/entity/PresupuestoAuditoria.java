package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "presupuestos_auditoria")
public class PresupuestoAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "presupuesto_id", nullable = false)
    private Long presupuestoId;

    @Column(name = "datos_json", columnDefinition = "TEXT", nullable = false)
    private String datosJson;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_modificador", length = 100)
    private String usuarioModificador;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPresupuestoId() { return presupuestoId; }
    public void setPresupuestoId(Long presupuestoId) { this.presupuestoId = presupuestoId; }
    public String getDatosJson() { return datosJson; }
    public void setDatosJson(String datosJson) { this.datosJson = datosJson; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
    public String getUsuarioModificador() { return usuarioModificador; }
    public void setUsuarioModificador(String usuarioModificador) { this.usuarioModificador = usuarioModificador; }
}
