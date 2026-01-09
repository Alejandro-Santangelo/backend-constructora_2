package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que contiene un resumen de los jornales en un período de tiempo específico.")
public class ResumenPeriodoJornalDTO {

    @Schema(description = "Rango de fechas del resumen.", example = "2024-01-01 - 2024-01-31")
    private String periodo;

    @Schema(description = "Cantidad total de jornales registrados en el período.", example = "85")
    private long cantidadJornales;

    @Schema(description = "Monto total facturado en el período (horas * valor hora).", example = "170000.00")
    private BigDecimal montoTotal;

    @Schema(description = "Total de horas trabajadas en el período.", example = "680.00")
    private BigDecimal horasTotal;

    @Schema(description = "Valor promedio por hora en el período.", example = "250.00")
    private BigDecimal valorPromedioHora;
}