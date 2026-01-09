package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para profesional asignado en modalidad "total" (equipo fijo)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalAsignadoDTO {

    @NotNull(message = "El ID del profesional es obligatorio")
    @Positive(message = "El ID del profesional debe ser positivo")
    private Long profesionalId;

    private String nombre; // Informativo, viene del frontend

    @NotNull(message = "La cantidad por día es obligatoria")
    @PositiveOrZero(message = "La cantidad por día debe ser mayor o igual a cero")
    private Integer cantidadPorDia;
}
