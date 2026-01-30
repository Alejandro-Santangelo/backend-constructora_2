package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPagoResponseDTO {
    private Long id;
    private String numeroPedido;
    private LocalDate fechaPedido;
    private LocalDate fechaVencimiento;
    private LocalDateTime fechaAprobacion;
    private LocalDateTime fechaAutorizacion;
    private LocalDateTime fechaPago;
    private Double importe;
    private String concepto;
    private String estado;
    private String tipoPago;
    private String numeroFactura;
    private String numeroComprobante;
    private String observaciones;
    private String motivoRechazo;
    private Long usuarioAprobadorId;
    private Long usuarioAutorizadorId;
    private Long usuarioPagadorId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    private ProveedorPedidoPagoResponseDTO proveedor;
    private ObraSimpleDTO obra;
    private EmpresaResponseDTO empresa;
}
