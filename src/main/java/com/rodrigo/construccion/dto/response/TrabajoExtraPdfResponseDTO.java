package com.rodrigo.construccion.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para PDFs de trabajos extra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraPdfResponseDTO {
    
    private Long id;
    private Long trabajoExtraId;
    private String nombreArchivo;
    private Long tamanioBytes;
    private LocalDateTime fechaGeneracion;
    private String generadoPor;
    private Integer versionTrabajoExtra;
    private Boolean incluyeHonorarios;
    private Boolean incluyeConfiguracion;
}
