package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad AsignacionProfesionalObra
 * Representa la asignación de un profesional a un rubro específico de un presupuesto vinculado a una obra.
 * Permite asignaciones por ROL (solo el profesional) o por JORNAL (consume jornales del total disponible).
 * Diferencia con ProfesionalObra:
 * - ProfesionalObra: Asigna profesional a obra completa (sin rubro específico)
 * - AsignacionProfesionalObra: Asigna profesional a RUBRO del presupuesto de la obra
 */
@Entity
@Table(name = "asignaciones_profesional_obra", indexes = {
        @Index(name = "idx_asignacion_obra", columnList = "obra_id"),
        @Index(name = "idx_asignacion_profesional", columnList = "profesional_id"),
        @Index(name = "idx_asignacion_rubro", columnList = "rubro_id"),
        @Index(name = "idx_asignacion_tipo", columnList = "tipo_asignacion"),
        @Index(name = "idx_asignacion_estado", columnList = "estado"),
        @Index(name = "idx_asignacion_profesional_tipo", columnList = "profesional_id, tipo_asignacion")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionProfesionalObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // ==================== RELACIONES Y SUS IDs ====================

    @NotNull(message = "El profesional es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false, insertable = false, updatable = false)
    private Profesional profesional;

    @Column(name = "profesional_id", nullable = false)
    private Long profesionalId;

    @NotNull(message = "La obra es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false, insertable = false, updatable = false)
    private Obra obra;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id")
    private PresupuestoNoCliente presupuestoNoCliente;

    @Column(name = "presupuesto_no_cliente_id", insertable = false, updatable = false)
    private Long presupuestoNoClienteId;

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // ==================== CAMPOS DEL RUBRO ====================

    // rubroId puede ser NULL para asignaciones globales (sin presupuesto específico)
    @Column(name = "rubro_id")
    private Long rubroId;

    @Column(name = "item_id")
    private Long itemId;

    // rubroNombre puede ser NULL para asignaciones globales
    @Column(name = "rubro_nombre")
    private String rubroNombre;

    // ==================== CAMPOS DE ASIGNACIÓN ====================

    @NotBlank(message = "El tipo de asignación es obligatorio")
    @Column(name = "tipo_asignacion", nullable = false, length = 50)
    private String tipoAsignacion; // PROFESIONAL o JORNAL

    @Column(name = "importe_jornal", precision = 15, scale = 2)
    private BigDecimal importeJornal;

    @PositiveOrZero(message = "La cantidad de jornales debe ser mayor o igual a cero")
    @Column(name = "cantidad_jornales", precision = 10, scale = 2)
    private BigDecimal cantidadJornales;

    @PositiveOrZero(message = "Los jornales utilizados deben ser mayor o igual a cero")
    @Column(name = "jornales_utilizados", precision = 10, scale = 2)
    private BigDecimal jornalesUtilizados = BigDecimal.ZERO;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @NotBlank(message = "El estado es obligatorio")
    @Column(name = "estado", nullable = false, length = 50)
    private String estado = "ACTIVO"; // ACTIVO, FINALIZADO, CANCELADO

    // Campos adicionales de la tabla (precargados para evitar joins)
    @Column(name = "profesional_tipo", length = 100)
    private String profesionalTipo;

    @Column(name = "profesional_nombre")
    private String profesionalNombre;

    // Campos para asignación semanal (NUEVO)
    @Column(name = "modalidad", length = 50)
    private String modalidad; // 'total' | 'semanal' | NULL (legacy)

    @Column(name = "semanas_objetivo")
    private Integer semanasObjetivo;

    // ==================== AUDITORÍA ====================

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // ==================== MÉTODOS DE CONVENIENCIA ====================

    public BigDecimal getJornalesRestantes() {
        if (cantidadJornales == null || jornalesUtilizados == null) {
            return BigDecimal.ZERO;
        }
        return cantidadJornales.subtract(jornalesUtilizados);
    }

    /**
     * Hook para sincronizar campos desnormalizados antes de persistir
     */
    @PrePersist
    @PreUpdate
    public void sincronizarCamposProfesional() {
        if (profesional != null) {
            this.profesionalNombre = profesional.getNombre();
            this.profesionalTipo = profesional.getTipoProfesional();
        }
    }
}
