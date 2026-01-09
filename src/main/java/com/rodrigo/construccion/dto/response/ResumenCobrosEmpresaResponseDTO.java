package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para resumen de cobros empresa
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenCobrosEmpresaResponseDTO {

    private BigDecimal totalCobrado;
    private BigDecimal totalDisponible;
    private BigDecimal totalAsignado;
    private CantidadesPorEstado cobros;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CantidadesPorEstado {
        private Integer disponibles;
        private Integer asignadosParcial;
        private Integer asignadosTotal;
        private Integer anulados;
    }
}
