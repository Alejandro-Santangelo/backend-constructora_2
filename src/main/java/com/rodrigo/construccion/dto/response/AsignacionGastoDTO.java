package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que representa una asignación específica de gasto general a una obra
 * Usado dentro de ObraGastoGeneralDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionGastoDTO {
    
    private Long asignacionId;
    private String tipoPago;
    private String concepto;
    private String categoria;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal totalAsignado;
    private BigDecimal totalUtilizado;
    private BigDecimal saldoPendiente;
    private LocalDate fechaPago;
    private String metodoPago;
    private String estado;
    private String observaciones;
}
