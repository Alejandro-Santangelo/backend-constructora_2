package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para recibir profesionales desde el formulario de creación de obras
 * Puede ser un profesional existente o uno nuevo ingresado manualmente
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos de profesional desde formulario (existente o nuevo)")
public class ProfesionalFormDTO {

    @Schema(description = "ID del profesional (numérico si existe, string con 'manual_' si es nuevo)", 
            example = "166 o manual_1733456789123")
    private String id;

    @Schema(description = "Nombre del profesional", example = "Gabi Nieto", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Tipo de profesional", example = "Albañileria", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tipoProfesional;

    @Schema(description = "Valor por hora", example = "5000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal valorHora;

    @Schema(description = "Cantidad de jornales asignados (opcional)", example = "21")
    private BigDecimal cantidadJornales;

    @Schema(description = "Indica si es un profesional ingresado manualmente (true) o existente del sistema (false)", 
            example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean esManual;
}
