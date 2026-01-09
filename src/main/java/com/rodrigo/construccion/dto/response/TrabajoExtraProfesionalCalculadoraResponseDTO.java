package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response para profesional calculadora de trabajo extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraProfesionalCalculadoraResponseDTO {
    
    private Long id;
    private Long itemCalculadoraId;
    private Long empresaId;
    private Long profesionalObraId;
    private String rol;
    private String nombreCompleto;
    private BigDecimal cantidadJornales;
    private BigDecimal valorJornal;
    private BigDecimal subtotal;
    private Boolean incluirEnCalculoDias;
    private Long frontendId;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
