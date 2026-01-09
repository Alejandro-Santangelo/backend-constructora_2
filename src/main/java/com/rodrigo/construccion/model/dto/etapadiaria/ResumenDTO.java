package com.rodrigo.construccion.model.dto.etapadiaria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resumen de etapas diarias
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenDTO {

    private Integer totalTareas;
    private Integer totalProfesionalesDisponibles;
    private Integer totalAsignaciones;
}
