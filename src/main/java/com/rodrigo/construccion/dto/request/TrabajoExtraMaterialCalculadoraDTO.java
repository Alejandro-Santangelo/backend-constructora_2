package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear/actualizar material calculadora en trabajos extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoExtraMaterialCalculadoraDTO {
    
    private Long id;
    private Long obraMaterialId;
    private String nombre;
    private String descripcion;
    private String unidad;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal subtotal;
    private Long frontendId;
    private String observaciones;
}
