package com.rodrigo.construccion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostoResponseDTO {
    public Long id_costo;
    public Integer anio;
    public LocalDate fecha;
    public LocalDate fecha_aprobacion;
    public Boolean imputable;
    public BigDecimal monto;
    public Integer semana;
    public LocalDate fecha_actualizacion;
    public LocalDate fecha_creacion;
    public Long id_obra;
    public String estado;
    public String tipo_costo;
    public String categoria;
    public String concepto;
    public String comentarios;
    public String descripcion;
    public String motivo_rechazo;
}
