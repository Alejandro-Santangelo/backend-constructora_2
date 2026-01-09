package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO con resumen de retiros
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenRetirosDTO {

    private Long cantidad;
    private BigDecimal monto;
    private Long activos;
    private Long anulados;
}
