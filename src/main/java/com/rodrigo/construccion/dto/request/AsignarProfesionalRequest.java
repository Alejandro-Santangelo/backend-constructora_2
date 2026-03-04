package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO para crear una nueva asignación profesional-obra por tipo de profesional
 * Permite asignar un profesional específico por su tipo a una obra
 */
@Getter
@Setter
@Schema(description = "Request para asignar un profesional por tipo a una obra")
public class AsignarProfesionalRequest {

    // --- NUEVO CAMPO Y MÉTODOS ---
    @Schema(description = "ID del profesional a asignar (opcional)", example = "1")
    private Long profesionalId;

    @Schema(description = "Tipo de profesional a asignar (obligatorio si no se usa profesionalId)", example = "Oficial Albañil")
    public String tipoProfesional;

    @Schema(description = "Nombre completo del profesional (obligatorio si no se usa profesionalId)", example = "Juan Pérez")
    public String nombre;

    @NotNull(message = "El ID de la obra es obligatorio")
    @Positive(message = "El ID de la obra debe ser positivo")
    @Schema(description = "ID de la obra donde se asignará el profesional", example = "1", required = true)
    public Long obraId;

    @NotNull(message = "El ID de la empresa es obligatorio")
    @Positive(message = "El ID de la empresa debe ser mayor que cero")
    @Schema(description = "ID de la empresa que gestiona la obra", example = "1", required = true)
    public Long empresaId;

    @Schema(description = "Fecha de inicio de la asignación (por defecto: fecha actual)", example = "2025-10-04")
    public LocalDate fechaDesde;

    @Schema(description = "Fecha de fin de la asignación (opcional)", example = "2025-12-31")
    public LocalDate fechaHasta;

    @Schema(description = "Rol específico del profesional en la obra", example = "Jefe de Obra")
    public String rolEnObra;

    @Schema(description = "Valor por hora asignado para esta obra específica", example = "25000")
    public Double valorHoraAsignado;

    @Schema(description = "Cantidad de jornales asignados (opcional, por defecto 0)", example = "20")
    public Integer cantidadJornales;

    @Schema(description = "Estado de la asignación", example = "true", defaultValue = "true")
    public Boolean activo = true;

}