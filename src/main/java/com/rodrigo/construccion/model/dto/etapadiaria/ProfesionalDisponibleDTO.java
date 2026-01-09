package com.rodrigo.construccion.model.dto.etapadiaria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para profesional disponible en un día específico
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesionalDisponibleDTO {

    private Long asignacionDiaId;
    private Long profesionalId;
    private String profesionalNombre;
    private String tipoProfesional;
    private Integer cantidadJornales;
    private String semanaIso;
}
