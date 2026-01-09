package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO con resumen de cobros
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCobrosDTO {

    private Long cantidad;
    private BigDecimal monto;
    private Long cobrados;
    private Long pendientes;
    private Long anulados;
}
