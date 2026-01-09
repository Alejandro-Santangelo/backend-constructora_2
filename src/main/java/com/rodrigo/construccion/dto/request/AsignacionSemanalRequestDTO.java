package com.rodrigo.construccion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de request para crear asignación semanal de profesionales
 * Acepta dos modalidades: 'total' (equipo fijo) y 'semanal' (por semana)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionSemanalRequestDTO {

    @NotNull(message = "El ID de la obra es obligatorio")
    @Positive(message = "El ID de la obra debe ser positivo")
    private Long obraId;

    @NotBlank(message = "La modalidad es obligatoria")
    private String modalidad; // "total" o "semanal"

    @NotNull(message = "Las semanas objetivo son obligatorias")
    @Positive(message = "Las semanas objetivo deben ser positivas")
    private Integer semanasObjetivo;

    // Para modalidad "total"
    @Valid
    private List<ProfesionalAsignadoDTO> profesionales;

    // Para modalidad "semanal"
    @Valid
    private List<AsignacionPorSemanaDTO> asignacionesPorSemana;
}
