package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO Response para consultar el saldo de caja chica
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información del saldo de caja chica")
public class SaldoCajaChicaResponse {

    @Schema(description = "Monto total asignado de caja chica", example = "50000.00")
    private BigDecimal montoAsignado;

    @Schema(description = "Saldo disponible actual", example = "35000.00")
    private BigDecimal saldoDisponible;

    @Schema(description = "Total gastado", example = "15000.00")
    private BigDecimal gastado;

    @Schema(description = "Cantidad de gastos registrados", example = "5")
    private Integer cantidadGastos;
}
