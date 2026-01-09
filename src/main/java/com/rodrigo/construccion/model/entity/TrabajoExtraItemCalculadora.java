package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad para items de la calculadora de trabajos extra.
 * Réplica fiel de ItemCalculadoraPresupuesto adaptada a TrabajoExtra.
 * Soporta dos modos de ingreso:
 * - Automático: cantidad_jornales × importe_jornal + materiales
 * - Manual: total ingresado directamente
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trabajos_extra_items_calculadora", indexes = {
    @Index(name = "idx_te_items_trabajo_extra_id", columnList = "trabajo_extra_id"),
    @Index(name = "idx_te_items_empresa_id", columnList = "empresa_id"),
    @Index(name = "idx_te_items_tipo_profesional", columnList = "tipo_profesional"),
    @Index(name = "idx_te_items_es_gasto_general", columnList = "es_gasto_general")
})
public class TrabajoExtraItemCalculadora {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_extra_id", nullable = false)
    @JsonBackReference
    private TrabajoExtra trabajoExtra;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "tipo_profesional", nullable = false, length = 255)
    private String tipoProfesional;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "cantidad_jornales", precision = 10, scale = 2)
    private BigDecimal cantidadJornales;

    @Column(name = "importe_jornal", precision = 15, scale = 2)
    private BigDecimal importeJornal;

    @Column(name = "subtotal_mano_obra", precision = 15, scale = 2)
    private BigDecimal subtotalManoObra;

    @Column(name = "materiales", precision = 15, scale = 2)
    private BigDecimal materiales;

    @Column(name = "total_manual", precision = 15, scale = 2)
    private BigDecimal totalManual;
    
    @Column(name = "descripcion_total_manual", length = 500)
    private String descripcionTotalManual;
    
    @Column(name = "observaciones_total_manual", columnDefinition = "TEXT")
    private String observacionesTotalManual;

    @Column(name = "total", precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "es_modo_manual", nullable = false)
    private Boolean esModoManual = false;

    @Column(name = "incluir_en_calculo_dias")
    private Boolean incluirEnCalculoDias;

    @Column(name = "trabaja_en_paralelo", nullable = false)
    private Boolean trabajaEnParalelo = true;

    @Column(name = "es_rubro_vacio")
    private Boolean esRubroVacio = false;

    @Column(name = "subtotal_materiales", precision = 15, scale = 2)
    private BigDecimal subtotalMateriales;

    // Relaciones con profesionales, materiales y jornales desglosados
    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TrabajoExtraProfesionalCalculadora> profesionales = new ArrayList<>();

    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference  
    private List<TrabajoExtraMaterialCalculadora> materialesLista = new ArrayList<>();

    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TrabajoExtraJornalCalculadora> jornales = new ArrayList<>();

    // ============================================================================
    // CAMPOS PARA GASTOS GENERALES (RELACIONAL - NO JSON)
    // ============================================================================

    @Column(name = "es_gasto_general", nullable = false)
    private Boolean esGastoGeneral = false;

    @Column(name = "subtotal_gastos_generales", precision = 15, scale = 2)
    private BigDecimal subtotalGastosGenerales;

    @Column(name = "descripcion_gastos_generales", length = 500)
    private String descripcionGastosGenerales;

    @Column(name = "observaciones_gastos_generales", columnDefinition = "TEXT")
    private String observacionesGastosGenerales;

    // ============================================================================
    // CAMPOS DE DESCRIPCIÓN/OBSERVACIONES POR CATEGORÍA
    // ============================================================================

    @Column(name = "descripcion_profesionales", length = 500)
    private String descripcionProfesionales;

    @Column(name = "observaciones_profesionales", columnDefinition = "TEXT")
    private String observacionesProfesionales;

    @Column(name = "descripcion_materiales", length = 500)
    private String descripcionMateriales;

    @Column(name = "observaciones_materiales", columnDefinition = "TEXT")
    private String observacionesMateriales;

    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TrabajoExtraGastoGeneral> gastosGenerales = new ArrayList<>();

    // ============================================================================
    // AUDITORÍA
    // ============================================================================

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

    // ============================================================================
    // MÉTODOS DE UTILIDAD
    // ============================================================================

    public void addProfesional(TrabajoExtraProfesionalCalculadora profesional) {
        profesionales.add(profesional);
        profesional.setItemCalculadora(this);
    }

    public void removeProfesional(TrabajoExtraProfesionalCalculadora profesional) {
        profesionales.remove(profesional);
        profesional.setItemCalculadora(null);
    }

    public void addMaterial(TrabajoExtraMaterialCalculadora material) {
        materialesLista.add(material);
        material.setItemCalculadora(this);
    }

    public void removeMaterial(TrabajoExtraMaterialCalculadora material) {
        materialesLista.remove(material);
        material.setItemCalculadora(null);
    }

    public void addJornal(TrabajoExtraJornalCalculadora jornal) {
        jornales.add(jornal);
        jornal.setItemCalculadora(this);
    }

    public void removeJornal(TrabajoExtraJornalCalculadora jornal) {
        jornales.remove(jornal);
        jornal.setItemCalculadora(null);
    }

    public void addGastoGeneral(TrabajoExtraGastoGeneral gasto) {
        gastosGenerales.add(gasto);
        gasto.setItemCalculadora(this);
    }

    public void removeGastoGeneral(TrabajoExtraGastoGeneral gasto) {
        gastosGenerales.remove(gasto);
        gasto.setItemCalculadora(null);
    }
}
