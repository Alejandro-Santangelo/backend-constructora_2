package com.rodrigo.construccion.dto.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresupuestoProfesionalResponseDTO {
    private Long id;
    private String tipo;
    private BigDecimal honorarioHora;
    private BigDecimal honorarioDia;
    private BigDecimal honorarioSemana;
    private BigDecimal honorarioMes;
    private Integer horas;
    private Integer dias;
    private Integer semanas;
    private Integer meses;
    private BigDecimal totalHonorarios;
}
