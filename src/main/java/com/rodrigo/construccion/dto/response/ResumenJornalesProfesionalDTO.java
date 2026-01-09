package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que agrupa todos los jornales de un profesional en una obra específica, con totales.")
public class ResumenJornalesProfesionalDTO {

    // --- Datos del Profesional (se muestran una sola vez) ---
    @Schema(description = "ID del profesional", example = "6")
    private Long idProfesional;

    @Schema(description = "Nombre completo del profesional", example = "Carlos Bianchi")
    private String nombreProfesional;

    @Schema(description = "Tipo de profesional", example = "Arquitecto")
    private String tipoProfesional;

    // --- Datos de la Obra y Asignación (se muestran una sola vez) ---
    @Schema(description = "ID de la obra", example = "10")
    private Long idObra;

    @Schema(description = "Nombre de la obra", example = "Diseño y Construcción Stand para Expo")
    private String nombreObra;

    @Schema(description = "Rol del profesional en esta obra", example = "Diseñador de Stand")
    private String rolEnObra;

    // --- Resumen y Totales ---
    @Schema(description = "Cantidad total de jornales registrados para esta asignación", example = "13")
    private int cantidadJornales;

    @Schema(description = "Suma total de horas trabajadas", example = "104.00")
    private BigDecimal totalHorasTrabajadas;

    @Schema(description = "Suma total del monto a pagar por todos los jornales", example = "260000.00")
    private BigDecimal montoTotalJornales;

    // --- Lista de Jornales ---
    @Schema(description = "Lista detallada de los jornales individuales.")
    private List<JornalResumenDTO> jornales;
}