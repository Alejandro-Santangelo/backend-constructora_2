package com.rodrigo.construccion.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Respuesta de material en presupuesto, con total calculado")
public class MaterialResponseDTO {
    @Schema(description = "ID del material (si es de catálogo)", example = "1", nullable = true)
    private Long id;
    @Schema(description = "Nombre del material", example = "Cemento")
    private String nombre;
    @Schema(description = "Cantidad requerida", example = "100")
    private Double cantidad;
    @Schema(description = "Unidad de medida", example = "kg")
    private String unidadMedida;
    @Schema(description = "Precio unitario", example = "500.00")
    private BigDecimal precioUnitario;
    @Schema(description = "Observaciones", example = "Material de alta calidad")
    private String observaciones;
    @Schema(description = "Total calculado para este material (cantidad * precio unitario)", example = "50000.00")
    private BigDecimal totalMaterial;
}
