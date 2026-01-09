package com.rodrigo.construccion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HonorarioProfesionalObraResponseDTO {
    // --- Datos del Profesional ---
    private String nombreProfesional;
    private String tipoProfesional;
    private String especialidadProfesional;
    private Boolean activoProfesional;
    private String cuitProfesional;
    private Integer horas;
    private Integer dias;
    private Integer semanas;
    private Integer meses;
    private BigDecimal honorarioHora;
    private BigDecimal honorarioDia;
    private BigDecimal honorarioSemana;
    private BigDecimal honorarioMes;
    private BigDecimal valorHoraDefault = BigDecimal.ZERO;
    private BigDecimal porcentajeGanancia = BigDecimal.ZERO;
    private BigDecimal importeGanancia = BigDecimal.ZERO;

    // --- Datos del Honorario (El Pago) ---
    private Long idHonorario;
    private BigDecimal monto;
    private LocalDate fecha;
    private String observaciones;

    // --- Datos de la Obra y Asignación ---
    private String nombreObra;
    private String rolEnObra;
    private BigDecimal valorHoraAsignado;

}
