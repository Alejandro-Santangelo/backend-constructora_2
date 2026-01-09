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
import java.time.LocalDateTime;

/**
 * Entidad MovimientoMaterial
 *
 * Representa los movimientos de entrada y salida de materiales.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movimiento_material", indexes = {
        @Index(name = "idx_movimientos_stock", columnList = "id_stock_material"),
        @Index(name = "idx_movimientos_material", columnList = "id_material"),
        @Index(name = "idx_movimientos_fecha", columnList = "fecha_movimiento"),
        @Index(name = "idx_movimientos_empresa", columnList = "id_empresa")
})
@Filter(name = "empresaFilter", condition = "id_empresa = :empresaId")
public class MovimientoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento_material")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_stock_material", nullable = false)
    private StockMaterial stockMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference("material-movimientos")
    private Material material;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Column(name = "cantidad", nullable = false, precision = 15, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "valor_total", precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @Column(name = "numero_documento", length = 100)
    private String numeroDocumento;

    @NotNull(message = "La fecha de movimiento es obligatoria")
    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "estado", length = 50)
    private String estado = "CONFIRMADO";

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "id_obra")
    private Long obraId;

    @Column(name = "id_usuario")
    private Long usuarioId;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    // Getters de conveniencia
    public Long getStockMaterialId() {
        return stockMaterial != null ? stockMaterial.getId() : null;
    }

    public Long getMaterialId() {
        return material != null ? material.getId() : null;
    }

    public String getNombreMaterial() {
        return material != null ? material.getNombre() : null;
    }

    public Long getEmpresaId() {
        return empresa != null ? empresa.getId() : null;
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "CONFIRMADO";
        }
        if (cantidad != null && precioUnitario != null) {
            valorTotal = cantidad.multiply(precioUnitario);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (cantidad != null && precioUnitario != null) {
            valorTotal = cantidad.multiply(precioUnitario);
        }
    }

    // Tipos de movimiento
    public static final String TIPO_ENTRADA = "ENTRADA";
    public static final String TIPO_SALIDA = "SALIDA";
    public static final String TIPO_TRANSFERENCIA = "TRANSFERENCIA";
    public static final String TIPO_AJUSTE = "AJUSTE";

    // Estados
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_CONFIRMADO = "CONFIRMADO";
    public static final String ESTADO_CANCELADO = "CANCELADO";

    // Métodos de utilidad
    public boolean esEntrada() {
        return TIPO_ENTRADA.equals(this.tipoMovimiento);
    }

    public boolean esSalida() {
        return TIPO_SALIDA.equals(this.tipoMovimiento);
    }

    public boolean esTransferencia() {
        return TIPO_TRANSFERENCIA.equals(this.tipoMovimiento);
    }

    public boolean esAjuste() {
        return TIPO_AJUSTE.equals(this.tipoMovimiento);
    }
}
