package com.rodrigo.construccion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para asignación de profesionales en una semana específica (modalidad "semanal")
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionPorSemanaDTO {

    @NotBlank(message = "La clave de semana es obligatoria")
    private String semanaKey; // Formato: "YYYY-Www" (ej: "2025-W49")

    @NotEmpty(message = "Debe haber al menos un profesional asignado")
    @Valid
    private List<ProfesionalBasicoDTO> profesionales;

    @NotEmpty(message = "Debe haber al menos un día con cantidad especificada")
    private Map<String, String> cantidadesPorDia; // {"2025-12-02": "3", "2025-12-03": "2"}
}
