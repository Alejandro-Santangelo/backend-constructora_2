package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta para el top de costos más altos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopCostosDTO {

    private Long costoId;
    private String concepto;
    private BigDecimal monto;
    private String obra;
    private String categoria;
    private LocalDate fecha;
}

