package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO para la creación y actualización de pedidos de pago
 * Solo contiene los campos que el cliente puede enviar
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPagoRequestDTO {
    @Schema(description = "Número del pedido (opcional, puede ser generado automáticamente)", example = "PP-2026-001")
    private String numeroPedido;

    @NotNull(message = "La fecha del pedido es obligatoria")
    @Schema(description = "Fecha del pedido", example = "2026-01-29")
    private LocalDate fechaPedido;

    @Schema(description = "Fecha de vencimiento del pedido", example = "2026-02-28")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser mayor a cero")
    @Schema(description = "Importe del pedido", example = "15000.50")
    private Double importe;

    @Schema(description = "Concepto o descripción del pedido", example = "Pago por materiales de construcción")
    private String concepto;

    @Schema(description = "Tipo de pago", example = "TRANSFERENCIA", allowableValues = {"TRANSFERENCIA", "CHEQUE", "EFECTIVO", "TARJETA"})
    private String tipoPago;

    @Schema(description = "Número de factura asociada", example = "F-2026-001")
    private String numeroFactura;

    @Schema(description = "Número de comprobante", example = "C-2026-001")
    private String numeroComprobante;

    @Schema(description = "Observaciones adicionales", example = "Pago urgente")
    private String observaciones;

    @NotNull(message = "El ID del proveedor es obligatorio")
    @Schema(description = "ID del proveedor al que se le realizará el pago", example = "1")
    private Long idProveedor;

    @Schema(description = "ID de la obra asociada (opcional)", example = "5")
    private Long idObra;
}
