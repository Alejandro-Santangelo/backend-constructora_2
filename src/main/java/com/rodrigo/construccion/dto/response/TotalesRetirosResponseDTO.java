package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO con totales de retiros personales
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TotalesRetirosResponseDTO {

    private Long empresaId;
    private BigDecimal totalRetiros;
    private Long cantidadRetiros;
    private Map<String, BigDecimal> retirosPorTipo;
    private List<RetiroMensualDTO> retirosPorMes;
}
