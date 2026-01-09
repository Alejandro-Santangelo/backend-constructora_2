package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.EstadoPagoTrabajoExtra;
import com.rodrigo.construccion.enums.EstadoTareaTrabajoExtra;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad TrabajoExtraTarea
 * Representa una tarea específica de un trabajo extra
 */
@Entity
@Table(name = "trabajos_extra_tareas", indexes = {
    @Index(name = "idx_trabajos_extra_tareas_trabajo", columnList = "trabajo_extra_id"),
    @Index(name = "idx_trabajos_extra_tareas_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraTarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trabajo_extra_id", nullable = false)
    private Long trabajoExtraId;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoTareaTrabajoExtra estado;

    @Column(name = "importe", precision = 15, scale = 2)
    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", length = 50)
    @Builder.Default
    private EstadoPagoTrabajoExtra estadoPago = EstadoPagoTrabajoExtra.PENDIENTE;

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_extra_id", insertable = false, updatable = false)
    private TrabajoExtra trabajoExtra;

    // Lista de índices de profesionales asignados a esta tarea
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "trabajos_extra_tareas_profesionales",
        joinColumns = @JoinColumn(name = "tarea_id")
    )
    @Column(name = "profesional_index")
    @Builder.Default
    private List<Integer> profesionalesIndices = new ArrayList<>();
}
