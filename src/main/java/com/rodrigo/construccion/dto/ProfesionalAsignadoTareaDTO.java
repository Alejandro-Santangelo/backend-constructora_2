package com.rodrigo.construccion.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
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
