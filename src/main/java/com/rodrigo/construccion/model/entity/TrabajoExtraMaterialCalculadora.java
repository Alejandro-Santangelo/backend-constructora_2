package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad para materiales calculadora de trabajos extra.
 * Representa los materiales desglosados dentro de un item.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trabajos_extra_material_calculadora", indexes = {
    @Index(name = "idx_te_material_item_id", columnList = "item_calculadora_id"),
    @Index(name = "idx_te_material_empresa_id", columnList = "empresa_id"),
    @Index(name = "idx_te_material_obra_material_id", columnList = "obra_material_id")
})
public class TrabajoExtraMaterialCalculadora {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id", nullable = false)
    @JsonBackReference
    private TrabajoExtraItemCalculadora itemCalculadora;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "obra_material_id")
    private Long obraMaterialId;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "unidad", nullable = false, length = 100)
    private String unidad;

    @Column(name = "cantidad", precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio", precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "frontend_id")
    private Long frontendId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
