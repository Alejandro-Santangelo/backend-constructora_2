package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response para gasto general de trabajo extra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraGastoGeneralResponseDTO {
    
    private Long id;
    private Long itemCalculadoraId;
    private Long empresaId;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private Boolean sinCantidad;
    private Boolean sinPrecio;
    private Integer orden;
    private Long frontendId;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
