package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "DTO con el resumen de honorarios para un período específico.")
public class ResumenPeriodoHonorarioDTO {

    @Schema(description = "Rango de fechas del período consultado.", example = "2024-10-01 - 2024-10-31")
    private String periodo;

    @Schema(description = "Cantidad total de honorarios en el período.", example = "25")
    private long cantidadHonorarios;

    @Schema(description = "Suma total de los montos de honorarios en el período.", example = "150000.50")
    private BigDecimal montoTotal;

    @Schema(description = "Monto promedio por honorario en el período.", example = "6000.02")
    private BigDecimal montoPromedio;
}