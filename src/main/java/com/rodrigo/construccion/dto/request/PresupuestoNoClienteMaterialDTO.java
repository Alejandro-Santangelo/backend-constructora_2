package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para material en presupuesto sin cliente")
public class PresupuestoNoClienteMaterialDTO {

    @Schema(description = "ID del material (solo para actualización)", example = "1")
    private Long id;

    @Schema(description = "Nombre del material", example = "Cemento Portland", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombreMaterial;

    @Schema(description = "Categoría del material", example = "Materiales de construcción")
    private String categoria;

    @Schema(description = "Cantidad", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal cantidad;

    @Schema(description = "Unidad de medida", example = "Bolsas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unidadMedida;

    @Schema(description = "Precio unitario", example = "12500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal precioUnitario;

    @Schema(description = "Subtotal (cantidad * precio unitario)", example = "625000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal subtotal;

    @Schema(description = "Observaciones adicionales", example = "Marca Loma Negra")
    private String observaciones;
}
