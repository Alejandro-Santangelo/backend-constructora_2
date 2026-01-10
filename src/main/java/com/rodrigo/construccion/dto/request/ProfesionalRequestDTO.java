
package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para crear/actualizar profesionales
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Datos para crear un nuevo profesional")
public class ProfesionalRequestDTO {

    @Schema(description = "ID del profesional (se genera automáticamente)", example = "0", hidden = true)
    private Long id;
    
    @Schema(description = "ID de la empresa", example = "123", required = true)
    private Long empresaId;

    @Schema(description = "Costo del jornal (Diario)", example = "5000.00", required = true)
    private BigDecimal costoJornal;

    // --- CAMPOS FALTANTES ---
    @Schema(description = "Cantidad de días (opcional, puede ser null)", example = "5", nullable = true)
    private Integer dias;

    @Schema(description = "Cantidad de horas (opcional, puede ser null)", example = "40", nullable = true)
    private Integer horas;

    @Schema(description = "Cantidad de meses (opcional, puede ser null)", example = "2", nullable = true)
    private Integer meses;

    @Schema(description = "Cantidad de semanas (opcional, puede ser null)", example = "3", nullable = true)
    private Integer semanas;

    @Schema(description = "Honorario por día (opcional, puede ser null)", example = "10000.00", nullable = true)
    private BigDecimal honorarioDia;

    @Schema(description = "Honorario por hora (opcional, puede ser null)", example = "1200.00", nullable = true)
    private BigDecimal honorarioHora;

    @Schema(description = "Honorario por mes (opcional, puede ser null)", example = "200000.00", nullable = true)
    private BigDecimal honorarioMes;

    @Schema(description = "Honorario por semana (opcional, puede ser null)", example = "40000.00", nullable = true)
    private BigDecimal honorarioSemana;

    @Schema(description = "Importe de ganancia (opcional, puede ser null)", example = "5000.00", nullable = true)
    private BigDecimal importeGanancia;

    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Schema(description = "Nombre completo del profesional (opcional, se genera automáticamente si no se envía)", example = "Juan Carlos Pérez")
    private String nombre;

    @NotBlank(message = "El tipo de profesional es obligatorio")
    @Size(max = 50, message = "El tipo de profesional no puede exceder 50 caracteres")
    @Schema(description = "Tipo de profesional", example = "Arquitecto", required = true, allowableValues = {
            "Arquitecto", "Ingeniero", "Maestro Mayor", "Albañil", "Electricista", "Plomero", "Pintor", "Carpintero" })
    private String tipoProfesional;

    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    @Schema(description = "Número de teléfono", example = "+54 11 1234-5678")
    private String telefono;

    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Schema(description = "Dirección de email", example = "juan.perez@email.com")
    private String email;

    @Size(max = 100, message = "La especialidad no puede exceder 100 caracteres")
    @Schema(description = "Especialidad específica", example = "Diseño estructural")
    private String especialidad;

    @PositiveOrZero(message = "El valor por hora debe ser mayor o igual a cero")
    @Schema(description = "Valor por hora de trabajo", example = "2500.00")
    private BigDecimal valorHoraDefault;

    @Schema(description = "Estado del profesional", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de creación (se genera automáticamente)", hidden = true)
    private LocalDateTime fechaCreacion;

    @Schema(description = "Porcentaje de ganancia aplicado (opcional, puede ser null)", example = "15.00", nullable = true)
    private BigDecimal porcentajeGanancia;

    @Schema(description = "CUIT del profesional (opcional, puede ser null)", example = "20-12345678-9", nullable = true)
    private String cuit;

    @Size(max = 100, message = "El rol personalizado no puede exceder 100 caracteres")
    @Schema(description = "Rol personalizado cuando tipoProfesional es 'Otro (personalizado)' (opcional, puede ser null)", example = "Medio Oficial Albañileria", nullable = true)
    private String rolPersonalizado;

}