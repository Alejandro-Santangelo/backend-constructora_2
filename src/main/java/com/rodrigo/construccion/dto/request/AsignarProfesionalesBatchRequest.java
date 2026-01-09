package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Solicitud para asignar varios profesionales a una obra")
public class AsignarProfesionalesBatchRequest {

    @Schema(description = "ID de la empresa", example = "1", required = true)
    private Long empresaId;

    @Schema(description = "ID de la obra", example = "10", required = true)
    private Long obraId;

    @Schema(description = "Lista de IDs de profesionales a asignar", example = "[2, 3, 5]", required = true)
    private List<Long> profesionalesIds;

}
