package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO que representa una asignación específica de material a una obra
 * Usado dentro de ObraMaterialDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionMaterialDTO {
    
    private Long asignacionId;
    private String descripcion;
    private String unidadMedida;
    private Boolean esGlobal;
    private BigDecimal cantidadAsignada;
    private BigDecimal cantidadUtilizada;
    private BigDecimal cantidadPendiente;
    private BigDecimal precioUnitario;
    private BigDecimal totalAsignado;
    private BigDecimal totalUtilizado;
    private BigDecimal saldoPendiente;
    private LocalDateTime fechaAsignacion;
    private Integer semana;
    private String observaciones;
}
