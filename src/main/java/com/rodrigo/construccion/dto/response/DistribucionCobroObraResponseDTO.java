package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para la distribución consolidada de cobros empresa por obra
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistribucionCobroObraResponseDTO {

    private Long obraId;
    private String nombreObra;
    private Integer numeroPresupuesto;
    private BigDecimal totalCobradoAsignado;
    private BigDecimal montoProfesionales;
    private BigDecimal montoMateriales;
    private BigDecimal montoGastosGenerales;
    private BigDecimal montoTrabajosExtra;
    private BigDecimal totalDistribuido;
    private BigDecimal saldoSinDistribuir;
}
