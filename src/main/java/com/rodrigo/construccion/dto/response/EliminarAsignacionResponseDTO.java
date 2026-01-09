package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para eliminación de asignación de cobro empresa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EliminarAsignacionResponseDTO {
    
    private String mensaje;
    private BigDecimal montoLiberado;
    private CobroActualizadoDTO cobroActualizado;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CobroActualizadoDTO {
        private Long id;
        private BigDecimal montoTotal;
        private BigDecimal montoAsignado;
        private BigDecimal montoDisponible;
        private String estado;
    }
}
