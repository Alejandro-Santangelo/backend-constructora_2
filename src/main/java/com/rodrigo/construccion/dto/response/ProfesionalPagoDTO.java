package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO que representa los datos de pago de un profesional asignado a un rubro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalPagoDTO {
    
    private Long asignacionId; // ID de la asignación
    private Long profesionalId;
    private String profesionalNombre;
    private String profesionalTipo;
    
    private String tipoAsignacion; // PROFESIONAL o JORNAL
    private BigDecimal importeJornal;
    private Integer cantidadJornales;
    private Integer jornalesUtilizados;
    private Integer jornalesRestantes;
    
    private BigDecimal totalAsignado; // importeJornal * cantidadJornales
    private BigDecimal totalUtilizado; // importeJornal * jornalesUtilizados
    private BigDecimal saldoPendiente; // totalAsignado - totalUtilizado
    
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado; // ACTIVO, FINALIZADO, CANCELADO
    private String modalidad; // total, semanal, null
    private Integer semanasObjetivo;
    private String observaciones;
}
