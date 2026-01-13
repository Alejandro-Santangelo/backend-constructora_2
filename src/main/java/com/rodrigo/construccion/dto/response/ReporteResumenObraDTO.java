package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el reporte resumen de obras
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteResumenObraDTO {

    private Long obraId;
    private String obraNombre;
    private String cliente;
    private BigDecimal totalCostos;
    private BigDecimal costosAprobados;
    private BigDecimal porcentajeAprobado;
}

