package com.rodrigo.construccion.dto.response;

import com.rodrigo.construccion.enums.EstadoEtapaDiaria;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO Response para etapas diarias
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información completa de una etapa diaria")
public class EtapaDiariaResponseDTO {

    @Schema(description = "ID de la etapa diaria")
    private Long id;

    @Schema(description = "ID de la obra")
    private Long obraId;

    @Schema(description = "ID de la empresa")
    private Long empresaId;

    @Schema(description = "Fecha de la etapa")
    private LocalDate fecha;

    @Schema(description = "Estado de la etapa")
    private EstadoEtapaDiaria estado;

    @Schema(description = "Descripción de la etapa")
    private String descripcion;

    @Schema(description = "Observaciones adicionales")
    private String observaciones;

    @Schema(description = "Fecha de creación")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación")
    private LocalDateTime fechaModificacion;

    @Schema(description = "Lista de tareas de la etapa")
    private List<TareaResponseDTO> tareas;

    /**
     * DTO interno para respuesta de tareas
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Información de una tarea")
    public static class TareaResponseDTO {
        @Schema(description = "ID de la tarea")
        private Long id;

        @Schema(description = "Descripción de la tarea")
        private String descripcion;

        @Schema(description = "Estado de la tarea")
        private String estado;

        @Schema(description = "IDs de profesionales asignados")
        private List<Long> profesionales;
    }
}
