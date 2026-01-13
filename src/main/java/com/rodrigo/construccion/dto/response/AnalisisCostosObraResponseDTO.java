package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para el análisis de costos de una obra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalisisCostosObraResponseDTO {

    private Long obraId;
    private BigDecimal totalCostos;
    private BigDecimal costosAprobados;
    private BigDecimal costosPendientes;
    private List<DistribucionCategoriaDTO> distribucionCategorias;

    /**
     * DTO interno para la distribución por categoría
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistribucionCategoriaDTO {
        private String categoria;
        private BigDecimal monto;
        private Long cantidad;
    }
}

