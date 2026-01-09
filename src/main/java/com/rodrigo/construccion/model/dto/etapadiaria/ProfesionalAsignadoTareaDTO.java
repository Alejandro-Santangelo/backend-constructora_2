package com.rodrigo.construccion.model.dto.etapadiaria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para profesional asignado a una tarea
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfesionalAsignadoTareaDTO {

    private Long asignacionDiaId;
    private Long profesionalId;
    private String profesionalNombre;
    private BigDecimal horasAsignadas;
    private String rol;
    private String estado;
    private String notas;
}
