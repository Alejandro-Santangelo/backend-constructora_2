package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para totales consolidados de pagos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TotalesPagosConsolidadosDTO {

    private BigDecimal totalMateriales;
    private BigDecimal totalGastosGenerales;
    private BigDecimal totalOtros;
    private BigDecimal totalGeneral;
    private Integer cantidadPagos;
}
