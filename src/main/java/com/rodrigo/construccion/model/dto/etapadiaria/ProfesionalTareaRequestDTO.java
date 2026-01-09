package com.rodrigo.construccion.model.dto.etapadiaria;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para asignar un profesional a una tarea específica
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalTareaRequestDTO {

    @NotNull(message = "El ID de asignación del día es obligatorio")
    private Long asignacionDiaId;

    private BigDecimal horasAsignadas;

    private String rol; // Responsable, Ayudante, Supervisor, etc.

    private String notas;
}
