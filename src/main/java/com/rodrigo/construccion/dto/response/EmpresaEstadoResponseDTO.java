package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta con el estado de una empresa.")
public class EmpresaEstadoResponseDTO {

    @Schema(description = "Indica si la empresa existe en la base de datos.", example = "true")
    private boolean existe;
    @Schema(description = "Indica si la empresa está marcada como activa.", example = "true")
    private Boolean activa;
    @Schema(description = "Nombre de la empresa, si existe.", example = "Constructora XYZ")
    private String nombre;
}