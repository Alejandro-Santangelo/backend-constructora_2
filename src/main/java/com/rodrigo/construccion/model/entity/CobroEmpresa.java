package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.EstadoCobroEmpresa;
import com.rodrigo.construccion.enums.MetodoPago;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad CobroEmpresa
 * 
 * Representa cobros recibidos a nivel de empresa que pueden ser asignados
 * total o parcialmente a una o varias obras.
 * 
 * Permite:
 * - Registrar cobros sin asignarlos inmediatamente a obras
 * - Asignación total o parcial a múltiples obras
 * - Trazabilidad completa del flujo de dinero
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cobros_empresa", indexes = {
    @Index(name = "idx_cobros_empresa_empresa_id", columnList = "empresa_id"),
    @Index(name = "idx_cobros_empresa_estado", columnList = "estado"),
    @Index(name = "idx_cobros_empresa_fecha", columnList = "fecha_cobro"),
    @Index(name = "idx_cobros_empresa_empresa_estado", columnList = "empresa_id, estado")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class CobroEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Empresa
    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // ========== DATOS FINANCIEROS ==========
    
    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto total debe ser mayor a cero")
    @Column(name = "monto_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "monto_asignado", precision = 15, scale = 2)
    private BigDecimal montoAsignado = BigDecimal.ZERO;

    @NotNull(message = "El monto disponible es obligatorio")
    @Column(name = "monto_disponible", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoDisponible;

    // ========== INFORMACIÓN DEL COBRO ==========
    
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "La fecha de cobro es obligatoria")
    @Column(name = "fecha_cobro", nullable = false)
    private LocalDate fechaCobro;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 50)
    private MetodoPago metodoPago;

    @Column(name = "numero_comprobante", length = 100)
    private String numeroComprobante;

    @Column(name = "tipo_comprobante", length = 50)
    private String tipoComprobante;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // ========== ESTADO ==========
    
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20, nullable = false)
    private EstadoCobroEmpresa estado = EstadoCobroEmpresa.DISPONIBLE;

    // ========== AUDITORÍA ==========
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== RELACIONES ==========
    
    @OneToMany(mappedBy = "cobroEmpresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AsignacionCobroEmpresaObra> asignaciones = new ArrayList<>();

    // ========== MÉTODOS DE NEGOCIO ==========
    
    /**
     * Calcula el monto disponible basado en el total menos lo asignado
     */
    public void calcularMontoDisponible() {
        if (this.montoTotal != null && this.montoAsignado != null) {
            this.montoDisponible = this.montoTotal.subtract(this.montoAsignado);
        }
    }

    /**
     * Actualiza el estado basado en el monto disponible
     */
    public void actualizarEstado() {
        if (this.estado == EstadoCobroEmpresa.ANULADO) {
            return; // No cambiar estado si está anulado
        }
        
        if (this.montoDisponible == null || this.montoDisponible.compareTo(BigDecimal.ZERO) <= 0) {
            this.estado = EstadoCobroEmpresa.ASIGNADO_TOTAL;
        } else if (this.montoAsignado != null && this.montoAsignado.compareTo(BigDecimal.ZERO) > 0) {
            this.estado = EstadoCobroEmpresa.ASIGNADO_PARCIAL;
        } else {
            this.estado = EstadoCobroEmpresa.DISPONIBLE;
        }
    }

    /**
     * Agrega una asignación a una obra
     */
    public void agregarAsignacion(AsignacionCobroEmpresaObra asignacion) {
        this.asignaciones.add(asignacion);
    }

    /**
     * Remueve una asignación
     */
    public void removerAsignacion(AsignacionCobroEmpresaObra asignacion) {
        this.asignaciones.remove(asignacion);
    }

    // ========== LIFECYCLE HOOKS ==========
    
    @PrePersist
    @PreUpdate
    protected void onSave() {
        calcularMontoDisponible();
        actualizarEstado();
    }
}
