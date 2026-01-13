package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para la comparación de presupuesto vs costos reales
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresupuestoVsRealResponseDTO {

    private Long obraId;
    private BigDecimal presupuestoEstimado;
    private BigDecimal costosReales;
    private BigDecimal variacion;
    private BigDecimal porcentajeVariacion;
    private String estado;
}

