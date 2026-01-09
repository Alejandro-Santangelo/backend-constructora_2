package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para crear o actualizar un jornal de trabajo.")
public class JornalRequestDTO {

    @NotNull(message = "La fecha es obligatoria")
    @Schema(description = "Fecha del jornal de trabajo", example = "2024-10-21", required = true)
    private LocalDate fecha;

    @NotNull(message = "Las horas trabajadas son obligatorias")
    @Positive(message = "Las horas trabajadas deben ser un valor positivo")
    @Schema(description = "Cantidad de horas trabajadas en el día", example = "8.5", required = true)
    private BigDecimal horasTrabajadas;

    @NotNull(message = "El valor por hora es obligatorio")
    @Positive(message = "El valor por hora debe ser un valor positivo")
    @Schema(description = "Valor por hora para este jornal específico", example = "2500.00", required = true)
    private BigDecimal valorHora;

    @Schema(description = "Observaciones o detalles sobre el trabajo realizado", example = "Finalización de revoque fino en pared norte.")
    private String observaciones;

    @NotNull(message = "El ID de la asignación (profesional-obra) es obligatorio")
    @Schema(description = "ID de la asignación a la que pertenece este jornal", example = "5", required = true)
    private Long asignacionId;

    @Schema(description = "Indica si este jornal debe incluirse en el cálculo automático de días hábiles", 
            example = "false",
            defaultValue = "false")
    private Boolean incluirEnCalculoDias = false;
}
