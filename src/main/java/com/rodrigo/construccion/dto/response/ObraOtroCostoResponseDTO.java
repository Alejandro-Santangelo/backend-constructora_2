package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con información del otro costo asignado a una obra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de un otro costo asignado a una obra")
public class ObraOtroCostoResponseDTO {

    @Schema(description = "ID de la asignación", example = "1")
    private Long id;

    @Schema(description = "ID de la obra", example = "5")
    private Long obraId;

    @Schema(description = "Nombre de la obra", example = "Av. Libertador 1234")
    private String nombreObra;

    @Schema(description = "ID del otro costo del presupuesto", example = "7")
    private Long presupuestoOtroCostoId;

    @Schema(description = "ID del gasto general", example = "5")
    private Long gastoGeneralId;

    @Schema(description = "Categoría del costo", example = "Transporte")
    private String categoria;

    @Schema(description = "Descripción del costo", example = "Flete de materiales")
    private String descripcion;

    @Schema(description = "Importe asignado a la obra", example = "15000.00")
    private BigDecimal importeAsignado;

    @Schema(description = "Fecha de asignación", example = "2025-12-05T10:30:00")
    private LocalDateTime fechaAsignacion;

    @Schema(description = "Número de semana en la que se requiere el gasto/costo", example = "2")
    private Integer semana;

    @Schema(description = "Observaciones sobre la asignación", example = "Costo de transporte para materiales")
    private String observaciones;
}
