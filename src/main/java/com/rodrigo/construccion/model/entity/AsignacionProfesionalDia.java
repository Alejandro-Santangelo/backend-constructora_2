package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad AsignacionProfesionalDia
 * 
 * Representa el detalle día a día de jornales asignados a un profesional en una obra.
 * Complementa {@link AsignacionProfesionalObra} cuando la modalidad es 'semanal' o 'total'.
 * 
 * Permite especificar cantidades diferentes de jornales para cada día laboral,
 * facilitando la planificación detallada por semana.
 */
@Entity
@Table(name = "asignacion_profesional_dia", 
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_asignacion_fecha", 
                         columnNames = {"asignacion_id", "fecha"})
    },
    indexes = {
        @Index(name = "idx_asignacion_dia_asignacion", columnList = "asignacion_id"),
        @Index(name = "idx_asignacion_dia_fecha", columnList = "fecha"),
        @Index(name = "idx_asignacion_dia_semana", columnList = "semana_iso"),
        @Index(name = "idx_asignacion_dia_empresa", columnList = "empresa_id"),
        @Index(name = "idx_asignacion_dia_empresa_fecha", columnList = "empresa_id, fecha")
    }
)
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionProfesionalDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La asignación es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", nullable = false)
    private AsignacionProfesionalObra asignacion;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La cantidad es obligatoria")
    @PositiveOrZero(message = "La cantidad debe ser mayor o igual a cero")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "semana_iso", length = 10)
    private String semanaIso; // Formato: YYYY-Www (ej: 2025-W49)

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    // Métodos de conveniencia
    public Long getAsignacionId() {
        return asignacion != null ? asignacion.getId() : null;
    }
}
