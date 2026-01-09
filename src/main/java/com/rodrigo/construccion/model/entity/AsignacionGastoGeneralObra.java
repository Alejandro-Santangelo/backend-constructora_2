package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para asignaciones de gastos generales a obras.
 * Representa la asignación de un gasto general a una obra (sin pago todavía).
 * Similar a AsignacionMaterialObra y AsignacionProfesionalObra.
 * Tabla: asignaciones_gasto_general_obra
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asignaciones_gasto_general_obra", indexes = {
    @Index(name = "idx_asignaciones_gastos_obra", columnList = "obra_id"),
    @Index(name = "idx_asignaciones_gastos_empresa_new", columnList = "empresa_id"),
    @Index(name = "idx_asignaciones_gastos_presupuesto_new", columnList = "presupuesto_no_cliente_id"),
    @Index(name = "idx_asignaciones_gastos_item_new", columnList = "item_calculadora_id"),
    @Index(name = "idx_asignaciones_gastos_gasto_calc", columnList = "gasto_general_calculadora_id"),
    @Index(name = "idx_asignaciones_gastos_semana", columnList = "semana")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class AsignacionGastoGeneralObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== RELACIONES ==========
    
    @NotNull(message = "La obra es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Obra obra;

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private PresupuestoNoCliente presupuestoNoCliente;

    @Column(name = "gasto_general_calculadora_id")
    private Long gastoGeneralCalculadoraId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private ItemCalculadoraPresupuesto itemCalculadora;

    // ========== DATOS DE LA ASIGNACIÓN ==========
    
    @NotNull(message = "La descripción es obligatoria")
    @Column(name = "descripcion", length = 500, nullable = false)
    private String descripcion;

    @Column(name = "cantidad_asignada", precision = 15, scale = 2)
    private BigDecimal cantidadAsignada;

    @Column(name = "precio_unitario", precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "El importe asignado es obligatorio")
    @Column(name = "importe_asignado", precision = 15, scale = 2, nullable = false)
    private BigDecimal importeAsignado;

    @Column(name = "semana")
    private Integer semana;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ========== AUDITORÍA ==========
    
    @NotNull
    @CreationTimestamp
    @Column(name = "fecha_asignacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // ========== MÉTODOS DE NEGOCIO ==========
    
    /**
     * Valida que la asignación sea consistente.
     */
    public void validar() {
        if (importeAsignado == null || importeAsignado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe asignado debe ser mayor a 0");
        }
        
        if (obra == null) {
            throw new IllegalArgumentException("La obra es obligatoria");
        }
        
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
    }

    /**
     * Calcula el importe asignado basado en cantidad y precio unitario.
     */
    public void calcularImporte() {
        if (cantidadAsignada != null && precioUnitario != null) {
            this.importeAsignado = cantidadAsignada.multiply(precioUnitario);
        }
    }

    /**
     * Verifica si la asignación es semanal.
     */
    public boolean esSemanal() {
        return semana != null && semana > 0;
    }

    /**
     * Verifica si la asignación es total.
     */
    public boolean esTotal() {
        return !esSemanal();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        validar();
        if (importeAsignado == null && cantidadAsignada != null && precioUnitario != null) {
            calcularImporte();
        }
    }
}
