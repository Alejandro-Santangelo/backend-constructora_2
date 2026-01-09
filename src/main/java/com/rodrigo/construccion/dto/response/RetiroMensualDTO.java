package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para retiros agrupados por mes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetiroMensualDTO {

    private String mes;   // Formato: "2025-12"
    private Integer anio;
    private BigDecimal total;
    private Long cantidad;
}
