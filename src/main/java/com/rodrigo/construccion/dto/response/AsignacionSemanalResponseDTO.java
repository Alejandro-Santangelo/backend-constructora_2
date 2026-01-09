package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de response para consultar asignaciones semanales de una obra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionSemanalResponseDTO {

    private Long asignacionId;
    private String modalidad; // "total" o "semanal"
    private Integer semanasObjetivo;
    private Integer totalJornalesAsignados;
    private List<AsignacionSemanaDetalleDTO> asignacionesPorSemana;
}
