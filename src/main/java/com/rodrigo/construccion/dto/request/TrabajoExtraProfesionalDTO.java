package com.rodrigo.construccion.dto.request;

import com.rodrigo.construccion.enums.TipoProfesionalTrabajoExtra;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para profesionales en trabajos extra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un profesional en un trabajo extra")
public class TrabajoExtraProfesionalDTO {

    @Schema(description = "ID del profesional (nulleable si es manual)")
    private Long profesionalId;

    @NotBlank(message = "El nombre del profesional es obligatorio")
    @Schema(description = "Nombre del profesional", example = "Juan Pérez", required = true)
    private String nombre;

    @Schema(description = "Especialidad del profesional", example = "Electricista")
    private String especialidad;

    @NotNull(message = "El tipo de profesional es obligatorio")
    @Schema(description = "Tipo de profesional: ASIGNADO_OBRA, LISTADO_GENERAL, MANUAL", 
            example = "ASIGNADO_OBRA", required = true)
    private TipoProfesionalTrabajoExtra tipo;

    @Schema(description = "Importe/costo por día del profesional", example = "100000")
    private java.math.BigDecimal importe;
}
