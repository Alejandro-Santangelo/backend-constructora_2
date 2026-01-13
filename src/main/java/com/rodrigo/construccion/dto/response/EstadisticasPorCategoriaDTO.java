package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para las estadísticas por categoría
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasPorCategoriaDTO {

    private String categoria;
    private Long cantidadCostos;
    private BigDecimal montoTotal;
}

