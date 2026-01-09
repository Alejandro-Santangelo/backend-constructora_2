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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad PedidoPago
 *
 * Representa los pedidos de pago realizados para proveedores.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pedido_pago", indexes = {
    @Index(name = "idx_pedidos_proveedor", columnList = "id_proveedor"),
    @Index(name = "idx_pedidos_obra", columnList = "id_obra"),
    @Index(name = "idx_pedidos_estado", columnList = "estado"),
    @Index(name = "idx_pedidos_fecha", columnList = "fecha_pedido"),
    @Index(name = "idx_pedidos_empresa", columnList = "id_empresa")
})
@Filter(name = "empresaFilter", condition = "id_empresa = :empresaId")
public class PedidoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido_pago")
    private Long id;

    @Column(name = "numero_pedido", length = 100)
    private String numeroPedido;

    @NotNull(message = "La fecha del pedido es obligatoria")
    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser mayor a cero")
    @Column(name = "importe", nullable = false)
    private Double importe;

    @Column(name = "concepto", length = 500)
    private String concepto;

    @Column(name = "estado", length = 50)
    private String estado = "BORRADOR";

    @Column(name = "tipo_pago", length = 50)
    private String tipoPago;

    @Column(name = "numero_factura", length = 100)
    private String numeroFactura;

    @Column(name = "numero_comprobante", length = 100)
    private String numeroComprobante;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "usuario_aprobador_id")
    private Long usuarioAprobadorId;

    @Column(name = "usuario_autorizador_id")
    private Long usuarioAutorizadorId;

    @Column(name = "usuario_pagador_id")
    private Long usuarioPagadorId;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_obra")
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    // Getters de conveniencia
    public Long getProveedorId() {
        return proveedor != null ? proveedor.getId() : null;
    }

    public String getNombreProveedor() {
        return proveedor != null ? proveedor.getNombre() : null;
    }

    public Long getObraId() {
        return obra != null ? obra.getId() : null;
    }

    public String getNombreObra() {
        return obra != null ? obra.getNombre() : null;
    }

    public Long getEmpresaId() {
        return empresa != null ? empresa.getId() : null;
    }
    
    // Estados del pedido
    public static final String ESTADO_BORRADOR = "BORRADOR";
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_APROBADO = "APROBADO";
    public static final String ESTADO_AUTORIZADO = "AUTORIZADO";
    public static final String ESTADO_PAGADO = "PAGADO";
    public static final String ESTADO_RECHAZADO = "RECHAZADO";
    public static final String ESTADO_CANCELADO = "CANCELADO";

    // Tipos de pago
    public static final String TIPO_TRANSFERENCIA = "TRANSFERENCIA";
    public static final String TIPO_CHEQUE = "CHEQUE";
    public static final String TIPO_EFECTIVO = "EFECTIVO";
    public static final String TIPO_TARJETA = "TARJETA";

    // Métodos de utilidad
    public boolean esBorrador() {
        return ESTADO_BORRADOR.equals(this.estado);
    }

    public boolean estaPendiente() {
        return ESTADO_PENDIENTE.equals(this.estado);
    }

    public boolean estaAprobado() {
        return ESTADO_APROBADO.equals(this.estado);
    }

    public boolean estaAutorizado() {
        return ESTADO_AUTORIZADO.equals(this.estado);
    }

    public boolean estaPagado() {
        return ESTADO_PAGADO.equals(this.estado);
    }

    public boolean estaRechazado() {
        return ESTADO_RECHAZADO.equals(this.estado);
    }

    public boolean estaCancelado() {
        return ESTADO_CANCELADO.equals(this.estado);
    }

    public boolean puedeSerModificado() {
        return ESTADO_BORRADOR.equals(this.estado) || ESTADO_PENDIENTE.equals(this.estado);
    }

    public boolean puedeSerAprobado() {
        return ESTADO_PENDIENTE.equals(this.estado);
    }

    public boolean puedeSerAutorizado() {
        return ESTADO_APROBADO.equals(this.estado);
    }

    public boolean puedeSerPagado() {
        return ESTADO_AUTORIZADO.equals(this.estado);
    }

    public boolean estaVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    // Métodos de estado
    public void aprobar(Long usuarioId) {
        this.estado = ESTADO_APROBADO;
        this.fechaAprobacion = LocalDateTime.now();
        this.usuarioAprobadorId = usuarioId;
        this.fechaModificacion = LocalDateTime.now();
    }

    public void autorizar(Long usuarioId) {
        this.estado = ESTADO_AUTORIZADO;
        this.fechaAutorizacion = LocalDateTime.now();
        this.usuarioAutorizadorId = usuarioId;
        this.fechaModificacion = LocalDateTime.now();
    }

    public void pagar(Long usuarioId) {
        this.estado = ESTADO_PAGADO;
        this.fechaPago = LocalDateTime.now();
        this.usuarioPagadorId = usuarioId;
        this.fechaModificacion = LocalDateTime.now();
    }

    public void rechazar(String motivo) {
        this.estado = ESTADO_RECHAZADO;
        this.motivoRechazo = motivo;
        this.fechaModificacion = LocalDateTime.now();
    }

    public void cancelar() {
        this.estado = ESTADO_CANCELADO;
        this.fechaModificacion = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (estado == null) {
            estado = ESTADO_BORRADOR;
        }
        if (fechaPedido == null) {
            fechaPedido = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
