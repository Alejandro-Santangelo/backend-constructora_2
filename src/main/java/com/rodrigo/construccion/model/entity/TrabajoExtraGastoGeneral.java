package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para gastos generales de trabajos extra.
 * Representa los gastos generales desglosados dentro de un item.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trabajos_extra_gasto_general", indexes = {
    @Index(name = "idx_te_gasto_item_id", columnList = "item_calculadora_id"),
    @Index(name = "idx_te_gasto_empresa_id", columnList = "empresa_id")
})
public class TrabajoExtraGastoGeneral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id", nullable = false)
    @JsonBackReference
    private TrabajoExtraItemCalculadora itemCalculadora;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "sin_cantidad", nullable = false)
    private Boolean sinCantidad = false;

    @Column(name = "sin_precio", nullable = false)
    private Boolean sinPrecio = false;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "frontend_id")
    private Long frontendId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
