package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodrigo.construccion.enums.TipoProfesionalTrabajoExtra;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO Response para profesionales en trabajos extra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de respuesta de un profesional en un trabajo extra")
public class TrabajoExtraProfesionalResponseDTO {

    @Schema(description = "ID del registro de profesional en trabajo extra")
    private Long id;

    @Schema(description = "ID del profesional (null si es manual)")
    private Long profesionalId;

    @Schema(description = "Nombre del profesional")
    private String nombre;

    @Schema(description = "Especialidad del profesional")
    private String especialidad;

    @Schema(description = "Tipo de profesional: ASIGNADO_OBRA, LISTADO_GENERAL, MANUAL")
    private TipoProfesionalTrabajoExtra tipo;

    @Schema(description = "Importe/costo del profesional")
    private BigDecimal importe;
}
