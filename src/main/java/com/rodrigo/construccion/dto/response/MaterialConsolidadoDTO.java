package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa un material con todas sus asignaciones consolidadas
 * Estructura: Material → Obras → Asignaciones por material
 * Usado para la gestión de pagos agrupada por material
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialConsolidadoDTO {
    
    // Datos del material
    private Long materialId;
    private String materialNombre;
    private String materialDescripcion;
    private String unidadMedida;
    private BigDecimal precioReferencia;
    
    // Lista de obras donde está asignado
    @Builder.Default
    private List<ObraMaterialDTO> obras = new ArrayList<>();
    
    // Totales consolidados del material (suma de todas las obras)
    private Integer totalObras;
    private Integer totalAsignaciones;
    private BigDecimal cantidadTotalAsignada;
    private BigDecimal cantidadTotalUtilizada;
    private BigDecimal cantidadPendiente;
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
        this.cantidadTotalUtilizada = obras.stream()
            .map(o -> o.getCantidadTotalUtilizada() != null ? o.getCantidadTotalUtilizada() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.cantidadPendiente = obras.stream()
            .map(o -> o.getCantidadPendiente() != null ? o.getCantidadPendiente() : BigDecimal.ZERO)
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
