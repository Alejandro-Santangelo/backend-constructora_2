package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para describir un profesional necesario en un presupuesto.
 * unidadActiva puede ser: "horas", "dias", "semanas", "meses", "obra_entera"
 */
@Getter
@Setter
public class ProfesionalNecesarioDTO {

    private String tipoProfesional;

    // Campos opcionales de contacto
    private String nombreProfesional;
    private String telefonoProfesional;

    @NotBlank(message = "unidadActiva es obligatoria")
    private String unidadActiva;

    @NotNull
    @PositiveOrZero
    private Double cantidad; // cantidad de la unidad activa

    @PositiveOrZero
    private Double importePorUnidad; // ahora opcional: si es null, el servicio calculará a partir de importeX* según unidadActiva

    @PositiveOrZero
    private Double importeXHora;

    @PositiveOrZero
    private Double importeXDia;

    @PositiveOrZero
    private Double importeXSemana;

    @PositiveOrZero
    private Double importeXMes;

    @PositiveOrZero
    private Double importeXObra;
    
    // Cantidades específicas por tipo de tiempo
    @PositiveOrZero
    private Integer cantidadHoras;
    
    @PositiveOrZero
    private Integer cantidadDias;
    
    @PositiveOrZero
    private Integer cantidadSemanas;
    
    @PositiveOrZero
    private Integer cantidadMeses;
    
    // Importe calculado que se usó al generar el presupuesto (se rellenará en el servicio)
    private Double importeCalculado;
    
    // Nuevos campos para manejo de unidades (jornales vs horas)
    private String tipoUnidad; // 'jornales' o 'horas', por defecto 'jornales'
    
    @PositiveOrZero
    private Double cantidadJornales; // Cantidad de jornales/días en los que se distribuyen las horas
}
