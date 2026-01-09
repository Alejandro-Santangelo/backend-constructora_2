package com.rodrigo.construccion.model.entity;

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
 * Entidad CobroObra
 * 
 * Representa los cobros efectuados al cliente por la ejecución de la obra.
 * Permite tracking de cobros parciales, pendientes y vencidos.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cobros_obra", indexes = {
    @Index(name = "idx_cobros_obra", columnList = "obra_id"),
    @Index(name = "idx_cobros_presupuesto", columnList = "presupuesto_no_cliente_id"),
    @Index(name = "idx_cobros_empresa", columnList = "empresa_id"),
    @Index(name = "idx_cobros_estado", columnList = "estado"),
    @Index(name = "idx_cobros_fecha", columnList = "fecha_cobro"),
    @Index(name = "idx_cobros_vencimiento", columnList = "fecha_vencimiento")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class CobroObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relaciones - obra_id es NULLABLE para permitir cobros en presupuestos APROBADOS sin obra aún
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = true)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private PresupuestoNoCliente presupuestoNoCliente;

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // ========== CAMPOS DE DIRECCIÓN ==========
    // Campos para búsqueda directa sin depender de Obra
    @Column(name = "calle", length = 255)
    private String calle;

    @Column(name = "altura", length = 50)
    private String altura;

    @Column(name = "barrio", length = 255)
    private String barrio;

    @Column(name = "torre", length = 50)
    private String torre;

    @Column(name = "piso", length = 50)
    private String piso;

    @Column(name = "depto", length = 50)
    private String depto;

    // ========== DATOS DEL COBRO ==========
    @Column(name = "tipo_cobro", length = 50)
    private String tipoCobro;

    @Column(name = "monto_cobrar", precision = 15, scale = 2)
    private BigDecimal montoCobrar;

    @Column(name = "monto_cobrado", precision = 15, scale = 2)
    private BigDecimal montoCobrado = BigDecimal.ZERO;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de cobro es obligatoria")
    @Column(name = "fecha_cobro", nullable = false)
    private LocalDate fechaCobro;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "concepto", length = 500)
    private String concepto;

    // Información del comprobante
    @Column(name = "numero_recibo", length = 100)
    private String numeroRecibo;

    @Column(name = "numero_factura", length = 100)
    private String numeroFactura;

    @Column(name = "tipo_comprobante", length = 50)
    private String tipoComprobante;

    // Método de pago
    @NotNull(message = "El método de pago es obligatorio")
    @Column(name = "metodo_pago", length = 50, nullable = false)
    private String metodoPago;

    // Estado del cobro
    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado", length = 50, nullable = false)
    private String estado = ESTADO_PENDIENTE;

    // Campos adicionales
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "comprobante_url", length = 500)
    private String comprobanteUrl;

    // ========== DISTRIBUCIÓN POR ÍTEMS ==========
    @Column(name = "modo_distribucion", length = 20)
    private String modoDistribucion = MODO_GENERAL;

    @Column(name = "monto_profesionales", precision = 15, scale = 2)
    private BigDecimal montoProfesionales;

    @Column(name = "monto_materiales", precision = 15, scale = 2)
    private BigDecimal montoMateriales;

    @Column(name = "monto_gastos_generales", precision = 15, scale = 2)
    private BigDecimal montoGastosGenerales;

    @Column(name = "monto_trabajos_extra", precision = 15, scale = 2)
    private BigDecimal montoTrabajosExtra;

    @Column(name = "porcentaje_profesionales", precision = 5, scale = 2)
    private BigDecimal porcentajeProfesionales;

    @Column(name = "porcentaje_materiales", precision = 5, scale = 2)
    private BigDecimal porcentajeMateriales;

    @Column(name = "porcentaje_gastos_generales", precision = 5, scale = 2)
    private BigDecimal porcentajeGastosGenerales;

    @Column(name = "porcentaje_trabajos_extra", precision = 5, scale = 2)
    private BigDecimal porcentajeTrabajosExtra;

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

    // ========== CONSTANTES DE ESTADO ==========
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_COBRADO = "COBRADO";
    public static final String ESTADO_VENCIDO = "VENCIDO";
    public static final String ESTADO_ANULADO = "ANULADO";

    // ========== CONSTANTES DE MÉTODO DE PAGO ==========
    public static final String METODO_TRANSFERENCIA = "TRANSFERENCIA";
    public static final String METODO_EFECTIVO = "EFECTIVO";
    public static final String METODO_CHEQUE = "CHEQUE";
    public static final String METODO_TARJETA = "TARJETA";

    // ========== CONSTANTES DE TIPO COMPROBANTE ==========
    public static final String TIPO_FACTURA_A = "FACTURA_A";
    public static final String TIPO_FACTURA_B = "FACTURA_B";
    public static final String TIPO_FACTURA_C = "FACTURA_C";
    public static final String TIPO_RECIBO = "RECIBO";
    public static final String TIPO_NOTA_CREDITO = "NOTA_CREDITO";

    // ========== CONSTANTES DE TIPO COBRO ==========
    public static final String TIPO_ANTICIPO = "ANTICIPO";
    public static final String TIPO_CERTIFICADO = "CERTIFICADO";
    public static final String TIPO_PAGO_FINAL = "PAGO_FINAL";
    public static final String TIPO_AJUSTE = "AJUSTE";

    // ========== CONSTANTES DE MODO DISTRIBUCIÓN ==========
    public static final String MODO_GENERAL = "GENERAL";
    public static final String MODO_POR_ITEMS = "POR_ITEMS";

    // ========== MÉTODOS DE UTILIDAD ==========
    
    public boolean esPendiente() {
        return ESTADO_PENDIENTE.equals(this.estado);
    }

    public boolean estaCobrado() {
        return ESTADO_COBRADO.equals(this.estado);
    }

    public boolean estaVencido() {
        return ESTADO_VENCIDO.equals(this.estado);
    }

    public boolean estaAnulado() {
        return ESTADO_ANULADO.equals(this.estado);
    }

    public boolean puedeSerModificado() {
        return ESTADO_PENDIENTE.equals(this.estado) || ESTADO_VENCIDO.equals(this.estado);
    }

    public boolean puedeSerCobrado() {
        return ESTADO_PENDIENTE.equals(this.estado) || ESTADO_VENCIDO.equals(this.estado);
    }

    public boolean tieneVencimiento() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now()) 
                && ESTADO_PENDIENTE.equals(this.estado);
    }

    // ========== MÉTODOS DE ESTADO ==========
    
    public void marcarComoCobrado() {
        this.estado = ESTADO_COBRADO;
        this.fechaModificacion = LocalDateTime.now();
    }

    public void marcarComoVencido() {
        this.estado = ESTADO_VENCIDO;
        this.fechaModificacion = LocalDateTime.now();
    }

    public void anular() {
        this.estado = ESTADO_ANULADO;
        this.fechaModificacion = LocalDateTime.now();
    }

    // ========== GETTERS DE CONVENIENCIA ==========
    
    public Long getObraId() {
        return obra != null ? obra.getId() : null;
    }

    public String getNombreObra() {
        return obra != null ? obra.getNombre() : null;
    }

    public String getDireccionObra() {
        return obra != null ? obra.getDireccionCompleta() : null;
    }

    public Long getPresupuestoId() {
        return presupuestoNoCliente != null ? presupuestoNoCliente.getId() : null;
    }

    /**
     * Calcula el saldo disponible para asignar
     * Este método debe ser llamado después de cargar las asignaciones activas
     * 
     * @param totalAsignado suma de todas las asignaciones activas de este cobro
     * @return monto disponible sin asignar
     */
    public BigDecimal calcularSaldoDisponible(BigDecimal totalAsignado) {
        if (totalAsignado == null) {
            return monto;
        }
        return monto.subtract(totalAsignado);
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
            estado = ESTADO_PENDIENTE;
        }
        if (fechaCobro == null) {
            fechaCobro = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
