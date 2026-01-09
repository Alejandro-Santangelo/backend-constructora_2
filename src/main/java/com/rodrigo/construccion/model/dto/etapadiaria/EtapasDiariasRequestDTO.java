package com.rodrigo.construccion.model.dto.etapadiaria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear/actualizar etapas diarias con sus tareas y profesionales asignados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtapasDiariasRequestDTO {

    @NotNull(message = "El ID de la obra es obligatorio")
    private Long obraId;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotEmpty(message = "Debe especificar al menos una tarea")
    @Valid
    private List<TareaRequestDTO> tareas;
}
