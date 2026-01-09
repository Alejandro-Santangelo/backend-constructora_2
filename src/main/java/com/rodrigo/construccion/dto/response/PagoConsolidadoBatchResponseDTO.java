package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para response de batch de pagos consolidados.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoConsolidadoBatchResponseDTO {

    private String mensaje;
    private Integer cantidadRegistrados;
    private BigDecimal totalMonto;
    private List<PagoConsolidadoResponseDTO> pagos;
}
