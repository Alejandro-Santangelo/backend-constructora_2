package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO Request para Tarea de Etapa Diaria
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para crear o actualizar una tarea de etapa diaria")
public class TareaEtapaDiariaDTO {

    @Schema(description = "ID de la tarea (solo para actualización)")
    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    @Schema(description = "Descripción de la tarea", example = "Colocación de cerámicos", required = true)
    private String descripcion;

    @NotBlank(message = "El estado es obligatorio")
    @Schema(description = "Estado: PENDIENTE, EN_PROCESO, COMPLETADA", example = "PENDIENTE", required = true)
    private String estado;

    @Schema(description = "IDs de profesionales asignados a esta tarea", example = "[1, 2, 3]")
    private List<Long> profesionales;
}
