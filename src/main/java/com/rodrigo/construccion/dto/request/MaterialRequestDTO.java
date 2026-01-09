package com.rodrigo.construccion.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para crear un nuevo material")
public class MaterialRequestDTO {

    @Schema(description = "Total calculado para este material (cantidad * precio unitario)", example = "50000.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal totalMaterial;

    @Schema(description = "ID del material (se genera automáticamente)", example = "0", hidden = true)
    private Long id;

    @Schema(description = "Nombre del material", example = "Cemento", required = true)
    private String nombre;

    @Schema(description = "Cantidad requerida", example = "100", required = true)
    private Double cantidad;

    @Schema(description = "Unidad de medida", example = "kg", required = true)
    private String unidadMedida;

    @Schema(description = "Precio unitario", example = "500.00", required = true)
    private BigDecimal precioUnitario;

    @Schema(description = "Observaciones del material", example = "Material de alta calidad")
    private String observaciones;
}
