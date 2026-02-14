package com.rodrigo.construccion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar un profesional en un trabajo adicional (request y response)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoAdicionalProfesionalDTO {

    /**
     * ID del profesional asignado (solo en responses)
     */
    private Long id;

    /**
     * ID del profesional registrado (si esRegistrado=true)
     */
    private Long profesionalId;

    @NotBlank(message = "El nombre del profesional es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de profesional es obligatorio")
    private String tipoProfesional;

    private BigDecimal honorarioDia;

    private String telefono;

    private String email;

    @NotNull(message = "Debe indicar si es profesional registrado")
    private Boolean esRegistrado;

    /**
     * Fecha de asignación (solo en responses)
     */
    private String fechaAsignacion;
}
