package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de response para detalle de una semana específica
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionSemanaDetalleDTO {

    private String semanaKey; // "YYYY-Www"
    private List<ProfesionalAsignadoResponseDTO> profesionales;
    private List<DetalleDiaDTO> detallesPorDia;
}
