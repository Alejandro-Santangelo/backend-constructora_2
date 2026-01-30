package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Filter;

/**
 * Entidad para materiales desglosados dentro de un item de calculadora.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "material_calculadora")
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class MaterialCalculadora {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id", nullable = false)
    @JsonBackReference
    private ItemCalculadoraPresupuesto itemCalculadora;

    @Column(name = "frontend_id")
    private Long frontendId;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "es_global")
    private Boolean esGlobal = false;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "unidad", nullable = false, length = 100)
    private String unidad;

    @Column(name = "cantidad", precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio", precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonBackReference
    private Empresa empresa;

    // Vinculación con obra_material cuando el presupuesto está APROBADO
    @Column(name = "obra_material_id")
    private Long obraMaterialId;

    // Relación con pagos consolidados
    @OneToMany(mappedBy = "materialCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PagoConsolidado> pagos = new ArrayList<>();
}