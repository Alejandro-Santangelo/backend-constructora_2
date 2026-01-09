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
 * Entidad para jornales calculadora de trabajos extra.
 * Representa los jornales desglosados por rol dentro de un item.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trabajos_extra_jornal_calculadora", indexes = {
    @Index(name = "idx_te_jornal_item_id", columnList = "item_calculadora_id"),
    @Index(name = "idx_te_jornal_empresa_id", columnList = "empresa_id"),
    @Index(name = "idx_te_jornal_profesional_id", columnList = "profesional_obra_id")
})
public class TrabajoExtraJornalCalculadora {
    
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

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "incluir_en_calculo_dias")
    private Boolean incluirEnCalculoDias;

    @Column(name = "frontend_id")
    private Long frontendId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
