package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Filter;

/**
 * Entidad para profesionales desglosados dentro de un item de calculadora.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profesional_calculadora")
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class ProfesionalCalculadora {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id", nullable = false)
    @JsonBackReference
    private ItemCalculadoraPresupuesto itemCalculadora;

    @Column(name = "frontend_id")
    private Long frontendId;
    
    /**
     * ID de la relación profesional_obra cuando el presupuesto está APROBADO.
     * Se asigna al aprobar el presupuesto y crear la obra.
     * Permite vincular directamente con pagos_profesional_obra.
     */
    @Column(name = "profesional_obra_id")
    private Long profesionalObraId;

    @Column(name = "tipo", nullable = false, length = 255)
    private String tipo;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "es_global")
    private Boolean esGlobal = false;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "unidad", nullable = false, length = 100)
    private String unidad;

    @Column(name = "cantidad_jornales", precision = 10, scale = 2)
    private BigDecimal cantidadJornales;

    @Column(name = "importe_jornal", precision = 15, scale = 2)
    private BigDecimal importeJornal;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "sin_cantidad", nullable = false)
    private Boolean sinCantidad = false;

    @Column(name = "sin_importe", nullable = false)
    private Boolean sinImporte = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonBackReference
    private Empresa empresa;
}