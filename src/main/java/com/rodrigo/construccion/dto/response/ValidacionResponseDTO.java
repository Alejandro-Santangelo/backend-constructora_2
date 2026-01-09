package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta genérica para operaciones de validación.")
public class ValidacionResponseDTO {
    @Schema(description = "Indica si la validación fue exitosa (el recurso está disponible).", example = "true")
    private boolean disponible;
}