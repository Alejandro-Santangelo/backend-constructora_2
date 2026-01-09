package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para configuración de honorarios en trabajos extra.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HonorariosConfigDTO {
    
    // General
    private Boolean aplicarATodos;
    private BigDecimal valorGeneral;
    private String tipoGeneral; // PORCENTAJE / VALOR_FIJO
    
    // Por categoría - Jornales
    private Boolean jornalesActivo;
    private BigDecimal jornalesValor;
    private String jornalesTipo;
    
    // Por categoría - Materiales
    private Boolean materialesActivo;
    private BigDecimal materialesValor;
    private String materialesTipo;
    
    // Por categoría - Profesionales
    private Boolean profesionalesActivo;
    private BigDecimal profesionalesValor;
    private String profesionalesTipo;
    
    // Por categoría - Otros Costos
    private Boolean otrosCostosActivo;
    private BigDecimal otrosCostosValor;
    private String otrosCostosTipo;
    
    // Configuración Presupuesto
    private Boolean configuracionPresupuestoActivo;
    private BigDecimal configuracionPresupuestoValor;
    private String configuracionPresupuestoTipo;
}
