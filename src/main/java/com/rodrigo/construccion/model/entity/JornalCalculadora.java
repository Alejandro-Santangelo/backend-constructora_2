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
 * Entidad para jornales desglosados dentro de un item de calculadora.
 * Similar a ProfesionalCalculadora y MaterialCalculadora.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "jornal_calculadora")
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class JornalCalculadora {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id", nullable = false)
    @JsonBackReference
    private ItemCalculadoraPresupuesto itemCalculadora;

    @Column(name = "frontend_id")
    private Long frontendId;

    @Column(name = "rol", nullable = false, length = 255)
    private String rol;

    @Column(name = "es_global")
    private Boolean esGlobal = false;

    @Column(name = "cantidad", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidad;

    @Column(name = "valor_unitario", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * ID de la relación profesional_obra cuando el presupuesto está APROBADO.
     * Se asigna al aprobar el presupuesto y crear la obra.
     * Permite vincular directamente con pagos_profesional_obra.
     */
    @Column(name = "profesional_obra_id")
    private Long profesionalObraId;

    @Column(name = "incluir_en_calculo_dias")
    private Boolean incluirEnCalculoDias = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonBackReference
    private Empresa empresa;

    public Boolean getIncluirEnCalculoDias() {
        return incluirEnCalculoDias;
    }

    public void setIncluirEnCalculoDias(Boolean incluirEnCalculoDias) {
        this.incluirEnCalculoDias = incluirEnCalculoDias;
    }
}
