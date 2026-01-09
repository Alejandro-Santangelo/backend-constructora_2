package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear/actualizar gasto general en trabajos extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoExtraGastoGeneralDTO {
    
    private Long id;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private Boolean sinCantidad;
    private Boolean sinPrecio;
    private Integer orden;
    private Long frontendId;
    private String observaciones;
}
