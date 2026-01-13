package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para la validación de RUT de proveedor
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de la validación de RUT de proveedor")
public class RutValidacionResponseDTO {

    @Schema(description = "Indica si el RUT está disponible para usar", example = "true")
    private boolean disponible;

    @Schema(description = "RUT validado", example = "12345678-9")
    private String rut;

    @Schema(description = "Mensaje descriptivo del resultado", example = "El RUT está disponible")
    private String mensaje;

    public RutValidacionResponseDTO(boolean disponible, String rut) {
        this.disponible = disponible;
        this.rut = rut;
        this.mensaje = disponible
            ? "El RUT está disponible"
            : "El RUT ya está registrado en otro proveedor";
    }
}

