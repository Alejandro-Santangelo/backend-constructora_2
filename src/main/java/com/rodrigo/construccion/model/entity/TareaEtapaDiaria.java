package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad TareaEtapaDiaria
 * Representa tareas asociadas a etapas diarias de obra
 */
@Entity
@Table(name = "tareas_etapa_diaria", indexes = {
    @Index(name = "idx_tareas_etapa_diaria_etapa", columnList = "etapa_diaria_id"),
    @Index(name = "idx_tareas_etapa_diaria_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaEtapaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etapa_diaria_id", nullable = false)
    private Long etapaDiariaId;

    @Column(name = "nombre_tarea", length = 255)
    private String nombreTarea;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "hora_inicio")
    private java.time.LocalTime horaInicio;

    @Column(name = "hora_fin")
    private java.time.LocalTime horaFin;

    @Builder.Default
    @Column(name = "prioridad", length = 20)
    private String prioridad = "MEDIA"; // BAJA, MEDIA, ALTA, URGENTE

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etapa_diaria_id", insertable = false, updatable = false)
    private EtapaDiaria etapaDiaria;

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProfesionalTareaDia> profesionales = new ArrayList<>();
}
