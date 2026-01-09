package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para las peticiones de creación y actualización de clientes
 * Contiene solo los campos que el usuario debe proporcionar
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear o actualizar un cliente")
public class ClienteRequestDTO {

    @Schema(description = "Nombre del cliente (razón social)", example = "Juan Pérez")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    private String nombre;

    @Schema(description = "Nombre del solicitante/contacto", example = "María García")
    @Size(max = 200, message = "El nombre del solicitante no puede exceder 200 caracteres")
    private String nombreSolicitante;

    @Schema(description = "CUIT/CUIL del cliente", example = "20-12345678-9")
    @Size(max = 15, message = "El CUIT/CUIL no puede exceder 15 caracteres")
    private String cuitCuil;

    @Schema(description = "Dirección del cliente", example = "Av. Corrientes 1234, CABA")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @Schema(description = "Teléfono del cliente", example = "+54 11 1234-5678")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Schema(description = "Email del cliente", example = "juan.perez@email.com")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

}