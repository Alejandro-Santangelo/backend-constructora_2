package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de request para crear/actualizar adelantos
 */
@Schema(description = "Datos para crear o actualizar un adelanto a un profesional")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdelantoRequestDTO {

    @Schema(description = "ID del profesional-obra (asignación)", required = true, example = "123")
    @NotNull(message = "El ID de profesional-obra es obligatorio")
    @Positive(message = "El ID de profesional-obra debe ser positivo")
    private Long profesionalObraId;

    @Schema(description = "ID de la empresa", required = true, example = "1")
    @NotNull(message = "El ID de empresa es obligatorio")
    @Positive(message = "El ID de empresa debe ser positivo")
    private Long empresaId;

    @Schema(description = "Monto del adelanto", required = true, example = "50000.00")
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @DecimalMin(value = "1.00", message = "El monto debe ser al menos 1.00")
    private BigDecimal monto;

    @Schema(description = "Fecha del adelanto (default: hoy)", example = "2024-03-15")
    private LocalDate fechaPago;

    @Schema(description = "Período del adelanto: 1_SEMANA, 2_SEMANAS, 1_MES, OBRA_COMPLETA", example = "1_SEMANA")
    @Size(max = 50, message = "El período no puede exceder 50 caracteres")
    private String periodoAdelanto;

    @Schema(description = "Motivo/concepto del adelanto", example = "Adelanto por emergencia médica")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    @Schema(description = "Observaciones adicionales", example = "Se descontará en 4 pagos semanales")
    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    @Schema(description = "Método de pago: efectivo, transferencia, cheque", example = "efectivo")
    @Size(max = 50, message = "El método de pago no puede exceder 50 caracteres")
    private String metodoPago;

    @Schema(description = "Número de comprobante/recibo", example = "ADL-2024-001")
    @Size(max = 100, message = "El número de comprobante no puede exceder 100 caracteres")
    private String numeroComprobante;

    @Schema(description = "ID de presupuesto asociado (opcional)", example = "45")
    private Long presupuestoNoClienteId;

    @Schema(description = "Aprobado por (nombre de quien aprueba)", example = "Juan Pérez")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String aprobadoPor;
}
