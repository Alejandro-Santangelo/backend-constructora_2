package com.rodrigo.construccion.dto.response;

import com.rodrigo.construccion.enums.TipoEntidadFinanciera;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Representación de una entidad financiera registrada en el sistema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntidadFinancieraResponseDTO {

    private Long id;
    private Long empresaId;
    private TipoEntidadFinanciera tipoEntidad;
    private Long entidadId;
    private Long presupuestoNoClienteId;
    private String nombreDisplay;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
