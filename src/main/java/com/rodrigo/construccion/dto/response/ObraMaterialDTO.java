package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa una obra con sus asignaciones de materiales
 * Usado dentro de MaterialConsolidadoDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObraMaterialDTO {
    
    // Datos de la obra
    private Long obraId;
    private String obraNombre;
    private String obraEstado;
    private String direccionCompleta;
    
    // Lista de asignaciones de este material en esta obra
    @Builder.Default
    private List<AsignacionMaterialDTO> asignaciones = new ArrayList<>();
    
    // Totales de esta obra
    private Integer totalAsignaciones;
    private BigDecimal cantidadTotalAsignada;
    private BigDecimal cantidadTotalUtilizada;
    private BigDecimal cantidadPendiente;
    private BigDecimal montoTotalAsignado;
    private BigDecimal montoTotalUtilizado;
    private BigDecimal saldoPendiente;
    
    /**
     * Calcula los totales de esta obra a partir de sus asignaciones
     */
    public void calcularTotales() {
        this.totalAsignaciones = asignaciones.size();
        this.cantidadTotalAsignada = asignaciones.stream()
            .map(a -> a.getCantidadAsignada() != null ? a.getCantidadAsignada() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.cantidadTotalUtilizada = asignaciones.stream()
            .map(a -> a.getCantidadUtilizada() != null ? a.getCantidadUtilizada() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.cantidadPendiente = this.cantidadTotalAsignada.subtract(this.cantidadTotalUtilizada);
        this.montoTotalAsignado = asignaciones.stream()
            .map(a -> a.getTotalAsignado() != null ? a.getTotalAsignado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.montoTotalUtilizado = asignaciones.stream()
            .map(a -> a.getTotalUtilizado() != null ? a.getTotalUtilizado() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.saldoPendiente = this.montoTotalAsignado.subtract(this.montoTotalUtilizado);
    }
}
