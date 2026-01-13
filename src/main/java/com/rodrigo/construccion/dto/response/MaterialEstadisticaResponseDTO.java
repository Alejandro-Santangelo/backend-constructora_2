package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaterialEstadisticaResponseDTO {
    private long totalMateriales;
    private BigDecimal precioPromedio;
}
