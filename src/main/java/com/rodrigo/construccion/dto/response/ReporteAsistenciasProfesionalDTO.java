package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para el resumen de asistencias por profesional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAsistenciasProfesionalDTO {

    private Long profesionalObraId;
    private String nombreProfesional;
    private BigDecimal totalHoras;
    private Integer diasTrabajados;
}
