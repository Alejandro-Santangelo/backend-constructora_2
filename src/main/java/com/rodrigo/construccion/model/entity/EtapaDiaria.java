package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.EstadoEtapaDiaria;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad EtapaDiaria
 * Representa una etapa diaria de avance en una obra
 */
@Entity
@Table(name = "etapas_diarias",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_obra_fecha", columnNames = {"obra_id", "fecha"})
    },
    indexes = {
        @Index(name = "idx_etapas_diarias_obra", columnList = "obra_id"),
        @Index(name = "idx_etapas_diarias_empresa", columnList = "empresa_id"),
        @Index(name = "idx_etapas_diarias_fecha", columnList = "fecha"),
        @Index(name = "idx_etapas_diarias_estado", columnList = "estado"),
        @Index(name = "idx_etapas_diarias_obra_fecha", columnList = "obra_id, fecha")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoEtapaDiaria estado;

    @Column(name = "nombre_etapa", length = 255)
    private String nombreEtapa;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "hora_inicio_estimada")
    private java.time.LocalTime horaInicioEstimada;

    @Column(name = "hora_fin_estimada")
    private java.time.LocalTime horaFinEstimada;

    @Column(name = "prioridad", length = 20)
    private String prioridad = "MEDIA"; // BAJA, MEDIA, ALTA, URGENTE

    @Column(name = "creado_por", length = 100)
    private String creadoPor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", insertable = false, updatable = false)
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", insertable = false, updatable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "etapaDiaria", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TareaEtapaDiaria> tareas = new ArrayList<>();
}
