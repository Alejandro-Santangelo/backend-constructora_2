package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO para respuestas con información de saldo de caja chica
 * Incluye totales de asignaciones, gastos y saldo actual
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información del saldo de caja chica de un profesional")
public class SaldoCajaChicaDTO {

    @Schema(description = "Nombre completo del profesional", example = "Ruben García")
    @JsonProperty("profesionalNombre")
    private String profesionalNombre;

    @Schema(description = "Tipo o rol del profesional", example = "Oficial")
    @JsonProperty("profesionalTipo")
    private String profesionalTipo;

    @Schema(description = "Total de dinero asignado al profesional", example = "5000.00")
    @JsonProperty("totalAsignado")
    private BigDecimal totalAsignado;

    @Schema(description = "Total de dinero gastado por el profesional", example = "1500.00")
    @JsonProperty("totalGastado")
    private BigDecimal totalGastado;

    @Schema(description = "Saldo actual disponible (asignado - gastado)", example = "3500.00")
    @JsonProperty("saldoActual")
    private BigDecimal saldoActual;

    @Schema(description = "ID del presupuesto", example = "68")
    @JsonProperty("presupuestoId")
    private Long presupuestoId;
}
