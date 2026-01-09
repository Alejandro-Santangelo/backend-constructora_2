package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO de respuesta para consultas de disponibilidad de profesionales
 */
@Getter
@Setter
@Schema(description = "Información de disponibilidad de un profesional")
public class DisponibilidadProfesionalResponse {

    @Schema(description = "ID del profesional")
    public Long id;

    @Schema(description = "Nombre completo del profesional")
    public String nombre;

    @Schema(description = "Tipo de profesional")
    public String tipoProfesional;

    @Schema(description = "especialidad del profesional")
    private String especialidad;

    @Schema(description = "Email de contacto")
    public String email;

    @Schema(description = "Teléfono de contacto")
    public String telefono;

    @Schema(description = "Indica si el profesional está disponible para nuevas asignaciones")
    public Boolean disponible;

    @Schema(description = "ID de la obra actual si está asignado")
    public Long obraActual;

    @Schema(description = "Nombre de la obra actual si está asignado")
    public String nombreObraActual;

    @Schema(description = "Fecha hasta cuando está asignado en la obra actual")
    public LocalDate fechaFinAsignacionActual;

    @Schema(description = "Valor por hora por defecto del profesional")
    public Double valorHoraDefault;

    @Schema(description = "Estado activo del profesional")
    public Boolean activo;

    @Schema(description = "Estado de disponibilidad del profesional ('DISPONIBLE' u 'OCUPADO')")
    public String estado;

}