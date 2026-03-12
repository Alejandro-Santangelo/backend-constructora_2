package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de request para crear un pago a cuenta sobre un item de rubro
 */
@Schema(description = "Datos para crear un pago a cuenta sobre un item de rubro del presupuesto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoCuentaRequestDTO {

    @Schema(description = "ID del presupuesto", required = true, example = "123")
    @NotNull(message = "El ID del presupuesto es obligatorio")
    @Positive(message = "El ID del presupuesto debe ser positivo")
    private Long presupuestoId;

    @Schema(description = "ID de la empresa", required = true, example = "1")
    @NotNull(message = "El ID de empresa es obligatorio")
    @Positive(message = "El ID de empresa debe ser positivo")
    private Long empresaId;

    @Schema(description = "Nombre del rubro", required = true, example = "Albañilería")
    @NotBlank(message = "El nombre del rubro es obligatorio")
    @Size(max = 255, message = "El nombre del rubro no puede exceder 255 caracteres")
    private String nombreRubro;

    @Schema(description = "Tipo de item: JORNALES, MATERIALES, GASTOS_GENERALES", required = true, example = "JORNALES")
    @NotBlank(message = "El tipo de item es obligatorio")
    @Pattern(regexp = "JORNALES|MATERIALES|GASTOS_GENERALES", 
             message = "El tipo de item debe ser: JORNALES, MATERIALES o GASTOS_GENERALES")
    private String tipoItem;

    @Schema(description = "Monto del pago a cuenta", required = true, example = "500000.00")
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @DecimalMin(value = "1.00", message = "El monto debe ser al menos 1.00")
    private BigDecimal monto;

    @Schema(description = "Método de pago: EFECTIVO, TRANSFERENCIA, CHEQUE, TARJETA, OTRO", example = "EFECTIVO")
    @Size(max = 50, message = "El método de pago no puede exceder 50 caracteres")
    private String metodoPago;

    @Schema(description = "Observaciones del pago", example = "Pago parcial primera quincena")
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @Schema(description = "Fecha del pago (default: ahora)", example = "2026-03-12T10:30:00")
    private LocalDateTime fechaPago;

    @Schema(description = "Usuario que registra el pago", example = "admin@empresa.com")
    @Size(max = 100, message = "El usuario registro no puede exceder 100 caracteres")
    private String usuarioRegistro;
}
