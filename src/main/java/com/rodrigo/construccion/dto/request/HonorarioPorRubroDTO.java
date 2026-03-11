package com.rodrigo.construccion.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HonorarioPorRubroDTO {

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
}
