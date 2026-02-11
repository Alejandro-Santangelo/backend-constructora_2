package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO específico para actualizar solo el estado de un trabajo extra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para actualizar el estado de un trabajo extra")
public class TrabajoExtraEstadoUpdateDTO {

    @NotBlank(message = "El estado es obligatorio")
    @Schema(description = "Estado del trabajo extra: A_ENVIAR, ENVIADO, APROBADO, RECHAZADO, EN_REVISION", 
            example = "ENVIADO", 
            required = true)
    private String estado;
}