package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO para actualizar una asignación profesional-obra existente
 * Permite modificar uno o todos los campos de una asignación
 */
@Getter
@Setter
@Schema(description = "Request para actualizar una asignación profesional-obra")
public class ActualizarAsignacionRequest {

    @Schema(description = "Nuevo tipo de profesional (opcional)", example = "Oficial Albañil")
    public String profesional;

    @Schema(description = "Nueva obra a asignar (opcional)", example = "1")
    public Long obraId;

    @Schema(description = "Nueva empresa (opcional)", example = "1")
    public Long empresaId;

    @Schema(description = "Nueva fecha de inicio (opcional)", example = "2025-10-04")
    public LocalDate fechaDesde;

    @Schema(description = "Nueva fecha de fin (opcional)", example = "2025-12-31")
    public LocalDate fechaHasta;

    @Schema(description = "Nuevo rol en la obra (opcional)", example = "Jefe de Obra")
    public String rolEnObra;

    @Schema(description = "Nuevo valor por hora (opcional)", example = "25000")
    public Double valorHoraAsignado;

    @Schema(description = "Nuevo estado de la asignación (opcional)", example = "true")
    public Boolean activo;

}