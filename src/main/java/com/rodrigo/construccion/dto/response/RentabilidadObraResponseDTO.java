package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el cálculo de rentabilidad de una obra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentabilidadObraResponseDTO {

    private Long obraId;
    private BigDecimal totalIngresos;
    private BigDecimal totalCostos;
    private BigDecimal utilidad;
    private BigDecimal margenRentabilidad;
    private String estado;
}

