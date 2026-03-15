package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa una obra con las asignaciones de un profesional específico
 * Usado dentro de ProfesionalConsolidadoDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObraAsignacionDTO {
    
    // Datos de la obra
    private Long obraId;
    private String obraNombre;
    private String obraEstado;
    private String direccionCompleta;
    
    // Lista de asignaciones del profesional en esta obra (agrupadas por rubro)
    @Builder.Default
    private List<AsignacionRubroDTO> asignaciones = new ArrayList<>();
    
    // Totales de este profesional en esta obra
    private Integer totalAsignaciones;
    private BigDecimal totalAsignado;
    private BigDecimal totalUtilizado;
    private BigDecimal totalPagado;  // Suma real de pagos efectuados
    private BigDecimal saldoPendiente;
    
    /**
     * Calcula los totales a partir de la lista de asignaciones
     */
    public void calcularTotales() {
        this.totalAsignaciones = asignaciones.size();
        this.totalAsignado = asignaciones.stream()
            .map(a -> a.getTotalAsignado() != null ? a.getTotalAsignado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalUtilizado = asignaciones.stream()
            .map(a -> a.getTotalUtilizado() != null ? a.getTotalUtilizado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalPagado = asignaciones.stream()
            .map(a -> a.getTotalPagado() != null ? a.getTotalPagado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.saldoPendiente = this.totalAsignado.subtract(this.totalPagado);
    }
}
