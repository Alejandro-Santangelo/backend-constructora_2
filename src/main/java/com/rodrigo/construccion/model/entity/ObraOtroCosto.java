package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.OrigenFondos;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad para asignaciones de otros costos a obras.
 * Soporta asignaciones semanales (sin fecha específica) y diarias (con fecha).
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

    @Column(name = "presupuesto_otro_costo_id")
    private Long presupuestoOtroCostoId;

    @Column(name = "gasto_general_id")
    private Long gastoGeneralId;

    @Column(name = "importe_asignado", nullable = false, precision = 15, scale = 2)
    private BigDecimal importeAsignado = BigDecimal.ZERO;

    @Column(name = "semana")
    private Integer semana;

    /**
     * Fecha de asignación específica (solo para asignaciones diarias).
     * NULL para asignaciones semanales.
     */
    @Column(name = "fecha_asignacion")
    private LocalDate fechaAsignacion;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Indica si es una asignación semanal (true) o diaria (false).
     * Semanales: aplican a toda la semana, sin fecha específica.
     * Diarias: asignadas a un día específico con fecha.
     */
    @Column(name = "es_semanal", nullable = false)
    private Boolean esSemanal = false;

    /**
     * Indica si el gasto fue creado manualmente (true) o vinculado a un presupuesto (false).
     */
    @Column(name = "es_manual", nullable = false)
    private Boolean esManual = false;

    /**
     * Indica si es una asignación global (true) que aplica a toda la obra
     * o una asignación específica a un item/etapa (false).
     */
    @Column(name = "es_global", nullable = false)
    private Boolean esGlobal = false;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    /**
     * Indica el origen de los fondos cuando el presupuesto es 0.
     * Permite rastrear si los fondos provienen de un retiro directo del socio
     * o del presupuesto de materiales.
     * Opcional (puede ser null).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "origen_fondos", length = 30)
    private OrigenFondos origenFondos;
}