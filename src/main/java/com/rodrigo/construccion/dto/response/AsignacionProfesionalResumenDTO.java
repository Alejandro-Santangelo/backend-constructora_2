package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con resumen de una asignación de profesional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionProfesionalResumenDTO {
    
    /**
     * ID de la asignación en tabla asignaciones_profesional_obra
     */
    private Long asignacionId;
    
    /**
     * ID del profesional
     */
    private Long profesionalId;
    
    /**
     * Nombre completo del profesional
     */
    private String profesionalNombre;
    
    /**
     * Tipo de profesional (Oficial Albañil, Ayudante, etc.)
     */
    private String profesionalTipo;
    
    /**
     * Cantidad de días asignados a este profesional
     */
    private Integer diasAsignados;
    
    /**
     * Total de jornales asignados (suma de cantidades de todos los días)
     */
    private Integer totalJornales;
}
