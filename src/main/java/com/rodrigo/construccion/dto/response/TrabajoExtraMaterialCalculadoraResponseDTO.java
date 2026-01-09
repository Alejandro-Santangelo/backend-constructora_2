package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO Response para material calculadora de trabajo extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraMaterialCalculadoraResponseDTO {
    
    private Long id;
    private Long itemCalculadoraId;
    private Long empresaId;
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
