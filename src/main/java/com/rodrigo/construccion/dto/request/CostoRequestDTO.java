package com.rodrigo.construccion.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostoRequestDTO {
    private Integer anio;
    private LocalDate fecha;
    private LocalDate fechaAprobacion;
    private Boolean imputable;
    private BigDecimal monto;
    private Integer semana;
    private LocalDate fechaActualizacion; // nulleable en POST, automática en PUT
    private LocalDate fechaCreacion; // nulleable en POST, automática en backend
    private Long idObra;
    private String estado;
    private String tipoCosto;
    private String categoria;
    private String concepto;
    private String comentarios;
    private String descripcion;
    private String motivoRechazo;
}
