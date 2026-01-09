package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para transferir datos de jornales dentro de items calculadora.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JornalCalculadoraDTO {

    @Schema(description = "ID del jornal (solo en respuestas)", example = "123")
    private Long id;

    @Schema(description = "ID temporal del frontend para rastreo", example = "1")
    private Long frontendId;

    @Schema(description = "Rol o tipo de jornal", example = "Oficial Albañil", required = true)
    @NotNull(message = "El rol es obligatorio")
    private String rol;

    @Schema(description = "Indica si es un item global del presupuesto híbrido", example = "false")
    private Boolean esGlobal = false;

    @Schema(description = "Cantidad de jornales", example = "200.00", required = true)
    @NotNull(message = "La cantidad es obligatoria")
    private BigDecimal cantidad;

    @Schema(description = "Valor unitario por jornal", example = "100000.00", required = true)
    @NotNull(message = "El valor unitario es obligatorio")
    private BigDecimal valorUnitario;

    @Schema(description = "Subtotal calculado (cantidad × valorUnitario)", example = "20000000.00")
    private BigDecimal subtotal;

    @Schema(description = "Observaciones adicionales", example = "Incluye horas extras")
    private String observaciones;

    @Schema(description = "Indica si este jornal debe incluirse en el cálculo automático de días hábiles", 
            example = "false",
            defaultValue = "false")
    private Boolean incluirEnCalculoDias = false;
}
