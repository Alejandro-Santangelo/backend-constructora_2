package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que almacena el cálculo inicial de costos por metros cuadrados
 * para un presupuesto. Permite estimar costos antes de agregar items detallados.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "presupuesto_costo_inicial")
public class PresupuestoCostoInicial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datos de entrada
    @Column(name = "metros_cuadrados", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.01", message = "Los metros cuadrados deben ser mayor a 0")
    private BigDecimal metrosCuadrados;

    @Column(name = "importe_por_metro", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.01", message = "El importe por metro debe ser mayor a 0")
    private BigDecimal importePorMetro;

    @Column(name = "total_estimado", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "El total estimado debe ser mayor o igual a 0")
    private BigDecimal totalEstimado;

    // Porcentajes de distribución
    @Column(name = "porcentaje_profesionales", nullable = false, precision = 5, scale = 2)
    @Min(value = 0, message = "El porcentaje de profesionales debe estar entre 0 y 100")
    @Max(value = 100, message = "El porcentaje de profesionales debe estar entre 0 y 100")
    private BigDecimal porcentajeProfesionales;

    @Column(name = "porcentaje_materiales", nullable = false, precision = 5, scale = 2)
    @Min(value = 0, message = "El porcentaje de materiales debe estar entre 0 y 100")
    @Max(value = 100, message = "El porcentaje de materiales debe estar entre 0 y 100")
    private BigDecimal porcentajeMateriales;

    @Column(name = "porcentaje_otros_costos", nullable = false, precision = 5, scale = 2)
    @Min(value = 0, message = "El porcentaje de otros costos debe estar entre 0 y 100")
    @Max(value = 100, message = "El porcentaje de otros costos debe estar entre 0 y 100")
    private BigDecimal porcentajeOtrosCostos;

    // Montos calculados
    @Column(name = "monto_profesionales", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "El monto de profesionales debe ser mayor o igual a 0")
    private BigDecimal montoProfesionales;

    @Column(name = "monto_materiales", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "El monto de materiales debe ser mayor o igual a 0")
    private BigDecimal montoMateriales;

    @Column(name = "monto_otros_costos", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "El monto de otros costos debe ser mayor o igual a 0")
    private BigDecimal montoOtrosCostos;

    // Timestamp
    @Column(name = "fecha_guardado", nullable = false)
    private LocalDateTime fechaGuardado;

    // Relación ONE-TO-ONE con PresupuestoNoCliente
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id", nullable = false, unique = true)
    @JsonBackReference
    private PresupuestoNoCliente presupuestoNoCliente;

    /**
     * Calcula todos los montos derivados basándose en los valores de entrada.
     * Este método debe ser llamado antes de persistir la entidad.
     */
    public void calcularMontos() {
        // Calcular total estimado
        if (metrosCuadrados != null && importePorMetro != null) {
            this.totalEstimado = metrosCuadrados.multiply(importePorMetro);
        }

        // Calcular montos por categoría
        if (totalEstimado != null) {
            if (porcentajeProfesionales != null) {
                this.montoProfesionales = totalEstimado
                    .multiply(porcentajeProfesionales)
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            }

            if (porcentajeMateriales != null) {
                this.montoMateriales = totalEstimado
                    .multiply(porcentajeMateriales)
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            }

            if (porcentajeOtrosCostos != null) {
                this.montoOtrosCostos = totalEstimado
                    .multiply(porcentajeOtrosCostos)
                    .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            }
        }

        // Establecer timestamp
        if (fechaGuardado == null) {
            this.fechaGuardado = LocalDateTime.now();
        }
    }

    /**
     * Valida que la suma de porcentajes no exceda 100.
     * @return true si la suma es válida (<=100), false en caso contrario
     */
    public boolean validarSumaPorcentajes() {
        BigDecimal suma = BigDecimal.ZERO;
        if (porcentajeProfesionales != null) suma = suma.add(porcentajeProfesionales);
        if (porcentajeMateriales != null) suma = suma.add(porcentajeMateriales);
        if (porcentajeOtrosCostos != null) suma = suma.add(porcentajeOtrosCostos);
        
        return suma.compareTo(new BigDecimal("100")) <= 0;
    }
}
