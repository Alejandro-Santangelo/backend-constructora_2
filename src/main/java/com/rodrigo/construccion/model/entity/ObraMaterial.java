package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Relación N:N entre Obra y Material del Presupuesto
 * Permite asignar materiales específicos del presupuesto a una obra
 */
@Entity
@Table(name = "asignaciones_material_obra", 
       uniqueConstraints = @UniqueConstraint(
           name = "uq_asignaciones_material_obra", 
           columnNames = {"obra_id", "material_calculadora_id"}))
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObraMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Column(name = "material_calculadora_id")
    private Long materialCalculadoraId;

    @Column(name = "cantidad_asignada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadAsignada = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "semana")
    private Integer semana;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", insertable = false, updatable = false)
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_calculadora_id", insertable = false, updatable = false)
    private MaterialCalculadora materialCalculadora;
}
