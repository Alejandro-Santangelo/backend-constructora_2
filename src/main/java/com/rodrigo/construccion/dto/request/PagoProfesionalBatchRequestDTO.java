package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO para registrar múltiples pagos parciales por rubro
 * Usado desde el modal de gestión de pagos profesionales
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoProfesionalBatchRequestDTO {

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    /**
     * Map de asignacionId -> importe a pagar
     * Ejemplo: { "88": 50000.00, "90": 75000.00 }
     */
    @NotNull(message = "Los importes por asignación son obligatorios")
    private Map<Long, BigDecimal> importesPorAsignacion;

    /**
     * Tipo de pago: PAGO_PARCIAL, ADELANTO, etc.
     * Por defecto: PAGO_PARCIAL
     */
    private String tipoPago = "PAGO_PARCIAL";

    /**
     * Observaciones adicionales del pago
     */
    private String observaciones;
    
    /**
     * Map de asignacionId -> fecha del pago
     * Permite registrar pagos con fechas personalizadas (ej: pago realizado hace 1 semana)
     * Si no se envía, se usa la fecha actual
     * Ejemplo: { "88": "2026-03-08", "90": "2026-03-15" }
     */
    private Map<Long, LocalDate> fechasPorAsignacion;
}
