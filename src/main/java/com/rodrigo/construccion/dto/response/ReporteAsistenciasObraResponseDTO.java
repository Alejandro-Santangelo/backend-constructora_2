package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para el reporte de asistencias de una obra
 * Contiene información agregada por profesional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAsistenciasObraResponseDTO {

    private String direccionObra;
    private Integer totalAsistencias;
    private List<ReporteAsistenciasProfesionalDTO> resumenPorProfesional;
}
