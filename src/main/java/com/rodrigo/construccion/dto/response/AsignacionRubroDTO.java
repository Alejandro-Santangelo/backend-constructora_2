package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que representa una asignación específica de un profesional a un rubro en una obra
 * Contiene todos los detalles financieros y operativos de la asignación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionRubroDTO {
    
    // Identificación
    private Long asignacionId;
    
    // Datos del rubro
    private Long rubroId;
    private String rubroNombre;
    
    // Datos de la asignación
    private String tipoAsignacion; // PROFESIONAL o JORNAL
    private BigDecimal importeJornal;
    private Integer cantidadJornales;
    private Integer jornalesUtilizados;
    private Integer jornalesRestantes;
    
    // Financiero
    private BigDecimal totalAsignado;
    private BigDecimal totalUtilizado;
    private BigDecimal saldoPendiente;
    
    // Fechas y estado
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado; // ACTIVO, FINALIZADO, CANCELADO
    
    // Campos adicionales
    private String modalidad; // total, semanal
    private Integer semanasObjetivo;
    private String observaciones;
}
