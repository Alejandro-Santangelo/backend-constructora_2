package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para las estadísticas generales de costos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasCostosResponseDTO {

    private Long totalCostos;
    private Long costosAprobados;
    private Long costosPendientes;
    private Long costosRechazados;
    private BigDecimal montoTotal;
    private BigDecimal montoAprobado;
    private BigDecimal montoPendiente;
}

