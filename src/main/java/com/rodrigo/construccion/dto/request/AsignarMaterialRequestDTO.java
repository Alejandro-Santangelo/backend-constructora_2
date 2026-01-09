package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para asignar un material del presupuesto a una obra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para asignar un material del presupuesto a una obra")
public class AsignarMaterialRequestDTO {

    @NotNull(message = "El ID de la obra es obligatorio")
    @Positive(message = "El ID de la obra debe ser un número positivo")
    @Schema(description = "ID de la obra a la que se asignará el material", example = "1")
    private Long obraId;

    @NotNull(message = "El ID del material del presupuesto es obligatorio")
    @Positive(message = "El ID del material debe ser un número positivo")
    @Schema(description = "ID del material del presupuesto (presupuesto_no_cliente_material)", example = "5")
    private Long presupuestoMaterialId;

    @NotNull(message = "La cantidad asignada es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Schema(description = "Cantidad del material a asignar a la obra", example = "100.50")
    private BigDecimal cantidadAsignada;

    @Schema(description = "Número de semana en la que se requiere el material (1-N)", example = "1")
    private Integer semana;

    @Schema(description = "Observaciones adicionales sobre la asignación", example = "Material para primera etapa")
    private String observaciones;
}
