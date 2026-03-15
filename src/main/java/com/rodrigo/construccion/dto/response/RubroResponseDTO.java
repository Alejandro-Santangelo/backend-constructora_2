package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Rubro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RubroResponseDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private Boolean activo;
}
