package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad CajaChicaObra
 * 
 * Representa la asignación de caja chica a profesionales en obras.
 * Permite tracking de fondos asignados, rendidos y anulados.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "caja_chica_obra", indexes = {
    @Index(name = "idx_caja_chica_empresa", columnList = "empresa_id"),
    @Index(name = "idx_caja_chica_presupuesto", columnList = "presupuesto_no_cliente_id"),
    @Index(name = "idx_caja_chica_profesional", columnList = "profesional_obra_id"),
    @Index(name = "idx_caja_chica_estado", columnList = "estado"),
    @Index(name = "idx_caja_chica_fecha", columnList = "fecha")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class CajaChicaObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @NotNull(message = "El presupuesto es obligatorio")
    @Column(name = "presupuesto_no_cliente_id", nullable = false)
    private Long presupuestoNoClienteId;

    @NotNull(message = "El profesional es obligatorio")
    @Column(name = "profesional_obra_id", nullable = false)
    private Long profesionalObraId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado", length = 50, nullable = false)
    private String estado = ESTADO_ACTIVO;

    // Auditoría
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== CONSTANTES DE ESTADO ==========
    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_RENDIDO = "RENDIDO";
    public static final String ESTADO_ANULADO = "ANULADO";

    // ========== MÉTODOS DE UTILIDAD ==========
    
    public boolean estaActivo() {
        return ESTADO_ACTIVO.equals(this.estado);
    }

    public boolean estaRendido() {
        return ESTADO_RENDIDO.equals(this.estado);
    }

    public boolean estaAnulado() {
        return ESTADO_ANULADO.equals(this.estado);
    }

    public boolean puedeSerModificado() {
        return ESTADO_ACTIVO.equals(this.estado);
    }

    // ========== MÉTODOS DE ESTADO ==========
    
    public void rendir() {
        if (!estaActivo()) {
            throw new IllegalStateException("Solo se puede rendir caja chica en estado ACTIVO");
        }
        this.estado = ESTADO_RENDIDO;
    }

    public void anular() {
        if (estaRendido()) {
            throw new IllegalStateException("No se puede anular caja chica ya rendida");
        }
        this.estado = ESTADO_ANULADO;
    }

    // ========== HOOKS DE JPA ==========
    
    @PrePersist
    protected void onCreate() {
        if (estado == null || estado.isEmpty()) {
            estado = ESTADO_ACTIVO;
        }
        if (fecha == null) {
            fecha = LocalDate.now();
        }
    }
}
