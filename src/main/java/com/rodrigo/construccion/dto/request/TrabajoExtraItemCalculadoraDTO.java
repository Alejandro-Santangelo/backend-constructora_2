package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para crear/actualizar item calculadora en trabajos extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajoExtraItemCalculadoraDTO {
    
    private Long id;
    private String tipoProfesional;
    private String descripcion;
    private String observaciones;
    
    // Modo de cálculo
    private Boolean esModoManual;
    
    // Jornales (modo automático)
    private BigDecimal cantidadJornales;
    private BigDecimal importeJornal;
    private BigDecimal subtotalManoObra;
    
    // Materiales
    private BigDecimal materiales;
    private BigDecimal subtotalMateriales;
    
    // Total manual
    private BigDecimal totalManual;
    private String descripcionTotalManual;
    private String observacionesTotalManual;
    
    // Total calculado
    private BigDecimal total;
    
    // Control de días hábiles
    private Boolean incluirEnCalculoDias;
    private Boolean trabajaEnParalelo;
    
    // Indicador de rubro vacío (sin jornales, materiales ni profesionales)
    private Boolean esRubroVacio;
    
    // Gastos generales
    private Boolean esGastoGeneral;
    private BigDecimal subtotalGastosGenerales;
    private String descripcionGastosGenerales;
    private String observacionesGastosGenerales;
    
    // Descripciones por categoría
    private String descripcionProfesionales;
    private String observacionesProfesionales;
    private String descripcionMateriales;
    private String observacionesMateriales;
    
    // Relaciones desglosadas
    private List<TrabajoExtraProfesionalCalculadoraDTO> profesionales;
    private List<TrabajoExtraMaterialCalculadoraDTO> materialesLista;
    private List<TrabajoExtraJornalCalculadoraDTO> jornales;
    private List<TrabajoExtraGastoGeneralDTO> gastosGenerales;
}
