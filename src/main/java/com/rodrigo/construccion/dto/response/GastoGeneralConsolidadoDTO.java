package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa un tipo de gasto general con todas sus asignaciones consolidadas
 * Estructura: GastoGeneral → Obras → Asignaciones por gasto
 * Usado para la gestión de pagos agrupada por tipo de gasto general
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoGeneralConsolidadoDTO {
    
    // Datos del gasto general
    private Long gastoId;
    private String gastoNombre;
    private String gastoDescripcion;
    private String categoria;
    private String unidadMedida;
    
    // Lista de obras donde está asignado
    @Builder.Default
    private List<ObraGastoGeneralDTO> obras = new ArrayList<>();
    
    // Totales consolidados del gasto (suma de todas las obras)
    private Integer totalObras;
    private Integer totalAsignaciones;
    private BigDecimal cantidadTotalAsignada;
    private BigDecimal montoTotalAsignado;
    private BigDecimal montoTotalUtilizado;
    private BigDecimal saldoPendiente;
    
    /**
     * Calcula los totales consolidados a partir de la lista de obras
     */
    public void calcularTotales() {
        this.totalObras = obras.size();
        this.totalAsignaciones = obras.stream()
            .mapToInt(o -> o.getTotalAsignaciones() != null ? o.getTotalAsignaciones() : 0)
            .sum();
        this.cantidadTotalAsignada = obras.stream()
            .map(o -> o.getCantidadTotalAsignada() != null ? o.getCantidadTotalAsignada() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.montoTotalAsignado = obras.stream()
            .map(o -> o.getMontoTotalAsignado() != null ? o.getMontoTotalAsignado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.montoTotalUtilizado = obras.stream()
            .map(o -> o.getMontoTotalUtilizado() != null ? o.getMontoTotalUtilizado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.saldoPendiente = obras.stream()
            .map(o -> o.getSaldoPendiente() != null ? o.getSaldoPendiente() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
