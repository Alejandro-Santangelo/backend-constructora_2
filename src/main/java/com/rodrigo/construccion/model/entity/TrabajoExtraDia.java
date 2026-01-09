package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad TrabajoExtraDia
 * Representa los días en los que se realizó un trabajo extra
 */
@Entity
@Table(name = "trabajos_extra_dias", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_trabajo_extra_fecha", columnNames = {"trabajo_extra_id", "fecha"})
    },
    indexes = {
        @Index(name = "idx_trabajos_extra_dias_trabajo", columnList = "trabajo_extra_id"),
        @Index(name = "idx_trabajos_extra_dias_fecha", columnList = "fecha")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trabajo_extra_id", nullable = false)
    private Long trabajoExtraId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_extra_id", insertable = false, updatable = false)
    private TrabajoExtra trabajoExtra;
}
