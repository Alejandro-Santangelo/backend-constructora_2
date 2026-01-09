package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para profesionales
 * Evita el bucle infinito al no incluir las relaciones bidireccionales
 */
@Schema(description = "Datos de respuesta de un profesional")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalResponseDTO {

    @Schema(description = "ID único del profesional", example = "1")
    private Long id;

    @Schema(description = "Nombre completo del profesional", example = "Juan Carlos Pérez")
    private String nombre;

    @Schema(description = "Tipo de profesional", example = "Arquitecto")
    private String tipoProfesional;

    @Schema(description = "Número de teléfono", example = "+54 11 1234-5678")
    private String telefono;

    @Schema(description = "Dirección de email", example = "juan.perez@email.com")
    private String email;

    @Schema(description = "Especialidad específica", example = "Diseño estructural")
    private String especialidad;

    @Schema(description = "Valor por hora de trabajo", example = "2500.00")
    private BigDecimal valorHoraDefault;

    @Schema(description = "Estado del profesional", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
    private LocalDateTime fechaCreacion;

    // Información adicional sin relaciones circulares
    @Schema(description = "Número de obras asignadas", example = "3")
    private Integer cantidadObrasAsignadas;

    @Schema(description = "Número de honorarios registrados", example = "5")
    private Integer cantidadHonorarios;

    @Schema(description = "Porcentaje de ganancia aplicado", example = "15.00")
    private BigDecimal porcentajeGanancia;

    @Schema(description = "Importe de ganancia calculado", example = "375.00")
    private BigDecimal importeGanancia;

    @Schema(description = "CUIT del profesional", example = "20-12345678-9")
    private String cuit;

    @Schema(description = "Rol personalizado cuando tipoProfesional es 'Otro (personalizado)'", example = "Medio Oficial Albañileria")
    private String rolPersonalizado;

    // Campos de honorarios y duración
    @Schema(description = "Cantidad de horas", example = "8")
    private Integer horas;

    @Schema(description = "Cantidad de días", example = "5")
    private Integer dias;

    @Schema(description = "Cantidad de semanas", example = "2")
    private Integer semanas;

    @Schema(description = "Cantidad de meses", example = "1")
    private Integer meses;

    @Schema(description = "Honorario por hora", example = "15000.00")
    private BigDecimal honorarioHora;

    @Schema(description = "Honorario por día", example = "120000.00")
    private BigDecimal honorarioDia;

    @Schema(description = "Honorario por semana", example = "600000.00")
    private BigDecimal honorarioSemana;

    @Schema(description = "Honorario por mes", example = "2400000.00")
    private BigDecimal honorarioMes;

    /**
     * Campo calculado: honorario preferencial según prioridad:
     * 1. honorarioDia
     * 2. valorHoraDefault
     * 3. honorarioHora
     * Este campo facilita al frontend obtener el valor a usar sin lógica adicional.
     */
    @Schema(description = "Honorario preferencial (calculado según prioridad: honorarioDia > valorHoraDefault > honorarioHora)", example = "120000.00")
    private BigDecimal honorario;
}