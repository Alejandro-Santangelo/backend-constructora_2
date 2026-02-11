package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para configuración de profesional en presupuesto sin cliente")
public class PresupuestoNoClienteProfesionalDTO {

    @Schema(description = "ID del profesional (solo para actualización)", example = "1")
    private Long id;

    @Schema(description = "Tipo de profesional", example = "Maestro Mayor de Obras", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tipoProfesional;

    @Schema(description = "Importe por hora", example = "5000.00")
    private BigDecimal importeHora;

    @Schema(description = "Importe por día", example = "40000.00")
    private BigDecimal importeDia;

    @Schema(description = "Importe por semana", example = "200000.00")
    private BigDecimal importeSemana;

    @Schema(description = "Importe por mes", example = "800000.00")
    private BigDecimal importeMes;

    @Schema(description = "Cantidad de horas", example = "160")
    private Integer cantidadHoras;

    @Schema(description = "Cantidad de días", example = "20")
    private Integer cantidadDias;

    @Schema(description = "Cantidad de semanas", example = "4")
    private Integer cantidadSemanas;

    @Schema(description = "Cantidad de meses", example = "1")
    private Integer cantidadMeses;

    @Schema(description = "Subtotal calculado", example = "800000.00")
    private BigDecimal subtotal;

    @Schema(description = "Observaciones adicionales", example = "Incluye beneficios sociales")
    private String observaciones;

    // Nuevos campos para manejo de unidades (jornales vs horas)
    @Schema(description = "Tipo de unidad de medida: 'jornales' o 'horas'. Por defecto 'jornales'", 
            example = "horas", 
            allowableValues = {"jornales", "horas"})
    private String tipoUnidad = "jornales";

    @Schema(description = "Cantidad de jornales/días en los que se distribuyen las horas. Solo aplica cuando tipoUnidad = 'horas'. Ejemplo: 2 horas por día durante 5 días", 
            example = "5")
    private BigDecimal cantidadJornales;
}
