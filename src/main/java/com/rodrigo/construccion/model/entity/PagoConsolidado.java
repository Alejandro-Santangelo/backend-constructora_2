package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.TipoPagoConsolidado;
import com.rodrigo.construccion.enums.MetodoPago;
import com.rodrigo.construccion.enums.EstadoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pagos_material_obra", indexes = {
    @Index(name = "idx_pagos_material_empresa", columnList = "empresa_id"),
    @Index(name = "idx_pagos_material_presupuesto", columnList = "presupuesto_no_cliente_id"),
    @Index(name = "idx_pagos_material_tipo", columnList = "tipo_pago"),
    @Index(name = "idx_pagos_material_fecha", columnList = "fecha_pago"),
    @Index(name = "idx_pagos_material_estado", columnList = "estado"),
    @Index(name = "idx_pagos_material_calculadora", columnList = "material_calculadora_id")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class PagoConsolidado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== RELACIONES ==========
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private PresupuestoNoCliente presupuestoNoCliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_calculadora_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private MaterialCalculadora materialCalculadora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private ItemCalculadoraPresupuesto itemCalculadora;

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // ========== DATOS DEL PAGO ==========
    
    @NotNull(message = "El tipo de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", length = 50, nullable = false)
    private TipoPagoConsolidado tipoPago;

    @NotNull(message = "El concepto es obligatorio")
    @Column(name = "concepto", length = 500, nullable = false)
    private String concepto;

    @Column(name = "cantidad", precision = 15, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    @Column(name = "monto", precision = 15, scale = 2, nullable = false)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 50)
    private MetodoPago metodoPago;

    @NotNull(message = "La fecha de pago es obligatoria")
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 50, nullable = false)
    private EstadoPago estado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "comprobante_url", length = 500)
    private String comprobanteUrl;

    @Column(name = "numero_comprobante", length = 100)
    private String numeroComprobante;

    // ========== VINCULACIONES ADICIONALES (opcionales, para trazabilidad futura) ==========
    
    @Column(name = "profesional_calculadora_id")
    private Long profesionalCalculadoraId;

    @Column(name = "jornal_calculadora_id")
    private Long jornalCalculadoraId;

    @Column(name = "etapa_diaria_id")
    private Long etapaDiariaId;

    @Column(name = "gasto_general_id")
    private Long gastoGeneralId;

    // ========== AUDITORÍA ==========
    
    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    // ========== MÉTODOS DE NEGOCIO ==========
    
    /**
     * Valida que el pago sea consistente según su tipo.
     */
    public void validar() {
        // Si es pago de materiales, debe tener materialCalculadoraId
        if (tipoPago == TipoPagoConsolidado.MATERIALES && materialCalculadora == null) {
            throw new IllegalArgumentException(
                "Para pagos de tipo MATERIALES, se debe especificar el materialCalculadoraId");
        }
        
        // Si es gasto general, NO debe tener materialCalculadoraId
        if (tipoPago == TipoPagoConsolidado.GASTOS_GENERALES && materialCalculadora != null) {
            throw new IllegalArgumentException(
                "Para pagos de tipo GASTOS_GENERALES, no se debe especificar materialCalculadoraId");
        }
        
        // Validar monto positivo
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        
        // Validar fecha no sea futura
        if (fechaPago.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de pago no puede ser futura");
        }
    }

    /**
     * Marca el pago como anulado.
     */
    public void anular() {
        if (this.estado == EstadoPago.ANULADO) {
            throw new IllegalStateException("El pago ya está anulado");
        }
        this.estado = EstadoPago.ANULADO;
    }

    /**
     * Verifica si el pago está activo (pagado y no anulado).
     */
    public boolean estaActivo() {
        return this.estado == EstadoPago.PAGADO;
    }
}
