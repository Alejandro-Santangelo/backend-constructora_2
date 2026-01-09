package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para request de pago consolidado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoConsolidadoRequestDTO {

    @NotNull(message = "El presupuestoNoClienteId es obligatorio")
    private Long presupuestoNoClienteId;

    private Long itemCalculadoraId; // Opcional

    private Long materialCalculadoraId; // Opcional, solo para MATERIALES

    private Long gastoGeneralCalculadoraId; // Opcional, solo para GASTOS_GENERALES

    private Long gastoGeneralId; // Opcional, solo para GASTOS_GENERALES (ID del gasto en presupuesto)

    @NotNull(message = "El tipoPago es obligatorio")
    private String tipoPago; // MATERIALES, GASTOS_GENERALES, OTROS_COSTOS

    @NotNull(message = "El concepto es obligatorio")
    private String concepto;

    private BigDecimal cantidad;

    private BigDecimal precioUnitario;

    @NotNull(message = "El monto es obligatorio")
    // @Positive removido: permite null/0, validación manual en servicio
    private BigDecimal monto;

    @NotNull(message = "El metodoPago es obligatorio")
    private String metodoPago; // EFECTIVO, TRANSFERENCIA, CHEQUE, DEBITO, CREDITO

    @NotNull(message = "La fechaPago es obligatoria")
    private LocalDate fechaPago;

    @NotNull(message = "El estado es obligatorio")
    private String estado; // PAGADO, PENDIENTE, ANULADO

    private String observaciones;

    private String numeroComprobante;

    private String comprobanteUrl;

    private Long empresaId; // Multi-tenant
}
