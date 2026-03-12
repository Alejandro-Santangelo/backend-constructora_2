package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para pagos a cuenta sobre items de rubros
 * Incluye información adicional de montos totales, pagado y pendiente
 */
@Schema(description = "Respuesta con detalles de un pago a cuenta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoCuentaResponseDTO {

    @Schema(description = "ID del pago", example = "1")
    private Long id;

    @Schema(description = "ID del presupuesto", example = "123")
    private Long presupuestoId;

    @Schema(description = "ID de la empresa", example = "1")
    private Long empresaId;

    @Schema(description = "Nombre del rubro", example = "Albañilería")
    private String nombreRubro;

    @Schema(description = "Tipo de item", example = "JORNALES")
    private String tipoItem;

    @Schema(description = "Monto de este pago", example = "500000.00")
    private BigDecimal monto;

    @Schema(description = "Método de pago", example = "EFECTIVO")
    private String metodoPago;

    @Schema(description = "Observaciones del pago", example = "Pago parcial primera quincena")
    private String observaciones;

    @Schema(description = "Fecha del pago", example = "2026-03-12")
    private LocalDate fechaPago;

    @Schema(description = "Usuario que registró el pago", example = "admin@empresa.com")
    private String usuarioRegistro;

    @Schema(description = "Fecha y hora de registro", example = "2026-03-12T10:30:00")
    private LocalDateTime fechaRegistro;

    // Campos adicionales para resumen
    @Schema(description = "Monto total presupuestado del item", example = "2000000.00")
    private BigDecimal montoTotalItem;

    @Schema(description = "Total pagado hasta ahora (incluye este pago)", example = "1200000.00")
    private BigDecimal totalPagado;

    @Schema(description = "Saldo pendiente del item", example = "800000.00")
    private BigDecimal saldoPendiente;

    @Schema(description = "Porcentaje pagado del total", example = "60.00")
    private BigDecimal porcentajePagado;
}
