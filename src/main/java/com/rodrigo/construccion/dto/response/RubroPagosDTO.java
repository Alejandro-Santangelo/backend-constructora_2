package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa un rubro con todos sus profesionales asignados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubroPagosDTO {
    
    private Long rubroId;
    private String rubroNombre;
    
    @Builder.Default
    private List<ProfesionalPagoDTO> profesionales = new ArrayList<>();
    
    // Totales consolidados del rubro
    private Integer totalProfesionales;
    private BigDecimal totalAsignado;
    private BigDecimal totalUtilizado;
    private BigDecimal saldoPendiente;
    
    /**
     * Calcula los totales consolidados a partir de la lista de profesionales
     */
    public void calcularTotales() {
        this.totalProfesionales = profesionales.size();
        this.totalAsignado = profesionales.stream()
            .map(p -> p.getTotalAsignado() != null ? p.getTotalAsignado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalUtilizado = profesionales.stream()
            .map(p -> p.getTotalUtilizado() != null ? p.getTotalUtilizado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.saldoPendiente = profesionales.stream()
            .map(p -> p.getSaldoPendiente() != null ? p.getSaldoPendiente() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
