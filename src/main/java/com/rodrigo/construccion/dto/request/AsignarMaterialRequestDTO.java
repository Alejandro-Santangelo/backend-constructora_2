package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para asignar un material del presupuesto o material global a una obra.
 * Soporta dos modos:
 * - ELEMENTO_DETALLADO: Material del presupuesto (presupuestoMaterialId requerido)
 * - CANTIDAD_GLOBAL: Material creado en el modal (descripcion y unidadMedida requeridos)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para asignar un material del presupuesto o global a una obra")
public class AsignarMaterialRequestDTO {

    @NotNull(message = "El ID de la obra es obligatorio")
    @Positive(message = "El ID de la obra debe ser un número positivo")
    @Schema(description = "ID de la obra a la que se asignará el material", example = "1")
    private Long obraId;

    @Schema(description = "ID del material del presupuesto (presupuesto_no_cliente_material). Null si es modo CANTIDAD_GLOBAL", example = "5")
    private Long presupuestoMaterialId;

    @Schema(description = "ID del material del catálogo general. Null si es manual", example = "3")
    private Long materialCatalogoId;

    @Schema(description = "Descripción del material. Requerida si esGlobal=true", example = "Cemento Portland")
    private String descripcion;

    @Schema(description = "Unidad de medida del material (kg, m2, m3, etc). Requerida si esGlobal=true", example = "kg")
    private String unidadMedida;

    @Schema(description = "Precio unitario del material. Opcional, por defecto 0.00 si no se envía", example = "1500.00")
    private BigDecimal precioUnitario;

    @NotNull(message = "La cantidad asignada es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Schema(description = "Cantidad del material a asignar a la obra", example = "100.50")
    private BigDecimal cantidadAsignada;

    @Schema(description = "Número de semana en la que se requiere el material (1-N)", example = "1")
    private Integer semana;

    @Schema(description = "Indica si es una asignación global (true) del modo CANTIDAD_GLOBAL o del presupuesto (false) en modo ELEMENTO_DETALLADO", example = "true")
    private Boolean esGlobal;

    @Schema(description = "Observaciones adicionales sobre la asignación", example = "Material para primera etapa")
    private String observaciones;
}
