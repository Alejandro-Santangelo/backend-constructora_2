package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para crear o actualizar un jornal diario de profesional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalJornalDiarioRequestDTO {

    /**
     * ID del profesional que trabajó
     */
    @NotNull(message = "El ID del profesional es obligatorio")
    @Positive(message = "El ID del profesional debe ser positivo")
    private Long profesionalId;

    /**
     * ID de la obra en la que trabajó
     */
    @NotNull(message = "El ID de la obra es obligatorio")
    @Positive(message = "El ID de la obra debe ser positivo")
    private Long obraId;

    /**
     * ID del rubro del presupuesto al que se asigna el jornal
     * 
     * Este ID corresponde a honorarios_por_rubro.id
     * El presupuesto vinculado a la obra define los rubros disponibles.
     * Ejemplos: "Albañilería", "Plomería", "Electricidad", etc.
     */
    @NotNull(message = "El ID del rubro es obligatorio")
    @Positive(message = "El ID del rubro debe ser positivo")
    private Long rubroId;

    /**
     * Fecha del trabajo
     */
    @NotNull(message = "La fecha es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    /**
     * Horas trabajadas expresadas como decimal
     * 
     * Valores válidos:
     * - 1.0 = día completo
     * - 0.5 = medio día
     * - 0.25 = cuarto de día
     * - Mínimo: 0.01
     * - Máximo: 1.5
     */
    @NotNull(message = "Las horas trabajadas son obligatorias")
    @DecimalMin(value = "0.01", message = "Las horas trabajadas deben ser mayor a 0")
    @DecimalMax(value = "1.5", message = "Las horas trabajadas no pueden exceder 1.5")
    @Digits(integer = 3, fraction = 2, message = "Las horas trabajadas deben tener máximo 2 decimales")
    private BigDecimal horasTrabajadasDecimal;

    /**
     * Tarifa diaria (opcional - si no se especifica, se toma del profesional)
     * 
     * Si se especifica, esta tarifa se usará en lugar de la tarifa configurada
     * en el maestro del profesional, permitiendo tarifas personalizadas por obra.
     */
    @DecimalMin(value = "0.00", message = "La tarifa diaria debe ser mayor o igual a 0")
    @Digits(integer = 10, fraction = 2, message = "La tarifa diaria debe tener máximo 2 decimales")
    private BigDecimal tarifaDiaria;

    /**
     * Observaciones sobre el trabajo realizado (opcional)
     */
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    /**
     * ID de la empresa (opcional - se toma del contexto si no se especifica)
     */
    private Long empresaId;
}
