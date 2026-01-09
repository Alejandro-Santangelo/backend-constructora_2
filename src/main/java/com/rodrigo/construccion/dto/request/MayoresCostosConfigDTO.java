package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para configuración de mayores costos en trabajos extra.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MayoresCostosConfigDTO {
    
    // General
    private Boolean aplicarValorGeneral;
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
    
    // Por categoría - Honorarios
    private Boolean honorariosActivo;
    private BigDecimal honorariosValor;
    private String honorariosTipo;
    
    // Configuración Presupuesto
    private Boolean configuracionPresupuestoActivo;
    private BigDecimal configuracionPresupuestoValor;
    private String configuracionPresupuestoTipo;
    
    // Importado
    private Boolean generalImportado;
    private String rubroImportado;
    private String nombreRubroImportado;
    private String explicacion;
}
