package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "costos", indexes = {
        @Index(name = "idx_costos_obra", columnList = "id_obra"),
        @Index(name = "idx_costos_fecha", columnList = "fecha"),
        @Index(name = "idx_costos_categoria", columnList = "categoria"),
        @Index(name = "idx_costos_estado", columnList = "estado")
})
@Filter(name = "empresaFilter", condition = "EXISTS (SELECT 1 FROM obras o JOIN cliente_empresa ce ON o.id_cliente = ce.id_cliente WHERE o.id_obra = id_obra AND ce.id_empresa = :empresaId)")
public class Costo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_costo")
    private Long id;

    @NotNull(message = "El concepto es obligatorio")
    @Column(name = "concepto", nullable = false, length = 500)
    private String concepto;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "categoria", length = 100)
    private String categoria = "General";

    @Column(name = "tipo_costo", length = 50)
    private String tipoCosto = "Directo";

    @Column(name = "estado", length = 50)
    private String estado = "Pendiente";

    @Column(name = "fecha_aprobacion")
    private LocalDate fechaAprobacion;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "imputable")
    private Boolean imputable = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Campos adicionales para compatibilidad con reportes existentes
    @Column(name = "semana")
    private Integer semana;

    @Column(name = "anio")
    private Integer anio;

    // Relaciones
    @JsonBackReference("obra-costos")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_obra", nullable = false)
    private Obra obra;

    // Getters de conveniencia
    public Long getObraId() {
        return obra != null ? obra.getId() : null;
    }

    public String getNombreObra() {
        return obra != null ? obra.getNombre() : null;
    }

    @PrePersist
    protected void onCreate() {
        if (fecha != null) {
            // Calcular semana y año automáticamente
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
            this.semana = fecha.get(weekFields.weekOfYear());
            this.anio = fecha.getYear();
        }
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
        if (fecha != null) {
            // Recalcular semana y año si cambia la fecha
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
            this.semana = fecha.get(weekFields.weekOfYear());
            this.anio = fecha.getYear();
        }
    }

}
