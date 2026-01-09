package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que representa un jornal de trabajo con información detallada del profesional y la obra.")
public class ProfesionalJornalResponseDTO {

    // --- Atributos del Profesional ---
    @Schema(description = "Nombre completo del profesional", example = "Carlos Bianchi")
    private String nombreProfesional;

    @Schema(description = "Tipo de profesional", example = "Plomero")
    private String tipoProfesional;

    @Schema(description = "Especialidad principal del profesional", example = "Instalaciones Sanitarias")
    private String especialidad;

    @Schema(description = "CUIT del profesional", example = "20-12345678-9")
    private String cuitProfesional;

    @Schema(description = "Email de contacto del profesional", example = "carlos.bianchi@constructora.com")
    private String emailProfesional;

    @Schema(description = "Teléfono de contacto del profesional", example = "+54 11 5555-1234")
    private String telefonoProfesional;

    @Schema(description = "Honorario por hora del profesional (si aplica)", example = "2500.00")
    private BigDecimal honorarioHora;

    @Schema(description = "Honorario por día del profesional (si aplica)", example = "20000.00")
    private BigDecimal honorarioDia;

    @Schema(description = "Honorario por semana del profesional (si aplica)", example = "100000.00")
    private BigDecimal honorarioSemana;

    @Schema(description = "Honorario por mes del profesional (si aplica)", example = "400000.00")
    private BigDecimal honorarioMes;

    @Schema(description = "Valor por hora por defecto del profesional", example = "2200.00")
    private BigDecimal valorHoraDefault;

    @Schema(description = "Porcentaje de ganancia configurado para el profesional", example = "15.00")
    private BigDecimal porcentajeGanancia;

    @Schema(description = "Importe de ganancia calculado para el profesional", example = "330.00")
    private BigDecimal importeGanancia;

    // --- Atributos de la Obra y Asignación ---
    @Schema(description = "Nombre de la obra donde se realizó el jornal", example = "Edificio Central")
    private String nombreObra;

    @Schema(description = "Rol específico del profesional en esta obra", example = "Plomero Principal")
    private String rolEnObra;

    @Schema(description = "Valor por hora pactado para esta asignación específica", example = "2450.75")
    private BigDecimal valorHoraAsignado;

    // --- Atributos del Jornal ---
    @Schema(description = "ID único del jornal", example = "101")
    private Long idJornal;

    @Schema(description = "Fecha del jornal de trabajo", example = "2024-10-20")
    private LocalDate fecha;

    @Schema(description = "Horas trabajadas en el jornal", example = "8.00")
    private BigDecimal horasTrabajadas;

    @Schema(description = "Valor por hora aplicado en este jornal específico", example = "2500.00")
    private BigDecimal valorHora;

    @Schema(description = "Observaciones sobre el trabajo realizado", example = "Instalación de cañerías en planta baja.")
    private String observaciones;
}
