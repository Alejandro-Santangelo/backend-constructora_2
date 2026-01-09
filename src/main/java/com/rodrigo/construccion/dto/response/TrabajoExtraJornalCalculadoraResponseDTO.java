package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO Response para jornal calculadora de trabajo extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraJornalCalculadoraResponseDTO {
    
    private Long id;
    private Long itemCalculadoraId;
    private Long empresaId;
    private Long profesionalObraId;
    private String rol;
    private BigDecimal cantidad;
    private BigDecimal valorUnitario;
    private BigDecimal subtotal;
    private Boolean incluirEnCalculoDias;
    private Long frontendId;
    private String observaciones;
}
