package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de response para detalle de un día específico
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleDiaDTO {

    private String fecha; // "YYYY-MM-DD"
    private Long profesionalId;
    private String profesionalNombre;
    private String profesionalTipo;
    private BigDecimal importeJornal;
    private Integer cantidad;
}
