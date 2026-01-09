package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO Request para registrar check-in (entrada) de asistencia
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para registrar entrada (check-in) de un profesional a la obra")
public class CheckInRequest {

    @NotNull(message = "El ID del profesional obra es obligatorio")
    @Schema(description = "ID de la asignación del profesional a la obra", example = "1", required = true)
    private Long profesionalObraId;

    @NotNull(message = "La fecha es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de la asistencia", example = "2025-10-22", required = true)
    private LocalDate fecha;

    @NotNull(message = "La hora de entrada es obligatoria")
    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora de entrada", example = "08:00:00", required = true)
    private LocalTime horaEntrada;

    @NotNull(message = "La latitud de entrada es obligatoria")
    @Schema(description = "Latitud de ubicación de entrada", example = "-34.603722", required = true)
    private Double latitudEntrada;

    @NotNull(message = "La longitud de entrada es obligatoria")
    @Schema(description = "Longitud de ubicación de entrada", example = "-58.381592", required = true)
    private Double longitudEntrada;

    @NotNull(message = "El ID de empresa es obligatorio")
    @Schema(description = "ID de la empresa", example = "1", required = true)
    private Long empresaId;
}
