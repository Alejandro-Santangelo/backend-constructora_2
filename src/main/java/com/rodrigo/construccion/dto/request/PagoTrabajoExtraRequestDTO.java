package com.rodrigo.construccion.dto.request;

import com.rodrigo.construccion.enums.MetodoPago;
import com.rodrigo.construccion.enums.TipoPagoTrabajoExtra;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para crear/actualizar pagos de trabajos extra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoTrabajoExtraRequestDTO {

    @NotNull(message = "El ID del trabajo extra es obligatorio")
    private Long trabajoExtraId;

    @NotNull(message = "El ID de la obra es obligatorio")
    private Long obraId;

    private Long presupuestoNoClienteId;

    @NotNull(message = "El tipo de pago es obligatorio")
    private TipoPagoTrabajoExtra tipoPago;

    // Referencias específicas (solo una debe estar llena según el tipo de pago)
    private Long trabajoExtroProfesionalId;
    private Long trabajoExtraTareaId;

    @NotNull(message = "El concepto es obligatorio")
    private String concepto;

    @NotNull(message = "El monto base es obligatorio")
    @Positive(message = "El monto base debe ser mayor a cero")
    private BigDecimal montoBase;

    private BigDecimal descuentos;
    private BigDecimal bonificaciones;

    @NotNull(message = "El monto final es obligatorio")
    @Positive(message = "El monto final debe ser mayor a cero")
    private BigDecimal montoFinal;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate fechaPago;

    private LocalDate fechaEmision;
    private MetodoPago metodoPago;
    private String numeroComprobante;
    private String comprobanteUrl;
    private String observaciones;
}
