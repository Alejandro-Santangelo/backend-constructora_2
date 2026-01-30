package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasPedidoPagoDTO {

    private Long totalPedidos;
    private Long pendientesAprobacion;
    private Long pendientesAutorizacion;
    private Long pendientesPago;
    private Long pagados;
    private Long rechazados;
    private Long cancelados;

    private Double importeTotalPendientes;
    private Double importeTotalAprobados;
    private Double importeTotalAutorizados;
    private Double importeTotalPagados;

    private LocalDate fechaConsulta;
}
