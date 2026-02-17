package com.rodrigo.construccion.dto.response;

import com.rodrigo.construccion.enums.TipoEntidadFinanciera;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Estadísticas financieras de una entidad (cobros vs gastos).
 *
 * Para cada entidad se calcula:
 * - totalCobrado: suma de cobros_entidad (para OBRA_INDEPENDIENTE y TRABAJO_ADICIONAL)
 *                 o cobros del sistema existente (para OBRA_PRINCIPAL y TRABAJO_EXTRA)
 * - totalGastos:  suma de gastos según el tipo de entidad
 * - saldo:        totalCobrado - totalGastos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasEntidadResponseDTO {

    private Long entidadFinancieraId;
    private TipoEntidadFinanciera tipoEntidad;
    private Long entidadId;
    private String nombreDisplay;

    private BigDecimal totalCobrado;
    private BigDecimal totalGastos;
    private BigDecimal saldo;

    /** Presupuesto original aprobado (cuando aplica). */
    private BigDecimal presupuestoAprobado;
}
