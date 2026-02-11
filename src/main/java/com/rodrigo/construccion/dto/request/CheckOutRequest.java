package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * DTO Request para registrar check-out (salida) de asistencia
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para registrar salida (check-out) de un profesional de la obra")
public class CheckOutRequest {

    @NotNull(message = "La hora de salida es obligatoria")
    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora de salida", example = "17:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalTime horaSalida;

    @NotNull(message = "La latitud de salida es obligatoria")
    @Schema(description = "Latitud de ubicación de salida", example = "-34.603722", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double latitudSalida;

    @NotNull(message = "La longitud de salida es obligatoria")
    @Schema(description = "Longitud de ubicación de salida", example = "-58.381592", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double longitudSalida;

    @NotNull(message = "El ID de empresa es obligatorio")
    @Schema(description = "ID de la empresa", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long empresaId;
}
