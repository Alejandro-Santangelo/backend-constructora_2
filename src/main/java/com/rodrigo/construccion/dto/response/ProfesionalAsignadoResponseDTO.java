package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de response para profesional asignado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesionalAsignadoResponseDTO {

    private Long profesionalId;
    private String nombre;
    private String tipoProfesional;
}
