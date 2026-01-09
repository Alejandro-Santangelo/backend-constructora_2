package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodrigo.construccion.enums.EstadoTareaTrabajoExtra;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO Response para tareas en trabajos extra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de respuesta de una tarea en un trabajo extra")
public class TrabajoExtraTareaResponseDTO {

    @Schema(description = "ID de la tarea")
    private Long id;

    @Schema(description = "Descripción de la tarea")
    private String descripcion;

    @Schema(description = "Estado de la tarea")
    private EstadoTareaTrabajoExtra estado;

    @Schema(description = "Importe de la tarea")
    private BigDecimal importe;

    @JsonProperty("profesionalesAsignados")
    @Schema(description = "Índices de los profesionales asignados a esta tarea")
    private List<Integer> profesionalesAsignados;
}
