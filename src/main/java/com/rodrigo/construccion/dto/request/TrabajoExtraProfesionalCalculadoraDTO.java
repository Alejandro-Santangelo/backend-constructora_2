package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear/actualizar profesional calculadora en trabajos extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoExtraProfesionalCalculadoraDTO {
    
    private Long id;
    private Long profesionalObraId;
    private String rol;
    private String nombreCompleto;
    private BigDecimal cantidadJornales;
    private BigDecimal valorJornal;
    private BigDecimal subtotal;
    private Boolean incluirEnCalculoDias;
    private Long frontendId;
    private String observaciones;
}
