package com.rodrigo.construccion.dto.request;

import com.rodrigo.construccion.enums.TipoEntidadFinanciera;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request para sincronizar (insertar o actualizar) una entidad financiera.
 *
 * Se llama:
 * - Al crear/actualizar una Obra (tipo OBRA_PRINCIPAL u OBRA_INDEPENDIENTE)
 * - Al crear/actualizar un TrabajoExtra (tipo TRABAJO_EXTRA)
 * - Al crear/actualizar un TrabajoAdicional (tipo TRABAJO_ADICIONAL)
 */
@Data
public class SincronizarEntidadFinancieraRequestDTO {

    @NotNull(message = "empresaId es obligatorio")
    private Long empresaId;

    @NotNull(message = "tipoEntidad es obligatorio")
    private TipoEntidadFinanciera tipoEntidad;

    @NotNull(message = "entidadId es obligatorio")
    private Long entidadId;

    /**
     * Solo para OBRA_PRINCIPAL y TRABAJO_EXTRA.
     * Se puede omitir (null) para los otros tipos.
     */
    private Long presupuestoNoClienteId;

    /** Nombre de la entidad para cacheo en nombre_display. */
    private String nombreDisplay;
}
