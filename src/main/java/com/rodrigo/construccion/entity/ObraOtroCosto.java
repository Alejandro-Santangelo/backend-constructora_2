package com.rodrigo.construccion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para asignaciones de otros costos a obras
 */
@Entity
@Table(name = "asignaciones_otro_costo_obra")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObraOtroCosto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Column(name = "importe_asignado", nullable = false, precision = 15, scale = 2)
    private BigDecimal importeAsignado = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "semana")
    private Integer semana;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "gasto_general_id")
    private Long gastoGeneralId;
}