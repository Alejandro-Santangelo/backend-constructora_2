package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO de respuesta para eliminación en cascada de obras
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EliminacionCascadaResponseDTO {
    
    private String mensaje;
    
    private Long obraId;
    
    private Map<String, Integer> registrosEliminados;
}
