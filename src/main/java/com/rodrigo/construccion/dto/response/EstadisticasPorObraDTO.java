package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para las estadísticas por obra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasPorObraDTO {

    private Long obraId;
    private String obraNombre;
    private Long cantidadCostos;
    private BigDecimal montoTotal;
}

