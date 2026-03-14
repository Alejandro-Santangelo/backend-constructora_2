package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa una obra con sus rubros y profesionales asignados
 * Estructura jerárquica para la gestión consolidada de pagos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObraPagosDTO {
    
    private Long obraId;
    private String obraNombre;
    private String obraEstado;
    private String direccionCompleta;
    
    @Builder.Default
    private List<RubroPagosDTO> rubros = new ArrayList<>();
    
    // Totales consolidados de la obra
    private Integer totalRubros;
    private Integer totalProfesionales;
    private BigDecimal totalAsignado;
    private BigDecimal totalUtilizado;
    private BigDecimal saldoPendiente;
    
    /**
     * Calcula los totales consolidados a partir de la lista de rubros
     */
    public void calcularTotales() {
        this.totalRubros = rubros.size();
        this.totalProfesionales = rubros.stream()
            .mapToInt(r -> r.getTotalProfesionales() != null ? r.getTotalProfesionales() : 0)
            .sum();
        this.totalAsignado = rubros.stream()
            .map(r -> r.getTotalAsignado() != null ? r.getTotalAsignado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalUtilizado = rubros.stream()
            .map(r -> r.getTotalUtilizado() != null ? r.getTotalUtilizado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.saldoPendiente = rubros.stream()
            .map(r -> r.getSaldoPendiente() != null ? r.getSaldoPendiente() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
