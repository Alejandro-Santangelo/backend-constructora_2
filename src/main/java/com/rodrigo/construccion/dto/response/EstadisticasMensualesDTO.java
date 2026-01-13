package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para las estadísticas mensuales de costos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasMensualesDTO {

    private Integer mes;
    private Integer anio;
    private Long cantidadCostos;
    private BigDecimal montoTotal;
}

