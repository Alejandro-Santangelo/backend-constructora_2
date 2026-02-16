package com.rodrigo.construccion.dto.request;

import com.rodrigo.construccion.enums.OrigenFondos;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para asignar un otro costo (del presupuesto o manual) a una obra.
 * Soporta asignaciones semanales (sin fechaAsignacion) y diarias (con fechaAsignacion).
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

    @Schema(description = "ID del otro costo del presupuesto (presupuesto_no_cliente_otro_costo). Null si es manual.", example = "3")
    private Long presupuestoOtroCostoId;

    @Schema(description = "ID del gasto general de la calculadora. Null si es manual.", example = "5")
    private Long gastoGeneralId;

    @NotNull(message = "El importe asignado es obligatorio")
    @Positive(message = "El importe debe ser mayor a cero")
    @Schema(description = "Importe del costo a asignar a la obra", example = "15000.00")
    private BigDecimal importeAsignado;

    @NotNull(message = "El número de semana es obligatorio")
    @Schema(description = "Número de semana en la que se requiere el gasto/costo (1-N)", example = "2")
    private Integer semana;

    @Schema(description = "Fecha de asignación específica. Solo para asignaciones diarias. Null para asignaciones semanales.", example = "2026-01-12")
    private LocalDate fechaAsignacion;

    @Schema(description = "Descripción del gasto/costo. Requerida si es manual.", example = "Volquetes")
    private String descripcion;

    @Schema(description = "Categoría del gasto. Requerida si es manual.", example = "Albañilería")
    private String categoria;

    @Schema(description = "Indica si es una asignación global (true) que aplica a toda la obra o específica a item/etapa (false)", example = "true")
    private Boolean esGlobal;

    @Schema(description = "Observaciones adicionales sobre la asignación", example = "Volquetes [Gasto Semanal Global]")
    private String observaciones;

    @Schema(description = "Origen de los fondos cuando presupuesto=0 (RETIRO_DIRECTO o PRESUPUESTO_MATERIALES). Opcional.", example = "RETIRO_DIRECTO")
    private OrigenFondos origenFondos;
}
