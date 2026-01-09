package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para response de pago consolidado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoConsolidadoResponseDTO {

    private Long id;
    private Long presupuestoNoClienteId;
    private Long itemCalculadoraId;
    private Long materialCalculadoraId;
    private Long gastoGeneralCalculadoraId;
    private Long gastoGeneralId;
    private Long empresaId;
    private String tipoPago;
    private String concepto;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal monto;
    private String metodoPago;
    private LocalDate fechaPago;
    private String estado;
    private String observaciones;
    private String motivoAnulacion;
    private String comprobanteUrl;
    private String numeroComprobante;
    private LocalDateTime fechaRegistro;
    private String usuarioRegistro;
}
