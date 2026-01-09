package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO con los detalles resumidos de un único jornal.")
public class JornalResumenDTO {

    @Schema(description = "ID único del jornal", example = "101")
    private Long idJornal;

    @Schema(description = "Fecha del jornal de trabajo", example = "2024-10-20")
    private LocalDate fecha;

    @Schema(description = "Horas trabajadas en el jornal", example = "8.00")
    private BigDecimal horasTrabajadas;

    @Schema(description = "Valor por hora aplicado en este jornal específico", example = "2500.00")
    private BigDecimal valorHora;

    @Schema(description = "Observaciones sobre el trabajo realizado", example = "Instalación de cañerías en planta baja.")
    private String observaciones;
}