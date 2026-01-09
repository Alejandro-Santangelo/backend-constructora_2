package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con información del material asignado a una obra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de un material asignado a una obra")
public class ObraMaterialResponseDTO {

    @Schema(description = "ID de la asignación", example = "1")
    private Long id;

    @Schema(description = "ID de la obra", example = "5")
    private Long obraId;

    @Schema(description = "Nombre de la obra", example = "Av. Libertador 1234")
    private String nombreObra;

    @Schema(description = "ID del material del presupuesto", example = "10")
    private Long presupuestoMaterialId;

    @Schema(description = "Nombre del material", example = "Cemento Portland")
    private String nombreMaterial;

    @Schema(description = "Categoría del material", example = "Materiales de construcción")
    private String categoria;

    @Schema(description = "Descripción del material", example = "Cemento para estructura")
    private String descripcionMaterial;

    @Schema(description = "Cantidad asignada a la obra", example = "100.50")
    private BigDecimal cantidadAsignada;

    @Schema(description = "Precio unitario del material", example = "850.00")
    private BigDecimal precioUnitario;

    @Schema(description = "Unidad de medida", example = "bolsa")
    private String unidadMedida;

    @Schema(description = "Total calculado (cantidadAsignada * precioUnitario)", example = "85425.00")
    private BigDecimal totalCalculado;

    @Schema(description = "Fecha de asignación", example = "2025-12-05T10:30:00")
    private LocalDateTime fechaAsignacion;

    @Schema(description = "Número de semana en la que se requiere el material", example = "1")
    private Integer semana;

    @Schema(description = "Observaciones sobre la asignación", example = "Material para primera etapa")
    private String observaciones;
}
