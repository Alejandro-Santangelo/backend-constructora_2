package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para una categoría individual de descuento.
 * Representa la configuración de descuento para jornales, materiales, honorarios o mayores costos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoCategoriaDTO {
    /**
     * Indica si el descuento está activo para esta categoría.
     * Por defecto true si no está especificado.
     */
    private Boolean activo;
    
    /**
     * Tipo de descuento: "porcentaje" o "fijo"
     */
    private String tipo;
    
    /**
     * Valor del descuento.
     * Si tipo es "porcentaje", representa el % (0-100).
     * Si tipo es "fijo", representa el importe a descontar.
     */
    private BigDecimal valor;
}
