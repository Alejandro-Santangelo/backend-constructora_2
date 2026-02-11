package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar un material desglosado dentro de un item de calculadora.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Material desglosado de un item de calculadora")
public class MaterialCalculadoraDTO {

    @Schema(description = "ID único del material en el frontend", example = "1730882345679")
    private Long id;

    @Schema(description = "Nombre del material", example = "Pintura látex blanca", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Indica si es un item global del presupuesto híbrido", example = "false")
    private Boolean esGlobal = false;

    @Schema(description = "Descripción detallada del material", example = "Pintura látex blanca interior premium 4L")
    private String descripcion;

    @Schema(description = "Observaciones sobre el material", example = "Marca Alba o similar, color blanco mate")
    private String observaciones;

    @Schema(description = "Unidad de medida", example = "litros", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unidad;

    @Schema(description = "Cantidad necesaria", example = "3.5")
    private BigDecimal cantidad;

    @Schema(description = "Precio unitario", example = "25000.00")
    private BigDecimal precio;

    @Schema(description = "Subtotal calculado (cantidad * precio)", example = "87500.00") 
    private BigDecimal subtotal;
}