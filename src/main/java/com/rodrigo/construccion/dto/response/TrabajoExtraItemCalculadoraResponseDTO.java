package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO Response para item calculadora de trabajo extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraItemCalculadoraResponseDTO {
    
    private Long id;
    private Long trabajoExtraId;
    private Long empresaId;
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
    private List<TrabajoExtraProfesionalCalculadoraResponseDTO> profesionales;
    private List<TrabajoExtraMaterialCalculadoraResponseDTO> materialesLista;
    private List<TrabajoExtraJornalCalculadoraResponseDTO> jornales;
    private List<TrabajoExtraGastoGeneralResponseDTO> gastosGenerales;
    
    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
