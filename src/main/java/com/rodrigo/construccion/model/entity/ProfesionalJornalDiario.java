package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro de tiempo trabajado por profesionales en obras por día.
 * 
 * Permite trackear:
 * - Profesionales trabajando en múltiples obras el mismo día  
 * - Profesionales asignados a RUBROS ESPECÍFICOS del presupuesto
 * - Horas trabajadas como decimal (1 = día completo, 0.5 = medio día, 0.25 = cuarto día)
 * - Monto cobrado calculado automáticamente (honorarioDia * horasTrabajadasDecimal)
 * 
 * Ejemplos:
 * - horasTrabajadasDecimal = 1.0 → trabajó día completo → cobra 100%
 * - horasTrabajadasDecimal = 0.5 → trabajó medio día → cobra 50%
 * - horasTrabajadasDecimal = 0.25 → trabajó cuarto de día → cobra 25%
 * 
 * Vinculación con presupuestos:
 * - El campo rubroId apunta a honorarios_por_rubro.id
 * - Cada presupuesto define sus propios rubros (ej: "Albañilería", "Plomería")
 * - El jornal queda registrado bajo un rubro específico del presupuesto de la obra
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profesional_jornales_diarios", 
    indexes = {
        @Index(name = "idx_jornales_profesional", columnList = "id_profesional"),
        @Index(name = "idx_jornales_obra", columnList = "id_obra"),
        @Index(name = "idx_jornales_rubro", columnList = "rubro_id"),
        @Index(name = "idx_jornales_fecha", columnList = "fecha"),
        @Index(name = "idx_jornales_empresa", columnList = "empresa_id"),
        @Index(name = "idx_jornales_profesional_fecha", columnList = "id_profesional,fecha"),
        @Index(name = "idx_jornales_obra_rubro", columnList = "id_obra,rubro_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_profesional_obra_rubro_fecha", 
            columnNames = {"id_profesional", "id_obra", "rubro_id", "fecha"}
        )
    }
)
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class ProfesionalJornalDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jornal_diario")
    private Long id;

    /**
     * Profesional que trabajó
     */
    @NotNull(message = "El profesional es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesional", nullable = false)
    @JsonBackReference
    private Profesional profesional;

    /**
     * Obra en la que trabajó
     */
    @NotNull(message = "La obra es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_obra", nullable = false)
    @JsonBackReference
    private Obra obra;

    /**
     * ID del rubro del presupuesto al que se asignó este jornal.
     * Apunta a honorarios_por_rubro.id
     * 
     * El presupuesto vinculado a la obra define los rubros disponibles.
     * Ejemplos: "Albañilería", "Plomería", "Electricidad", etc.
     */
    @NotNull(message = "El rubro es obligatorio")
    @Column(name = "rubro_id", nullable = false)
    private Long rubroId;

    /**
     * Fecha del trabajo
     */
    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /**
     * Horas trabajadas expresadas como decimal.
     * 
     * Representa la proporción del día trabajado:
     * - 1.0 = día completo (8 horas)
     * - 0.5 = medio día (4 horas)
     * - 0.25 = cuarto de día (2 horas)
     * - Valores entre 0.01 y 1.5 permitidos
     * 
     * Este valor se multiplica por honorarioDia para calcular montoCobrado.
     */
    @NotNull(message = "Las horas trabajadas son obligatorias")
    @DecimalMin(value = "0.01", message = "Las horas trabajadas deben ser mayor a 0")
    @DecimalMax(value = "1.5", message = "Las horas trabajadas no pueden exceder 1.5 (día y medio)")
    @Column(name = "horas_trabajadas_decimal", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasTrabajadasDecimal;

    /**
     * Tarifa diaria del profesional al momento del registro.
     * Se copia del campo honorarioDia del profesional para mantener histórico.
     */
    @NotNull(message = "La tarifa diaria es obligatoria")
    @Column(name = "tarifa_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaDiaria;

    /**
     * Monto cobrado calculado automáticamente.
     * 
     * Fórmula: tarifaDiaria * horasTrabajadasDecimal
     * 
     * Ejemplo:
     * - tarifaDiaria = 100000
     * - horasTrabajadasDecimal = 0.5
     * - montoCobrado = 50000
     */
    @Column(name = "monto_cobrado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoCobrado;

    /**
     * Observaciones opcionales sobre el trabajo realizado
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Campo para multi-tenancy
     */
    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    /**
     * Timestamps de auditoría
     */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Usuario que creó el registro (opcional)
     */
    @Column(name = "creado_por", length = 100)
    private String creadoPor;

    /**
     * Usuario que actualizó el registro (opcional)
     */
    @Column(name = "actualizado_por", length = 100)
    private String actualizadoPor;

    // Métodos de conveniencia

    /**
     * Calcula el monto cobrado basado en tarifaDiaria y horasTrabajadasDecimal
     */
    public void calcularMontoCobrado() {
        if (tarifaDiaria != null && horasTrabajadasDecimal != null) {
            this.montoCobrado = tarifaDiaria.multiply(horasTrabajadasDecimal);
        }
    }

    /**
     * Copia la tarifa diaria desde el profesional
     */
    public void copiarTarifaDesdeProfesional() {
        if (profesional != null && profesional.getHonorarioDia() != null) {
            this.tarifaDiaria = profesional.getHonorarioDia();
        }
    }

    /**
     * Inicializa el registro copiando datos del profesional y calculando monto
     */
    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        // Si no se especificó tarifaDiaria, copiar del profesional
        if (tarifaDiaria == null && profesional != null) {
            copiarTarifaDesdeProfesional();
        }
        
        // Calcular monto cobrado
        calcularMontoCobrado();
    }

    @Override
    public String toString() {
        return "ProfesionalJornalDiario{" +
                "id=" + id +
                ", profesional=" + (profesional != null ? profesional.getNombre() : "null") +
                ", obra=" + (obra != null ? obra.getNombre() : "null") +
                ", fecha=" + fecha +
                ", horasTrabajadasDecimal=" + horasTrabajadasDecimal +
                ", tarifaDiaria=" + tarifaDiaria +
                ", montoCobrado=" + montoCobrado +
                ", empresaId=" + empresaId +
                '}';
    }
}
