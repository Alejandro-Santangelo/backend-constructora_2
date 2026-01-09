package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoOtroCostoResponseDTO {
    private Long id;
    private String categoria;
    private String descripcion;
    private BigDecimal importe;
    private String observaciones;
}
