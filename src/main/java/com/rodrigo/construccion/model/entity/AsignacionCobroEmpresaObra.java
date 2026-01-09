package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad AsignacionCobroEmpresaObra
 * 
 * Vincula cobros de empresa con cobros de obra específicos, registrando
 * el monto asignado y permitiendo trazabilidad completa.
 * 
 * Relaciones:
 * - Un CobroEmpresa puede tener múltiples AsignacionCobroEmpresaObra
 * - Cada AsignacionCobroEmpresaObra referencia a un CobroObra específico
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asignaciones_cobro_empresa_obra", 
    indexes = {
        @Index(name = "idx_asig_cobro_empresa", columnList = "cobro_empresa_id"),
        @Index(name = "idx_asig_cobro_obra", columnList = "cobro_obra_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_cobro_empresa_obra", 
            columnNames = {"cobro_empresa_id", "cobro_obra_id"}
        )
    }
)
public class AsignacionCobroEmpresaObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== RELACIONES ==========
    
    @NotNull(message = "El cobro empresa es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cobro_empresa_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private CobroEmpresa cobroEmpresa;

    @NotNull(message = "El cobro obra es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cobro_obra_id", nullable = false)
    private CobroObra cobroObra;

    // ========== DATOS DE LA ASIGNACIÓN ==========
    
    @NotNull(message = "El monto asignado es obligatorio")
    @Positive(message = "El monto asignado debe ser mayor a cero")
    @Column(name = "monto_asignado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoAsignado;

    // ========== AUDITORÍA ==========
    
    @CreationTimestamp
    @Column(name = "fecha_asignacion", updatable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "usuario_asignacion", length = 100)
    private String usuarioAsignacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ========== MÉTODOS DE CONVENIENCIA ==========
    
    /**
     * Constructor de conveniencia para crear asignaciones
     */
    public AsignacionCobroEmpresaObra(CobroEmpresa cobroEmpresa, CobroObra cobroObra, BigDecimal montoAsignado) {
        this.cobroEmpresa = cobroEmpresa;
        this.cobroObra = cobroObra;
        this.montoAsignado = montoAsignado;
    }

    /**
     * Obtiene el ID del cobro empresa (útil para evitar lazy loading)
     */
    public Long getCobroEmpresaId() {
        return cobroEmpresa != null && cobroEmpresa.getId() != null ? cobroEmpresa.getId() : null;
    }

    /**
     * Obtiene el ID del cobro obra (útil para evitar lazy loading)
     */
    public Long getCobroObraId() {
        return cobroObra != null && cobroObra.getId() != null ? cobroObra.getId() : null;
    }

    /**
     * Obtiene el ID de la obra del cobro obra (útil para reportes)
     */
    public Long getObraId() {
        if (cobroObra == null) return null;
        Obra obra = cobroObra.getObra();
        return obra != null ? obra.getId() : null;
    }
}
