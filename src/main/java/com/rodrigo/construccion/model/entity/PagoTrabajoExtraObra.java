package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.enums.MetodoPago;
import com.rodrigo.construccion.enums.TipoPagoTrabajoExtra;
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
 * Entidad PagoTrabajoExtraObra
 * 
 * Registra los pagos realizados para trabajos extra de una obra.
 * Permite registrar pagos generales del trabajo, o pagos específicos a profesionales/tareas.
 * 
 * Lógica de negocio:
 * - Un pago puede ser general (al trabajo completo) o específico (a un profesional o tarea)
 * - Solo uno de los campos (trabajoExtraProfesionalId o trabajoExtraTareaId) debe estar lleno
 * - El estado se actualiza automáticamente en las entidades relacionadas
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pagos_trabajos_extra_obra", indexes = {
    @Index(name = "idx_pago_trabajo_extra", columnList = "trabajo_extra_id"),
    @Index(name = "idx_pago_trabajo_obra", columnList = "obra_id"),
    @Index(name = "idx_pago_trabajo_empresa", columnList = "empresa_id"),
    @Index(name = "idx_pago_trabajo_fecha", columnList = "fecha_pago"),
    @Index(name = "idx_pago_trabajo_estado", columnList = "estado"),
    @Index(name = "idx_pago_trabajo_tipo", columnList = "tipo_pago"),
    @Index(name = "idx_pago_trabajo_profesional", columnList = "trabajo_extra_profesional_id"),
    @Index(name = "idx_pago_trabajo_tarea", columnList = "trabajo_extra_tarea_id"),
    @Index(name = "idx_pago_trabajo_presupuesto", columnList = "presupuesto_no_cliente_id")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class PagoTrabajoExtraObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== RELACIONES PRINCIPALES ==========
    
    @NotNull(message = "El trabajo extra es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_extra_id", nullable = false)
    private TrabajoExtra trabajoExtra;
    
    @NotNull(message = "La obra es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    private Obra obra;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id")
    private PresupuestoNoCliente presupuestoNoCliente;
    
    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // ========== REFERENCIAS ESPECÍFICAS (OPCIONAL - SOLO UNA DEBE ESTAR LLENA) ==========
    
    /**
     * Si el pago es específico a un profesional del trabajo extra
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_extra_profesional_id")
    private TrabajoExtroProfesional trabajoExtroProfesional;
    
    /**
     * Si el pago es específico a una tarea del trabajo extra
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_extra_tarea_id")
    private TrabajoExtraTarea trabajoExtraTarea;

    // ========== DATOS DEL PAGO ==========
    
    @NotNull(message = "El tipo de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", length = 50, nullable = false)
    private TipoPagoTrabajoExtra tipoPago;
    
    @NotNull(message = "El concepto es obligatorio")
    @Column(name = "concepto", length = 500, nullable = false)
    private String concepto;
    
    // ========== MONTOS ==========
    
    @NotNull(message = "El monto base es obligatorio")
    @Positive(message = "El monto base debe ser mayor a cero")
    @Column(name = "monto_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoBase;
    
    @Column(name = "descuentos", precision = 15, scale = 2)
    private BigDecimal descuentos = BigDecimal.ZERO;
    
    @Column(name = "bonificaciones", precision = 15, scale = 2)
    private BigDecimal bonificaciones = BigDecimal.ZERO;
    
    @NotNull(message = "El monto final es obligatorio")
    @Positive(message = "El monto final debe ser mayor a cero")
    @Column(name = "monto_final", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoFinal;

    // ========== FECHA Y ESTADO ==========
    
    @NotNull(message = "La fecha de pago es obligatoria")
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;
    
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;
    
    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 50, nullable = false)
    private EstadoPago estado = EstadoPago.PAGADO;

    // ========== COMPROBANTE ==========
    
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 50)
    private MetodoPago metodoPago;
    
    @Column(name = "numero_comprobante", length = 100)
    private String numeroComprobante;
    
    @Column(name = "comprobante_url", length = 500)
    private String comprobanteUrl;

    // ========== OBSERVACIONES ==========
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    // ========== AUDITORÍA ==========
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;
    
    @Column(name = "usuario_creacion_id")
    private Long usuarioCreacionId;
    
    @Column(name = "usuario_modificacion_id")
    private Long usuarioModificacionId;

    // ========== CONSTANTES ==========
    
    public static final String TABLA = "pagos_trabajos_extra_obra";

    // ========== MÉTODOS DE NEGOCIO ==========
    
    /**
     * Valida que el pago sea consistente según su tipo
     */
    public void validar() {
        // Si es pago a profesional, debe tener trabajoExtroProfesionalId
        if (tipoPago == TipoPagoTrabajoExtra.PAGO_PROFESIONAL && trabajoExtroProfesional == null) {
            throw new IllegalArgumentException(
                "Para pagos de tipo PAGO_PROFESIONAL, se debe especificar el profesional");
        }
        
        // Si es pago a tarea, debe tener trabajoExtraTareaId
        if (tipoPago == TipoPagoTrabajoExtra.PAGO_TAREA && trabajoExtraTarea == null) {
            throw new IllegalArgumentException(
                "Para pagos de tipo PAGO_TAREA, se debe especificar la tarea");
        }
        
        // Si es pago general, NO debe tener profesional ni tarea específica
        if (tipoPago == TipoPagoTrabajoExtra.PAGO_GENERAL && 
            (trabajoExtroProfesional != null || trabajoExtraTarea != null)) {
            throw new IllegalArgumentException(
                "Para pagos de tipo PAGO_GENERAL, no se debe especificar profesional ni tarea");
        }
        
        // No puede tener ambos (profesional Y tarea)
        if (trabajoExtroProfesional != null && trabajoExtraTarea != null) {
            throw new IllegalArgumentException(
                "Un pago no puede ser específico a profesional Y tarea al mismo tiempo");
        }
    }

    /**
     * Calcula el monto final basándose en el monto base, descuentos y bonificaciones
     */
    public void calcularMontoFinal() {
        BigDecimal descuentosActuales = descuentos != null ? descuentos : BigDecimal.ZERO;
        BigDecimal bonificacionesActuales = bonificaciones != null ? bonificaciones : BigDecimal.ZERO;
        
        this.montoFinal = montoBase
            .subtract(descuentosActuales)
            .add(bonificacionesActuales);
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoPago.PAGADO;
        }
        if (descuentos == null) {
            descuentos = BigDecimal.ZERO;
        }
        if (bonificaciones == null) {
            bonificaciones = BigDecimal.ZERO;
        }
        validar();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
        validar();
    }
}
