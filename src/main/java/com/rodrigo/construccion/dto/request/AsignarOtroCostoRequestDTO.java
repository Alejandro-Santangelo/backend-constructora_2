package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para asignar un otro costo del presupuesto a una obra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para asignar un otro costo del presupuesto a una obra")
public class AsignarOtroCostoRequestDTO {

    @NotNull(message = "El ID de la obra es obligatorio")
    @Positive(message = "El ID de la obra debe ser un número positivo")
    @Schema(description = "ID de la obra a la que se asignará el costo", example = "1")
    private Long obraId;

    @NotNull(message = "El ID del otro costo del presupuesto es obligatorio")
    @Positive(message = "El ID del costo debe ser un número positivo")
    @Schema(description = "ID del otro costo del presupuesto (presupuesto_no_cliente_otro_costo)", example = "3")
    private Long presupuestoOtroCostoId;

    @NotNull(message = "El ID del gasto general es obligatorio")
    @Positive(message = "El ID del gasto general debe ser un número positivo")
    @Schema(description = "ID del gasto general de la calculadora", example = "5")
    private Long gastoGeneralId;

    @NotNull(message = "El importe asignado es obligatorio")
    @Positive(message = "El importe debe ser mayor a cero")
    @Schema(description = "Importe del costo a asignar a la obra", example = "15000.00")
    private BigDecimal importeAsignado;

    @Schema(description = "Número de semana en la que se requiere el gasto/costo (1-N)", example = "2")
    private Integer semana;

    @Schema(description = "Observaciones adicionales sobre la asignación", example = "Costo de transporte para materiales")
    private String observaciones;
}
