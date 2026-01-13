package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para el total de costos de una obra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalCostosObraResponseDTO {

    private Long obraId;
    private BigDecimal totalCostos;
    private Long cantidadCostos;
    private LocalDateTime fechaCalculo;
}

