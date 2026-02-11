package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.EstadoPagoTrabajoExtra;
import com.rodrigo.construccion.enums.TipoProfesionalTrabajoExtra;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad TrabajoExtroProfesional
 * Representa un profesional asignado a un trabajo extra
 */
@Entity
@Table(name = "trabajos_extra_profesionales", indexes = {
    @Index(name = "idx_trabajos_extra_prof_trabajo", columnList = "trabajo_extra_id"),
    @Index(name = "idx_trabajos_extra_prof_profesional", columnList = "profesional_id"),
    @Index(name = "idx_trabajos_extra_prof_tipo", columnList = "tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtroProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trabajo_extra_id", nullable = false)
    private Long trabajoExtraId;

    @Column(name = "profesional_id")
    private Long profesionalId;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "especialidad", length = 255)
    private String especialidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoProfesionalTrabajoExtra tipo;

    @Builder.Default
    @Column(name = "importe", nullable = false, precision = 15, scale=  2)
    private java.math.BigDecimal importe = java.math.BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", length = 50)
    @Builder.Default
    private EstadoPagoTrabajoExtra estadoPago = EstadoPagoTrabajoExtra.PENDIENTE;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_extra_id", insertable = false, updatable = false)
    private TrabajoExtra trabajoExtra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", insertable = false, updatable = false)
    private Profesional profesional;
}
