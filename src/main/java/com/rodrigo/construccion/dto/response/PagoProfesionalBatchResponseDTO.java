package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para batch de pagos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoProfesionalBatchResponseDTO {

    private Integer totalPagosCreados;
    private BigDecimal montoTotalPagado;
    private List<PagoProfesionalObraResponseDTO> pagosCreados;
    private String mensaje;
}
