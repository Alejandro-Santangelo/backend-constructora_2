package com.rodrigo.construccion.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoPorRubroDTO {

    private Long id;
    private String nombreRubro;
    private Boolean activo;
    private String tipo;
    private BigDecimal valor;

    // Profesionales
    private Boolean profesionalesActivo;
    private String profesionalesTipo;
    private BigDecimal profesionalesValor;

    // Materiales
    private Boolean materialesActivo;
    private String materialesTipo;
    private BigDecimal materialesValor;

    // Otros Costos
    private Boolean otrosCostosActivo;
    private String otrosCostosTipo;
    private BigDecimal otrosCostosValor;

    // Honorarios
    private Boolean honorariosActivo;
    private String honorariosTipo;
    private BigDecimal honorariosValor;

    // Mayores Costos
    private Boolean mayoresCostosActivo;
    private String mayoresCostosTipo;
    private BigDecimal mayoresCostosValor;
}
