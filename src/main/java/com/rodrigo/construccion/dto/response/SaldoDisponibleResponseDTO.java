package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO de respuesta con el saldo disponible completo
 * Incluye desglose de cobros, asignaciones y retiros
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaldoDisponibleResponseDTO {

    private Long empresaId;
    private BigDecimal totalCobrado;
    private BigDecimal totalPagado;
    private BigDecimal totalRetirado;
    private BigDecimal saldoDisponible;
    private DesgloseFinancieroDTO desglose;
}
