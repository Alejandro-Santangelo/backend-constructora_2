package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.rodrigo.construccion.enums.EstadoTareaTrabajoExtra;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para tareas en trabajos extra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de una tarea en un trabajo extra")
public class TrabajoExtraTareaDTO {

    @NotBlank(message = "La descripción de la tarea es obligatoria")
    @Schema(description = "Descripción de la tarea", example = "Instalar cableado en planta baja", requiredMode = Schema.RequiredMode.REQUIRED)
    private String descripcion;

    @NotNull(message = "El estado de la tarea es obligatorio")
    @Schema(description = "Estado de la tarea: TERMINADA, A_TERMINAR, POSTERGADA, SUSPENDIDA", 
            example = "TERMINADA", requiredMode = Schema.RequiredMode.REQUIRED)
    private EstadoTareaTrabajoExtra estado;

    @Schema(description = "Importe de la tarea", example = "15000.00")
    private BigDecimal importe;

    @JsonAlias({"profesionalesAsignados", "profesionalesIndices"})
    @Schema(description = "Índices de los profesionales asignados a esta tarea (referencia a la posición en la lista de profesionales)")
    private List<Integer> profesionalesIndices;
}
