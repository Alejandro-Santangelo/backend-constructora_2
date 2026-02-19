package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para configuración de descuentos en presupuestos no cliente.
 * Los descuentos se aplican DESPUÉS de honorarios y mayores costos, 
 * restando importes del total consolidado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescuentosConfigDTO {
    /**
     * Explicación/justificación de por qué se aplican los descuentos.
     * Este texto aparecerá en el PDF para el cliente.
     */
    private String explicacion;
    
    /**
     * Configuración de descuento sobre jornales.
     * El descuento se aplica sobre el subtotal de jornales SIN honorarios.
     */
    private DescuentoCategoriaDTO jornales;
    
    /**
     * Configuración de descuento sobre materiales.
     * El descuento se aplica sobre el subtotal de materiales SIN honorarios.
     */
    private DescuentoCategoriaDTO materiales;
    
    /**
     * Configuración de descuento sobre honorarios.
     * El descuento se aplica sobre el total de honorarios ya calculados.
     */
    private DescuentoCategoriaDTO honorarios;
    
    /**
     * Configuración de descuento sobre mayores costos.
     * El descuento se aplica sobre el total de mayores costos ya calculados.
     */
    private DescuentoCategoriaDTO mayoresCostos;
}
