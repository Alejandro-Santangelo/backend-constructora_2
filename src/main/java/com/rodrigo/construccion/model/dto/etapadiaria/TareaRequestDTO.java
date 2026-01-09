package com.rodrigo.construccion.model.dto.etapadiaria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO para una tarea individual dentro de una etapa diaria
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaRequestDTO {

    @NotBlank(message = "El nombre de la tarea es obligatorio")
    private String nombreTarea;

    private String descripcion;
    
    private String categoria;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private String estado; // PENDIENTE, EN_PROCESO, COMPLETADA, CANCELADA

    private String prioridad; // BAJA, MEDIA, ALTA, URGENTE

    @NotEmpty(message = "Debe asignar al menos un profesional a la tarea")
    @Valid
    private List<ProfesionalTareaRequestDTO> profesionales;
}
