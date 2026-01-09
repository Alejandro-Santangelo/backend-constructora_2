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

/**
 * Entidad para pagos de gastos generales / otros costos.
 * Tabla: pagos_gastos_generales_obra
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pagos_gastos_generales_obra", indexes = {
    @Index(name = "idx_pagos_gastos_empresa", columnList = "empresa_id"),
    @Index(name = "idx_pagos_gastos_presupuesto", columnList = "presupuesto_no_cliente_id"),
    @Index(name = "idx_pagos_gastos_tipo_pago", columnList = "tipo_pago"),
    @Index(name = "idx_pagos_gastos_fecha_pago", columnList = "fecha_pago"),
    @Index(name = "idx_pagos_gastos_estado", columnList = "estado"),
    @Index(name = "idx_pagos_gastos_gasto_calculadora", columnList = "gasto_general_calculadora_id"),
    @Index(name = "idx_pagos_gastos_item_calculadora", columnList = "item_calculadora_id")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class PagoGastoGeneralObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== RELACIONES ==========
    
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

    // ========== VINCULACIONES ADICIONALES (opcionales) ==========
    
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

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "usuario_creacion_id")
    private Long usuarioCreacionId;

    @Column(name = "usuario_modificacion_id")
    private Long usuarioModificacionId;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    // ========== MÉTODOS DE NEGOCIO ==========
    
    /**
     * Valida que el pago sea consistente.
     */
    public void validar() {
        // Validar monto positivo
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }
        
        // Validar fecha no sea futura
        if (fechaPago.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de pago no puede ser futura");
        }
        
        // Validar que es un tipo válido (GASTOS_GENERALES u OTROS_COSTOS)
        if (tipoPago != TipoPagoConsolidado.GASTOS_GENERALES && 
            tipoPago != TipoPagoConsolidado.OTROS_COSTOS) {
            throw new IllegalArgumentException(
                "Tipo de pago inválido para gastos generales: " + tipoPago);
        }
    }

    /**
     * Marca el pago como anulado.
     */
    public void anular(String motivo) {
        if (this.estado == EstadoPago.ANULADO) {
            throw new IllegalStateException("El pago ya está anulado");
        }
        this.estado = EstadoPago.ANULADO;
        this.motivoAnulacion = motivo;
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Verifica si el pago está activo (pagado y no anulado).
     */
    public boolean estaActivo() {
        return this.estado == EstadoPago.PAGADO;
    }

    /**
     * Extrae el número de semana de las observaciones si existe.
     * Busca patrón [SEMANA X] en el campo observaciones.
     * 
     * @return Número de semana si existe, null si es pago total
     */
    public Integer extraerNumeroSemana() {
        if (observaciones == null || observaciones.isEmpty()) {
            return null;
        }
        
        // Patrón: [SEMANA 1], [SEMANA 2], etc.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[SEMANA\\s+(\\d+)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(observaciones);
        
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        return null; // Pago total (sin semana específica)
    }

    /**
     * Verifica si es un pago semanal (tiene [SEMANA X] en observaciones).
     */
    public boolean esPagoSemanal() {
        return extraerNumeroSemana() != null;
    }

    /**
     * Verifica si es un pago total (sin [SEMANA X] en observaciones).
     */
    public boolean esPagoTotal() {
        return !esPagoSemanal();
    }
}
