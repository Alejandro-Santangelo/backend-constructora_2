package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para materiales con información de stock disponible
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaterialConStockResponseDTO {
    private Long id;
    private String nombreMaterial;
    private String unidadMedida;
    private BigDecimal precioUnitario;
    private BigDecimal cantidadDisponible;  // Cantidad disponible en stock
    private BigDecimal cantidadAsignada;    // Cantidad ya asignada a obras
    private BigDecimal cantidadRestante;    // Cantidad restante = disponible - asignada
    private String categoria;
    private String descripcion;
    private String ubicacion;
    private String estado; // DISPONIBLE, AGOTADO, STOCK_BAJO
}