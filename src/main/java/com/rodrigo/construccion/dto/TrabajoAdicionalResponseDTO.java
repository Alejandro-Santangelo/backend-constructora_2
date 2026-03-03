package com.rodrigo.construccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para respuestas de trabajo adicional
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoAdicionalResponseDTO {

    private Long id;

    private String nombre;

    private BigDecimal importe;

    private BigDecimal importeJornales;
    private BigDecimal importeMateriales;
    private BigDecimal importeGastosGenerales;
    private BigDecimal importeHonorarios;
    private String tipoHonorarios;
    private BigDecimal importeMayoresCostos;
    private String tipoMayoresCostos;

    // ========== HONORARIOS INDIVIDUALES POR CATEGORÍA (SISTEMA NUEVO) ==========
    private BigDecimal honorarioJornales;
    private String tipoHonorarioJornales;
    
    private BigDecimal honorarioMateriales;
    private String tipoHonorarioMateriales;
    
    private BigDecimal honorarioGastosGenerales;
    private String tipoHonorarioGastosGenerales;
    
    private BigDecimal honorarioMayoresCostos;
    private String tipoHonorarioMayoresCostos;

    // ========== DESCUENTOS SOBRE IMPORTES BASE POR CATEGORÍA ==========
    private BigDecimal descuentoJornales;
    private String tipoDescuentoJornales;
    
    private BigDecimal descuentoMateriales;
    private String tipoDescuentoMateriales;
    
    private BigDecimal descuentoGastosGenerales;
    private String tipoDescuentoGastosGenerales;
    
    private BigDecimal descuentoMayoresCostos;
    private String tipoDescuentoMayoresCostos;

    // ========== DESCUENTOS SOBRE HONORARIOS POR CATEGORÍA (NUEVOS) ==========
    private BigDecimal descuentoHonorarioJornales;
    private String tipoDescuentoHonorarioJornales;
    
    private BigDecimal descuentoHonorarioMateriales;
    private String tipoDescuentoHonorarioMateriales;
    
    private BigDecimal descuentoHonorarioGastosGenerales;
    private String tipoDescuentoHonorarioGastosGenerales;
    
    private BigDecimal descuentoHonorarioMayoresCostos;
    private String tipoDescuentoHonorarioMayoresCostos;

    private Integer diasNecesarios;

    private LocalDate fechaInicio;

    private String descripcion;

    private String observaciones;

    private Long obraId;

    private Long trabajoExtraId;

    /**
     * ID del trabajo adicional padre (si este es un trabajo adicional hijo)
     * null si es un trabajo adicional raíz
     */
    private Long trabajoAdicionalPadreId;

    private Long empresaId;

    private String estado;

    private String fechaCreacion;

    private String fechaActualizacion;

    /**
     * Lista de profesionales asignados
     */
    @Builder.Default
    private List<TrabajoAdicionalProfesionalDTO> profesionales = new ArrayList<>();

    /**
     * Lista de trabajos adicionales hijos (si este es un trabajo adicional padre)
     * null o vacía si no tiene hijos
     */
    @Builder.Default
    private List<TrabajoAdicionalResponseDTO> trabajosAdicionalesHijos = new ArrayList<>();
}
