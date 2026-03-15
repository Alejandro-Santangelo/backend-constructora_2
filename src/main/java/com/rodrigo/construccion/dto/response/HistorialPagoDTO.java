package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO simple para representar un pago individual en el historial
 * Usado para mostrar fechas y montos de pagos anteriores
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialPagoDTO {
    
    private Long pagoId;
    private LocalDate fechaPago;
    private BigDecimal monto;
    private String tipoPago;
    private String observaciones;
}
