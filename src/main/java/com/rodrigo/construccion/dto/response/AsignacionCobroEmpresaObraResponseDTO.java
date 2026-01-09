package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para AsignacionCobroEmpresaObra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionCobroEmpresaObraResponseDTO {

    private Long id;
    private Long cobroEmpresaId;
    private Long cobroObraId;
    private Long obraId;
    
    // Información de la obra (para mostrar en UI)
    private String obraNombre;
    private String obraDireccion;
    
    private BigDecimal montoAsignado;
    private LocalDateTime fechaAsignacion;
    private String usuarioAsignacion;
    private String observaciones;
    
    // Flag para indicar si tiene distribución por ítems
    private Boolean tieneDistribucionItems;
}
