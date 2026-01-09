package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO para jornales en presupuesto sin cliente
 */
@Data
public class JornalDTO {

    @Schema(description = "ID del jornal (null al crear)", example = "1")
    private Long id;

    @Schema(description = "Rol del jornal", example = "Albañil", required = true)
    @NotBlank(message = "El rol es obligatorio")
    private String rol;

    @Schema(description = "Cantidad de jornales", example = "10.5", required = true)
    @PositiveOrZero(message = "La cantidad debe ser positiva o cero")
    private BigDecimal cantidad;

    @Schema(description = "Valor unitario por jornal", example = "25000.00", required = true)
    @PositiveOrZero(message = "El valor unitario debe ser positivo o cero")
    private BigDecimal valorUnitario;

    @Schema(description = "Subtotal calculado (cantidad * valorUnitario)", example = "262500.00")
    private BigDecimal subtotal;

    @Schema(description = "Observaciones adicionales", example = "Incluye traslado")
    private String observaciones;

    @Schema(description = "Indica si este jornal debe incluirse en el cálculo automático de días hábiles", 
            example = "false",
            defaultValue = "false")
    private Boolean incluirEnCalculoDias = false;
}
