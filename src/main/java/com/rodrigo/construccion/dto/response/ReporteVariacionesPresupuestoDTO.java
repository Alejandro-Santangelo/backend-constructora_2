package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el reporte de variaciones presupuestarias
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteVariacionesPresupuestoDTO {

    private Long obraId;
    private String obraNombre;
    private BigDecimal presupuestoEstimado;
    private BigDecimal costosReales;
    private BigDecimal variacion;
    private BigDecimal porcentajeVariacion;
}

