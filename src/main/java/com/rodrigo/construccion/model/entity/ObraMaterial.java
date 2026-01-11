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
 * Relación N:N entre Obra y Material del Presupuesto o Material Global
 * Soporta dos modos:
 * - ELEMENTO_DETALLADO: Material del presupuesto (materialCalculadoraId)
 * - CANTIDAD_GLOBAL: Material del catálogo general (materialCatalogoId)
 */
@Entity
@Table(name = "asignaciones_material_obra")
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

    @Column(name = "material_catalogo_id")
    private Long materialCatalogoId;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;

    @Column(name = "cantidad_asignada", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadAsignada = BigDecimal.ZERO;

    /**
     * Indica si es una asignación global (true) del modo CANTIDAD_GLOBAL
     * o del presupuesto (false) en modo ELEMENTO_DETALLADO.
     */
    @Column(name = "es_global", nullable = false)
    private Boolean esGlobal = false;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_catalogo_id", insertable = false, updatable = false)
    private Material materialCatalogo;
}
