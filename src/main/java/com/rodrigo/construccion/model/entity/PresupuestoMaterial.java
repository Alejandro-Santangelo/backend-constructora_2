package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Relación entre Presupuesto y Material (puede ser material existente o libre)
 */
@Entity
@Getter
@Setter
@Table(name = "presupuesto_material")
public class PresupuestoMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material", nullable = true)
    private Material material; // Si es null, es material libre

    @Column(name = "nombre_material")
    private String nombreMaterial; // Para materiales libres

    @Column(name = "cantidad")
    private Double cantidad;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "precio_unitario", precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "observaciones")
    private String observaciones;

    public BigDecimal getImporteTotal() {
        if (getPrecioUnitario() != null && getCantidad() != null) {
            return getPrecioUnitario().multiply(BigDecimal.valueOf(getCantidad()));
        }
        return BigDecimal.ZERO;
    }
}
