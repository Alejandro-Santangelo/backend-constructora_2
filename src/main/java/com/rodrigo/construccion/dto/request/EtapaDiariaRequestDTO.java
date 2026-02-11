package com.rodrigo.construccion.dto.request;

import com.rodrigo.construccion.enums.EstadoEtapaDiaria;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO Request para crear o actualizar una etapa diaria
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para crear o actualizar una etapa diaria")
public class EtapaDiariaRequestDTO {

    @NotNull(message = "El ID de la obra es obligatorio")
    @Schema(description = "ID de la obra", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long obraId;

    @NotNull(message = "La fecha es obligatoria")
    @Schema(description = "Fecha de la etapa", example = "2025-12-03", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fecha;

    @NotNull(message = "El estado es obligatorio")
    @Schema(description = "Estado de la etapa: TERMINADA, EN_PROCESO, SUSPENDIDA, MODIFICADA, CANCELADA", 
            example = "TERMINADA", requiredMode = Schema.RequiredMode.REQUIRED)
    private EstadoEtapaDiaria estado;

    @Schema(description = "Descripción de la etapa", example = "Completado el revoque de planta baja")
    private String descripcion;

    @Schema(description = "Observaciones adicionales", example = "Pendiente revisión de humedad")
    private String observaciones;

    @Schema(description = "Lista de tareas de la etapa")
    private List<TareaEtapaDiariaDTO> tareas;
}
