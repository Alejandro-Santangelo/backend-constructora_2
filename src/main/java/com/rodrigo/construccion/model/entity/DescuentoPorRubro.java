package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "descuentos_por_rubro", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"presupuesto_no_cliente_id", "nombre_rubro"}))
public class DescuentoPorRubro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id", nullable = false)
    @JsonBackReference("descuentosPorRubro")
    private PresupuestoNoCliente presupuestoNoCliente;

    @Column(name = "nombre_rubro", length = 100, nullable = false)
    private String nombreRubro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubro_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Rubro rubro;

    @Column(name = "activo", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean activo = true;

    @Column(name = "tipo", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'porcentaje'")
    private String tipo = "porcentaje";

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    // Profesionales
    @Column(name = "profesionales_activo", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean profesionalesActivo = true;

    @Column(name = "profesionales_tipo", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'porcentaje'")
    private String profesionalesTipo = "porcentaje";

    @Column(name = "profesionales_valor", precision = 10, scale = 2)
    private BigDecimal profesionalesValor;

    // Materiales
    @Column(name = "materiales_activo", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean materialesActivo = true;

    @Column(name = "materiales_tipo", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'porcentaje'")
    private String materialesTipo = "porcentaje";

    @Column(name = "materiales_valor", precision = 10, scale = 2)
    private BigDecimal materialesValor;

    // Otros Costos
    @Column(name = "otros_costos_activo", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean otrosCostosActivo = true;

    @Column(name = "otros_costos_tipo", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'porcentaje'")
    private String otrosCostosTipo = "porcentaje";

    @Column(name = "otros_costos_valor", precision = 10, scale = 2)
    private BigDecimal otrosCostosValor;

    // Honorarios
    @Column(name = "honorarios_activo", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean honorariosActivo = false;

    @Column(name = "honorarios_tipo", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PORCENTAJE'")
    private String honorariosTipo = "PORCENTAJE";

    @Column(name = "honorarios_valor", precision = 10, scale = 2)
    private BigDecimal honorariosValor;

    // Mayores Costos
    @Column(name = "mayores_costos_activo", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean mayoresCostosActivo = false;

    @Column(name = "mayores_costos_tipo", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PORCENTAJE'")
    private String mayoresCostosTipo = "PORCENTAJE";

    @Column(name = "mayores_costos_valor", precision = 10, scale = 2)
    private BigDecimal mayoresCostosValor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
