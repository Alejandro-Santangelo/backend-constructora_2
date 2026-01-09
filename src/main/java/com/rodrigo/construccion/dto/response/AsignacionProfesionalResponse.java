package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO de respuesta para consultas de asignaciones de profesionales
 */
@Getter
@Setter
@Schema(description = "Información de asignación de un profesional a una obra")
public class AsignacionProfesionalResponse {

    @Schema(description = "ID de la asignación")
    public Long idAsignacion;
        @Schema(description = "ID de la asignación profesional-obra que usa el frontend")
        public Long profesionalObraId;

    @Schema(description = "ID del profesional")
    public Long profesionalId;

    @Schema(description = "Nombre del profesional")
    public String nombreProfesional;

    @Schema(description = "Tipo/especialidad del profesional")
    public String tipoProfesional;

    @Schema(description = "ID de la obra")
    public Long obraId;

    @Schema(description = "Nombre de la obra")
    public String nombreObra;

    @Schema(description = "Estado de la obra")
    public String estadoObra;

    @Schema(description = "Dirección de la obra")
    public String direccionObra;

    @Schema(description = "Fecha de inicio de la asignación")
    public LocalDate fechaDesde;

    @Schema(description = "Fecha de fin de la asignación")
    public LocalDate fechaHasta;

    @Schema(description = "Rol específico en la obra")
    public String rolEnObra;

    @Schema(description = "Valor por hora asignado")
    public Double valorHoraAsignado;

    @Schema(description = "Estado de la asignación")
    public Boolean activo;

    @Schema(description = "Fecha de creación de la asignación")
    public LocalDate fechaCreacion;

}