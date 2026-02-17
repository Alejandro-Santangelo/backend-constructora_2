package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Request para obtener estadísticas de múltiples entidades financieras.
 */
@Data
public class EstadisticasMultiplesRequestDTO {

    @NotNull(message = "empresaId es obligatorio")
    private Long empresaId;

    @NotEmpty(message = "Debe indicar al menos una entidad financiera")
    private List<Long> entidadesFinancierasIds;
}
