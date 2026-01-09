package com.rodrigo.construccion.model.dto.etapadiaria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/**
 * DTO de respuesta para una tarea individual
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaResponseDTO {

    private Long etapaId;
    private Long tareaId;
    private String nombreTarea;
    private String descripcion;
    private String categoria;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    private String prioridad;
    private List<ProfesionalAsignadoTareaDTO> profesionalesAsignados;
}
