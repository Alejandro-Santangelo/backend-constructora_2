package com.rodrigo.construccion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar el estado de un trabajo adicional
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarEstadoTrabajoAdicionalDTO {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "PENDIENTE|EN_PROGRESO|COMPLETADO|CANCELADO", 
             message = "El estado debe ser: PENDIENTE, EN_PROGRESO, COMPLETADO o CANCELADO")
    private String estado;
}
