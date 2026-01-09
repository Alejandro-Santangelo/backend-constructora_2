package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad ProfesionalTareaEtapa
 * Relación N:N entre tareas de etapas diarias y profesionales
 */
@Entity
@Table(name = "profesionales_tarea_etapa", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_tarea_profesional", columnNames = {"tarea_id", "profesional_id"})
    },
    indexes = {
        @Index(name = "idx_profesionales_tarea_etapa_tarea", columnList = "tarea_id"),
        @Index(name = "idx_profesionales_tarea_etapa_profesional", columnList = "profesional_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesionalTareaEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tarea_id", nullable = false)
    private Long tareaId;

    @Column(name = "profesional_id", nullable = false)
    private Long profesionalId;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", updatable = false)
    private LocalDateTime fechaAsignacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", insertable = false, updatable = false)
    private TareaEtapaDiaria tarea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", insertable = false, updatable = false)
    private Profesional profesional;
}
