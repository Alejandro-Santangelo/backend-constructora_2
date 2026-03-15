package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de resumen de jornales por profesional/obra
 * 
 * Usado para reportes agrupados que muestran totales de horas y montos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalJornalResumenDTO {

    /**
     * ID del profesional
     */
    private Long profesionalId;

    /**
     * Nombre del profesional
     */
    private String profesionalNombre;

    /**
     * ID de la obra
     */
    private Long obraId;

    /**
     * Nombre de la obra
     */
    private String obraNombre;

    /**
     * Cantidad de jornales registrados
     */
    private BigDecimal cantidadJornales;

    /**
     * Total de horas trabajadas (suma de horasTrabajadasDecimal)
     * Ejemplo: 5.25 = 5 días y 1 cuarto
     */
    private BigDecimal totalHorasDecimal;

    /**
     * Total cobrado (suma de montoCobrado)
     */
    private BigDecimal totalCobrado;

    /**
     * Promedio de horas por jornal
     */
    public BigDecimal getPromedioHorasPorJornal() {
        if (cantidadJornales != null && cantidadJornales.compareTo(BigDecimal.ZERO) > 0 && totalHorasDecimal != null) {
            return totalHorasDecimal.divide(
                cantidadJornales, 
                2, 
                java.math.RoundingMode.HALF_UP
            );
        }
        return BigDecimal.ZERO;
    }

    /**
     * Promedio de monto por jornal
     */
    public BigDecimal getPromedioMontoPorJornal() {
        if (cantidadJornales != null && cantidadJornales.compareTo(BigDecimal.ZERO) > 0 && totalCobrado != null) {
            return totalCobrado.divide(
                cantidadJornales, 
                2, 
                java.math.RoundingMode.HALF_UP
            );
        }
        return BigDecimal.ZERO;
    }
}
