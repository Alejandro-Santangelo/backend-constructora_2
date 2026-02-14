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

    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;
}
