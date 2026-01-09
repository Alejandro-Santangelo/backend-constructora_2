package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de response general para operaciones exitosas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionSemanalCreacionResponseDTO {

    private Boolean success;
    private String message;
    private DatosAsignacionDTO data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DatosAsignacionDTO {
        private Long asignacionId;
        private Integer totalJornalesAsignados;
        private Integer diasHabiles;
        private Integer profesionalesAsignados;
    }
}
