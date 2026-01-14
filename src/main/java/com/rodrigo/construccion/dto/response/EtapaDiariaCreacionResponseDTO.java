package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para creación de etapas diarias
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapaDiariaCreacionResponseDTO {

    private Long etapaId;
    private Integer tareasCreadas;
    private Integer asignacionesProfesionales;
    private List<TareaCreacionDTO> tareas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TareaCreacionDTO {
        private Long tareaId;
        private String nombreTarea;
        private Integer profesionalesAsignados;
    }
}
