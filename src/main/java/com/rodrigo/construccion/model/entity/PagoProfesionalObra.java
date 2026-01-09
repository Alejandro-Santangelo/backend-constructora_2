package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad PagoProfesionalObra
 * 
 * Representa los pagos efectuados a profesionales asignados a obras.
 * Incluye: pagos semanales, adelantos, premios, bonos y ajustes.
 * 
 * Lógica de cálculo:
 * - Pago semanal: Se calcula basándose en presentismo y días trabajados
 * - Adelantos: Se descuentan automáticamente de futuros pagos semanales
 * - Premios/Bonos: Pueden ser fijos o porcentuales
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pagos_profesional_obra", indexes = {
    @Index(name = "idx_pagos_profesional_obra", columnList = "profesional_obra_id"),
    @Index(name = "idx_pagos_empresa", columnList = "empresa_id"),
    @Index(name = "idx_pagos_tipo", columnList = "tipo_pago"),
    @Index(name = "idx_pagos_fecha", columnList = "fecha_pago"),
    @Index(name = "idx_pagos_periodo", columnList = "periodo_desde, periodo_hasta"),
    @Index(name = "idx_pagos_estado", columnList = "estado")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class PagoProfesionalObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relaciones
    @NotNull(message = "El profesional obra es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_obra_id", nullable = false)
    private ProfesionalObra profesionalObra;

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "presupuesto_no_cliente_id")
    private Long presupuestoNoClienteId;

    // Tipo y fecha del pago
    @NotNull(message = "El tipo de pago es obligatorio")
    @Column(name = "tipo_pago", length = 50, nullable = false)
    private String tipoPago;

    @NotNull(message = "La fecha de pago es obligatoria")
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    // Período (para pagos semanales)
    @Column(name = "periodo_desde")
    private LocalDate periodoDesde;

    @Column(name = "periodo_hasta")
    private LocalDate periodoHasta;

    // Semana (para pagos semanales)
    @Column(name = "semana")
    private Integer semana;

    // Montos - Campos requeridos por frontend
    @Column(name = "monto_bruto", precision = 15, scale = 2)
    private BigDecimal montoBruto;

    @PositiveOrZero(message = "El descuento de presentismo debe ser mayor o igual a cero")
    @Column(name = "descuento_presentismo", precision = 15, scale = 2)
    private BigDecimal descuentoPresentismo = BigDecimal.ZERO;

    @Column(name = "monto_neto", precision = 15, scale = 2)
    private BigDecimal montoNeto;

    // Montos - Campos legacy (mantener retrocompatibilidad)
    @NotNull(message = "El monto base es obligatorio")
    @Positive(message = "El monto base debe ser mayor a cero")
    @Column(name = "monto_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoBase;

    @PositiveOrZero(message = "El descuento de adelantos debe ser mayor o igual a cero")
    @Column(name = "descuento_adelantos", precision = 15, scale = 2)
    private BigDecimal descuentoAdelantos = BigDecimal.ZERO;

    @Column(name = "ajustes", precision = 15, scale = 2)
    private BigDecimal ajustes = BigDecimal.ZERO;

    @NotNull(message = "El monto final es obligatorio")
    @Column(name = "monto_final", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoFinal;

    // Datos de premios/bonos (si aplica)
    @Column(name = "premio_tipo", length = 20)
    private String premioTipo; // FIJO, PORCENTAJE

    @Column(name = "premio_valor", precision = 15, scale = 2)
    private BigDecimal premioValor;

    @Column(name = "premio_base", precision = 15, scale = 2)
    private BigDecimal premioBase;

    @Column(name = "premio_concepto", length = 500)
    private String premioConcepto;

    // Presentismo
    @Column(name = "dias_trabajados")
    private Integer diasTrabajados;

    @Column(name = "dias_esperados")
    private Integer diasEsperados;

    @Column(name = "porcentaje_presentismo", precision = 5, scale = 2)
    private BigDecimal porcentajePresentismo;

    // Información del comprobante
    @Column(name = "numero_recibo", length = 100)
    private String numeroRecibo;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "numero_comprobante", length = 100)
    private String numeroComprobante;

    // Estado
    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado", length = 50, nullable = false)
    private String estado = ESTADO_PAGADO;

    // Campos adicionales
    @Column(name = "concepto", length = 500)
    private String concepto;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "comprobante_url", length = 500)
    private String comprobanteUrl;

    // Auditoría
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_creacion_id")
    private Long usuarioCreacionId;

    @Column(name = "usuario_modificacion_id")
    private Long usuarioModificacionId;

    // ========== CONSTANTES DE TIPO DE PAGO ==========
    public static final String TIPO_SEMANAL = "SEMANAL";
    public static final String TIPO_ADELANTO = "ADELANTO";
    public static final String TIPO_PREMIO = "PREMIO";
    public static final String TIPO_BONO = "BONO";
    public static final String TIPO_AJUSTE = "AJUSTE";
    public static final String TIPO_LIQUIDACION_FINAL = "LIQUIDACION_FINAL";

    // ========== CONSTANTES DE ESTADO ==========
    public static final String ESTADO_PAGADO = "PAGADO";
    public static final String ESTADO_ANULADO = "ANULADO";

    // ========== CONSTANTES DE MÉTODO DE PAGO ==========
    public static final String METODO_TRANSFERENCIA = "TRANSFERENCIA";
    public static final String METODO_EFECTIVO = "EFECTIVO";
    public static final String METODO_CHEQUE = "CHEQUE";

    // ========== CONSTANTES DE TIPO PREMIO ==========
    public static final String PREMIO_TIPO_FIJO = "FIJO";
    public static final String PREMIO_TIPO_PORCENTAJE = "PORCENTAJE";

    // ========== MÉTODOS DE UTILIDAD ==========
    
    public boolean esPagoSemanal() {
        return TIPO_SEMANAL.equals(this.tipoPago);
    }

    public boolean esAdelanto() {
        return TIPO_ADELANTO.equals(this.tipoPago);
    }

    public boolean esPremio() {
        return TIPO_PREMIO.equals(this.tipoPago);
    }

    public boolean esBono() {
        return TIPO_BONO.equals(this.tipoPago);
    }

    public boolean estaPagado() {
        return ESTADO_PAGADO.equals(this.estado);
    }

    public boolean estaAnulado() {
        return ESTADO_ANULADO.equals(this.estado);
    }

    public boolean puedeSerModificado() {
        return !ESTADO_ANULADO.equals(this.estado);
    }

    // ========== MÉTODOS DE CÁLCULO ==========
    
    /**
     * Calcula el monto del premio si es porcentual
     */
    public BigDecimal calcularMontoPremio() {
        if (premioTipo == null || premioValor == null) {
            return BigDecimal.ZERO;
        }
        
        if (PREMIO_TIPO_FIJO.equals(premioTipo)) {
            return premioValor;
        } else if (PREMIO_TIPO_PORCENTAJE.equals(premioTipo) && premioBase != null) {
            return premioBase.multiply(premioValor)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Calcula el porcentaje de presentismo basado en días trabajados vs esperados
     */
    public BigDecimal calcularPorcentajePresentismo() {
        if (diasTrabajados == null || diasEsperados == null || diasEsperados == 0) {
            return BigDecimal.valueOf(100);
        }
        
        return BigDecimal.valueOf(diasTrabajados)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(diasEsperados), 2, RoundingMode.HALF_UP);
    }

    /**
     * Aplica el descuento por presentismo al monto base
     */
    public BigDecimal aplicarPresentismo(BigDecimal monto) {
        if (porcentajePresentismo == null || porcentajePresentismo.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return monto;
        }
        
        return monto.multiply(porcentajePresentismo)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el monto final considerando todos los factores:
     * - Monto base
     * - Presentismo
     * - Descuento de adelantos
     * - Ajustes
     * - Premios
     */
    public BigDecimal calcularMontoFinal() {
        BigDecimal total = montoBase != null ? montoBase : BigDecimal.ZERO;
        
        // Aplicar presentismo si corresponde
        if (esPagoSemanal() && porcentajePresentismo != null) {
            total = aplicarPresentismo(total);
        }
        
        // Descontar adelantos
        if (descuentoAdelantos != null) {
            total = total.subtract(descuentoAdelantos);
        }
        
        // Aplicar ajustes
        if (ajustes != null) {
            total = total.add(ajustes);
        }
        
        // Agregar premio si es un pago con premio
        if ((esPremio() || esBono()) && premioValor != null) {
            total = total.add(calcularMontoPremio());
        }
        
        return total.max(BigDecimal.ZERO); // No permitir montos negativos
    }

    // ========== MÉTODOS DE ESTADO ==========
    
    public void anular() {
        this.estado = ESTADO_ANULADO;
        this.fechaModificacion = LocalDateTime.now();
    }

    // ========== GETTERS DE CONVENIENCIA ==========
    
    public Long getProfesionalObraId() {
        return profesionalObra != null ? profesionalObra.getId() : null;
    }

    public String getNombreProfesional() {
        return profesionalObra != null ? profesionalObra.getNombreProfesional() : null;
    }

    public String getDireccionObra() {
        return profesionalObra != null ? profesionalObra.getDireccionObraCompleta() : null;
    }

    // ========== HOOKS DE JPA ==========
    
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaModificacion == null) {
            fechaModificacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = ESTADO_PAGADO;
        }
        if (fechaPago == null) {
            fechaPago = LocalDate.now();
        }
        if (descuentoAdelantos == null) {
            descuentoAdelantos = BigDecimal.ZERO;
        }
        if (descuentoPresentismo == null) {
            descuentoPresentismo = BigDecimal.ZERO;
        }
        if (ajustes == null) {
            ajustes = BigDecimal.ZERO;
        }
        // Calcular presentismo si no está establecido
        if (porcentajePresentismo == null && diasTrabajados != null && diasEsperados != null) {
            porcentajePresentismo = calcularPorcentajePresentismo();
        }
        // Calcular monto final si no está establecido
        if (montoFinal == null && montoBase != null) {
            montoFinal = calcularMontoFinal();
        }
        // Calcular montoNeto (frontend requirement)
        if (montoBruto != null) {
            montoNeto = montoBruto
                .subtract(descuentoAdelantos != null ? descuentoAdelantos : BigDecimal.ZERO)
                .subtract(descuentoPresentismo != null ? descuentoPresentismo : BigDecimal.ZERO);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
        // Recalcular montoNeto automáticamente
        if (montoBruto != null) {
            montoNeto = montoBruto
                .subtract(descuentoAdelantos != null ? descuentoAdelantos : BigDecimal.ZERO)
                .subtract(descuentoPresentismo != null ? descuentoPresentismo : BigDecimal.ZERO);
        }
    }
}
