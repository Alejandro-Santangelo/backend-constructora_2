package com.rodrigo.construccion.dto.response;

import com.rodrigo.construccion.enums.EstadoCobroEmpresa;
import com.rodrigo.construccion.enums.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para CobroEmpresa
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CobroEmpresaResponseDTO {

    private Long id;
    private Long empresaId;

    private BigDecimal montoTotal;
    private BigDecimal montoAsignado;
    private BigDecimal montoDisponible;

    private String descripcion;
    private LocalDate fechaCobro;
    private String metodoPago;
    private String numeroComprobante;
    private String tipoComprobante;
    private String observaciones;

    private String estado;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Para el detalle completo
    private List<AsignacionCobroEmpresaObraResponseDTO> asignaciones;
    
    // Cantidad de asignaciones (para listados)
    private Integer cantidadAsignaciones;
}
