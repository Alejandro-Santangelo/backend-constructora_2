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
 * Entidad para profesionales calculadora de trabajos extra.
 * Representa los profesionales desglosados dentro de un item.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trabajos_extra_profesional_calculadora", indexes = {
    @Index(name = "idx_te_profesional_item_id", columnList = "item_calculadora_id"),
    @Index(name = "idx_te_profesional_empresa_id", columnList = "empresa_id"),
    @Index(name = "idx_te_profesional_obra_id", columnList = "profesional_obra_id")
})
public class TrabajoExtraProfesionalCalculadora {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id", nullable = false)
    @JsonBackReference
    private TrabajoExtraItemCalculadora itemCalculadora;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "profesional_obra_id")
    private Long profesionalObraId;

    @Column(name = "rol", nullable = false, length = 255)
    private String rol;

    @Column(name = "nombre_completo", length = 255)
    private String nombreCompleto;

    @Column(name = "cantidad_jornales", precision = 10, scale = 2)
    private BigDecimal cantidadJornales;

    @Column(name = "valor_jornal", precision = 15, scale = 2)
    private BigDecimal valorJornal;

    @Column(name = "subtotal", precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "incluir_en_calculo_dias")
    private Boolean incluirEnCalculoDias;

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
