package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa un profesional con todas sus asignaciones consolidadas
 * Estructura: Profesional → Obras → Asignaciones por rubro
 * Usado para la gestión de pagos agrupada por profesional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalConsolidadoDTO {
    
    // Datos del profesional
    private Long profesionalId;
    private String profesionalNombre;
    private String profesionalTipo;
    private String profesionalDni;
    private String profesionalTelefono;
    private String profesionalEmail;
    
    // Lista de obras donde está asignado
    @Builder.Default
    private List<ObraAsignacionDTO> obras = new ArrayList<>();
    
    // Totales consolidados del profesional (suma de todas las obras)
    private Integer totalObras;
    private Integer totalAsignaciones;
    private BigDecimal totalAsignado;
    private BigDecimal totalUtilizado;
    private BigDecimal saldoPendiente;
    
    /**
     * Calcula los totales consolidados a partir de la lista de obras
     */
    public void calcularTotales() {
        this.totalObras = obras.size();
        this.totalAsignaciones = obras.stream()
            .mapToInt(o -> o.getTotalAsignaciones() != null ? o.getTotalAsignaciones() : 0)
            .sum();
        this.totalAsignado = obras.stream()
            .map(o -> o.getTotalAsignado() != null ? o.getTotalAsignado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalUtilizado = obras.stream()
            .map(o -> o.getTotalUtilizado() != null ? o.getTotalUtilizado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.saldoPendiente = obras.stream()
            .map(o -> o.getSaldoPendiente() != null ? o.getSaldoPendiente() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
