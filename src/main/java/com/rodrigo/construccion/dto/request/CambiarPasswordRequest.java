package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para cambiar la contraseña de un usuario
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarPasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String passwordNueva;
}

