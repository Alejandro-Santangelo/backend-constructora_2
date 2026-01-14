package com.rodrigo.construccion.dto.response;

import com.rodrigo.construccion.dto.ProfesionalDisponibleDTO;
import com.rodrigo.construccion.dto.ResumenDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta principal para etapas diarias
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtapasDiariasResponseDTO {

    private LocalDate fecha;
    private Long obraId;
    private String obraNombre;
    private List<ProfesionalDisponibleDTO> profesionalesDisponibles;
    private List<TareaResponseDTO> tareas;
    private ResumenDTO resumen;
}
