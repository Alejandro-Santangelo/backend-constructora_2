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
public class PresupuestoMaterialResponseDTO {
    private Long id;
    private String nombreMaterial;
    private BigDecimal cantidad;
    private String unidadMedida;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String categoria;
    private String descripcion;
    private String observaciones;
    
    // Campos opcionales para stock (compatibilidad)
    private BigDecimal cantidadDisponible;
    private String estadoStock;
}
