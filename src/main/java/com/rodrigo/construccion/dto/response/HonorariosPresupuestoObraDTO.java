package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para retornar información de honorarios del último presupuesto vinculado a una obra.
 * Contiene solo los datos relevantes de honorarios, optimizado para el frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HonorariosPresupuestoObraDTO {
    
    // Identificación de la obra
    private Long obraId;
    private String obraNombre;
    private String obraDireccion;
    
    // Identificación del presupuesto
    private Long presupuestoId;
    private Long numeroPresupuesto;
    private Integer numeroVersion;
    private String estadoPresupuesto;
    private LocalDate fechaEmision;
    
    // Totales principales
    private BigDecimal totalPresupuesto;        // Total base sin honorarios
    private BigDecimal totalHonorarios;         // Total de honorarios calculados
    private BigDecimal totalFinal;              // Total con honorarios (totalPresupuesto + totalHonorarios)
    
    // Configuración general de honorarios
    private Boolean honorariosAplicarATodos;
    private BigDecimal honorariosValorGeneral;
    private String honorariosTipoGeneral;       // "porcentaje" o "fijo"
    
    // Honorarios de dirección de obra
    private Double honorarioDireccionValorFijo;
    private Double honorarioDireccionPorcentaje;
    private Double honorarioDireccionImporte;
    
    // Honorarios por categoría - Profesionales
    private Boolean honorariosProfesionalesActivo;
    private String honorariosProfesionalesTipo;
    private BigDecimal honorariosProfesionalesValor;
    
    // Honorarios por categoría - Materiales
    private Boolean honorariosMaterialesActivo;
    private String honorariosMaterialesTipo;
    private BigDecimal honorariosMaterialesValor;
    
    // Honorarios por categoría - Jornales
    private Boolean honorariosJornalesActivo;
    private String honorariosJornalesTipo;
    private BigDecimal honorariosJornalesValor;
    
    // Honorarios por categoría - Otros Costos
    private Boolean honorariosOtrosCostosActivo;
    private String honorariosOtrosCostosTipo;
    private BigDecimal honorariosOtrosCostosValor;
    
    // Honorarios por categoría - Configuración Presupuesto
    private Boolean honorariosConfiguracionPresupuestoActivo;
    private String honorariosConfiguracionPresupuestoTipo;
    private BigDecimal honorariosConfiguracionPresupuestoValor;
}
