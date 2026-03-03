package com.rodrigo.construccion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para crear/actualizar un trabajo adicional
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoAdicionalRequestDTO {

    @NotBlank(message = "El nombre del trabajo adicional es obligatorio")
    private String nombre;

    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser mayor a cero")
    private BigDecimal importe;

    private BigDecimal importeJornales;
    private BigDecimal importeMateriales;
    private BigDecimal importeGastosGenerales;
    private BigDecimal importeHonorarios;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoHonorarios;
    private BigDecimal importeMayoresCostos;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoMayoresCostos;

    // ========== HONORARIOS INDIVIDUALES POR CATEGORÍA (SISTEMA NUEVO) ==========
    private BigDecimal honorarioJornales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoHonorarioJornales;
    
    private BigDecimal honorarioMateriales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoHonorarioMateriales;
    
    private BigDecimal honorarioGastosGenerales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoHonorarioGastosGenerales;
    
    private BigDecimal honorarioMayoresCostos;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoHonorarioMayoresCostos;

    // ========== DESCUENTOS SOBRE IMPORTES BASE POR CATEGORÍA ==========
    private BigDecimal descuentoJornales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoDescuentoJornales;
    
    private BigDecimal descuentoMateriales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoDescuentoMateriales;
    
    private BigDecimal descuentoGastosGenerales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoDescuentoGastosGenerales;
    
    private BigDecimal descuentoMayoresCostos;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoDescuentoMayoresCostos;

    // ========== DESCUENTOS SOBRE HONORARIOS POR CATEGORÍA (NUEVOS) ==========
    private BigDecimal descuentoHonorarioJornales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoDescuentoHonorarioJornales;
    
    private BigDecimal descuentoHonorarioMateriales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoDescuentoHonorarioMateriales;
    
    private BigDecimal descuentoHonorarioGastosGenerales;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoDescuentoHonorarioGastosGenerales;
    
    private BigDecimal descuentoHonorarioMayoresCostos;
    /** Valores posibles: "fijo" | "porcentaje" */
    private String tipoDescuentoHonorarioMayoresCostos;

    @NotNull(message = "Los días necesarios son obligatorios")
    @Positive(message = "Los días necesarios deben ser al menos 1")
    private Integer diasNecesarios;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private String descripcion;

    private String observaciones;

    /**
     * Lista de profesionales asignados
     */
    @Valid
    @Builder.Default
    private List<TrabajoAdicionalProfesionalDTO> profesionales = new ArrayList<>();

    /**
     * ID de la obra padre (SIEMPRE obligatorio)
     */
    @NotNull(message = "El ID de la obra es obligatorio")
    private Long obraId;

    /**
     * ID del trabajo extra (opcional)
     * - null: trabajo adicional creado directamente desde la obra
     * - valor: trabajo adicional creado desde un trabajo extra
     */
    private Long trabajoExtraId;

    /**
     * ID del trabajo adicional padre (opcional - NUEVA FUNCIONALIDAD)
     * Permite crear jerarquías anidadas de trabajos adicionales.
     * - null: trabajo adicional raíz (sin padre adicional)
     * - valor: trabajo adicional hijo de otro trabajo adicional
     * 
     * Cuando se especifica, hereda automáticamente del padre:
     * - obraId
     * - empresaId
     * - trabajoExtraId (si el padre lo tiene)
     * 
     * IMPORTANTE: No puede tener valores en trabajoExtraId Y trabajoAdicionalPadreId simultáneamente.
     * Si se especifica trabajoAdicionalPadreId, trabajoExtraId debe ser null.
     */
    private Long trabajoAdicionalPadreId;

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;
}
