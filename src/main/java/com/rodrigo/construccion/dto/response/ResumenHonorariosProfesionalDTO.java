package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema(description = "DTO que agrupa todos los honorarios de un profesional, con totales y datos del profesional.")
public class ResumenHonorariosProfesionalDTO {

    // --- Datos del Profesional (se muestran una sola vez) ---
    @Schema(description = "ID del profesional", example = "6")
    private Long idProfesional;

    @Schema(description = "Nombre completo del profesional", example = "Carlos Bianchi")
    private String nombreProfesional;

    @Schema(description = "Tipo de profesional", example = "Arquitecto")
    private String tipoProfesional;

    @Schema(description = "CUIT del profesional", example = "20-12345678-9")
    private String cuitProfesional;

    // --- Resumen y Totales ---
    @Schema(description = "Cantidad total de honorarios registrados para este profesional", example = "5")
    private int cantidadHonorarios;

    @Schema(description = "Suma total del monto pagado en todos los honorarios", example = "250000.00")
    private BigDecimal montoTotalPagado;

    // --- Lista de Honorarios ---
    @Schema(description = "Lista detallada de los honorarios individuales.")
    private List<HonorarioSimpleDTO> honorarios;
}