package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO básico de profesional (solo ID y nombre)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalBasicoDTO {

    @NotNull(message = "El ID del profesional es obligatorio")
    @Positive(message = "El ID del profesional debe ser positivo")
    private Long profesionalId;

    private String nombre; // Informativo, viene del frontend
}
