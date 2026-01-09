package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO con resumen de asignaciones
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenAsignacionesDTO {

    private Long cantidad;
    private BigDecimal monto;
    private Long activas;
    private Long anuladas;
}
