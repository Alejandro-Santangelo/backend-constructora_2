package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para asignar profesional a rubro de obra")
public class AsignacionProfesionalRequestDTO {

    @NotNull(message = "El ID del profesional es obligatorio")
    @Schema(description = "ID del profesional a asignar", example = "45", required = true)
    private Long profesionalId;

    @NotNull(message = "El ID del rubro es obligatorio")
    @Schema(description = "ID del rubro del presupuesto", example = "1689", required = true)
    private Long rubroId;

    @NotNull(message = "El ID del item es obligatorio")
    @Schema(description = "ID del profesional o jornal específico dentro del rubro",
            example = "697",
            required = true)
    private Long itemId;

    @Schema(description = "Nombre del rubro del presupuesto", example = "Albañilería")
    private String rubroNombre;

    @NotBlank(message = "El tipo de asignación es obligatorio")
    @Schema(description = "Tipo de asignación: PROFESIONAL (solo rol) o JORNAL (consume jornales)",
            example = "JORNAL",
            allowableValues = {"PROFESIONAL", "JORNAL"},
            required = true)
    private String tipoAsignacion;

    @PositiveOrZero(message = "La cantidad de jornales debe ser mayor o igual a cero")
    @Schema(description = "Cantidad de jornales a asignar (solo si tipoAsignacion=JORNAL)", example = "10")
    private Integer cantidadJornales;

    @Schema(description = "Fecha de inicio de la asignación", example = "2025-12-01")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de fin de la asignación", example = "2025-12-15")
    private LocalDate fechaFin;

    @Schema(description = "Observaciones adicionales", example = "Asignado para trabajos de cimientos")
    private String observaciones;
}
