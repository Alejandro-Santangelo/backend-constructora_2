package com.rodrigo.construccion.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Honorarios por dirección de obra, fijo o porcentaje")
public class HonorarioDireccionObraDTO {

    @Schema(description = "Valor fijo de honorario por dirección de obra", example = "10000.00", nullable = true)
    private BigDecimal valorFijo;

    @Schema(description = "Porcentaje sobre el monto total del presupuesto", example = "5.0", nullable = true)
    private Double porcentaje;

    @Schema(description = "Total calculado de honorario por dirección de obra", example = "15000.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal totalHonorarioDireccion;
}
