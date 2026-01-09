package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO Response para asistencia de obra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de asistencia")
public class AsistenciaObraResponse {

    @Schema(description = "ID de la asistencia", example = "1")
    private Long id;

    @Schema(description = "ID de la asignación profesional-obra", example = "1")
    private Long profesionalObraId;

    @Schema(description = "Nombre del profesional", example = "Juan Pérez")
    private String nombreProfesional;

    @Schema(description = "Dirección completa de la obra", example = "Av. Corrientes 1234 Piso 5 Depto A")
    private String direccionObra;

    @Schema(description = "Fecha de la asistencia", example = "2025-10-22")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @Schema(description = "Hora de entrada", example = "08:00:00")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaEntrada;

    @Schema(description = "Latitud de entrada", example = "-34.603722")
    private Double latitudEntrada;

    @Schema(description = "Longitud de entrada", example = "-58.381592")
    private Double longitudEntrada;

    @Schema(description = "Hora de salida", example = "17:30:00")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaSalida;

    @Schema(description = "Latitud de salida", example = "-34.603722")
    private Double latitudSalida;

    @Schema(description = "Longitud de salida", example = "-58.381592")
    private Double longitudSalida;

    @Schema(description = "Horas trabajadas en formato decimal", example = "9.5")
    private BigDecimal horasTrabajadas;
}
