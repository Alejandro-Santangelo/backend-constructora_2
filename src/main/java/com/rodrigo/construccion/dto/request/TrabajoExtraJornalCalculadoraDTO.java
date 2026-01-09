package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear/actualizar jornal calculadora en trabajos extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoExtraJornalCalculadoraDTO {
    
    private Long id;
    private Long profesionalObraId;
    private String rol;
    private BigDecimal cantidad;
    private BigDecimal valorUnitario;
    private BigDecimal subtotal;
    private Boolean incluirEnCalculoDias;
    private Long frontendId;
    private String observaciones;
}
