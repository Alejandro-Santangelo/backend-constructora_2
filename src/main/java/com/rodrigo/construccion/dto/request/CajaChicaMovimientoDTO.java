package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registrar movimientos de caja chica (asignaciones y gastos)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para registrar asignaciones y gastos de caja chica")
public class CajaChicaMovimientoDTO {

    @Schema(description = "ID del movimiento (solo para consultas, no enviar en creación)")
    private Long id;

    @NotNull(message = "El ID del presupuesto es obligatorio")
    @Schema(description = "ID del presupuesto al que pertenece el movimiento", example = "68", required = true)
    @JsonProperty("presupuestoId")
    private Long presupuestoId;

    @NotBlank(message = "El nombre del profesional es obligatorio")
    @Size(max = 100, message = "El nombre del profesional no puede exceder 100 caracteres")
    @Schema(description = "Nombre completo del profesional", example = "Ruben García", required = true)
    @JsonProperty("profesionalNombre")
    private String profesionalNombre;

    @NotBlank(message = "El tipo de profesional es obligatorio")
    @Size(max = 50, message = "El tipo de profesional no puede exceder 50 caracteres")
    @Schema(description = "Tipo o rol del profesional", example = "Oficial", required = true)
    @JsonProperty("profesionalTipo")
    private String profesionalTipo;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    @Pattern(regexp = "ASIGNACION|GASTO", message = "El tipo debe ser ASIGNACION o GASTO")
    @Schema(description = "Tipo de movimiento: ASIGNACION (dinero entregado) o GASTO (dinero gastado)", 
            example = "ASIGNACION", 
            required = true,
            allowableValues = {"ASIGNACION", "GASTO"})
    @JsonProperty("tipo")
    private String tipo;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Monto del movimiento (siempre positivo)", example = "5000.00", required = true)
    @JsonProperty("monto")
    private BigDecimal monto;

    @NotNull(message = "La fecha es obligatoria")
    @Schema(description = "Fecha del movimiento (formato: yyyy-MM-dd)", example = "2025-10-23", required = true)
    @JsonProperty("fecha")
    private LocalDate fecha;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción o concepto del movimiento", example = "Caja chica para compra de materiales")
    @JsonProperty("descripcion")
    private String descripcion;

    @Size(max = 100, message = "El usuario de registro no puede exceder 100 caracteres")
    @Schema(description = "Usuario que registra el movimiento (opcional)", example = "admin@empresa.com")
    @JsonProperty("usuarioRegistro")
    private String usuarioRegistro;
}
