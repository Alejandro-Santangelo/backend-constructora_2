package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad ProfesionalTareaDia
 * 
 * Tabla intermedia MUCHOS a MUCHOS entre tareas y profesionales asignados por día.
 * Permite que un profesional tenga varias tareas en un día y una tarea tenga varios profesionales.
 * 
 * IMPORTANTE: Se vincula con asignacion_profesional_dia (no directamente con profesionales)
 * para garantizar que solo se asignen profesionales que ya estén disponibles ese día.
 */
@Entity
@Table(name = "profesional_tarea_dia",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_tarea_asignacion", 
                         columnNames = {"tarea_id", "asignacion_dia_id"})
    },
    indexes = {
        @Index(name = "idx_profesional_tarea_tarea", columnList = "tarea_id"),
        @Index(name = "idx_profesional_tarea_asignacion", columnList = "asignacion_dia_id"),
        @Index(name = "idx_profesional_tarea_empresa", columnList = "empresa_id"),
        @Index(name = "idx_profesional_tarea_estado", columnList = "estado")
    }
)
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalTareaDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "La tarea es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", nullable = false)
    private TareaEtapaDiaria tarea;

    @NotNull(message = "La asignación del día es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_dia_id", nullable = false)
    private AsignacionProfesionalDia asignacionDia;

    @Column(name = "horas_asignadas", precision = 4, scale = 2)
    private BigDecimal horasAsignadas;

    @Column(name = "rol_en_tarea", length = 100)
    private String rolEnTarea; // Responsable, Ayudante, Supervisor, etc.

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "estado", length = 50)
    private String estado = "ASIGNADO"; // ASIGNADO, INICIADO, COMPLETADO

    @Column(name = "hora_inicio_real")
    private LocalTime horaInicioReal; // Para check-in

    @Column(name = "hora_fin_real")
    private LocalTime horaFinReal; // Para check-out

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
