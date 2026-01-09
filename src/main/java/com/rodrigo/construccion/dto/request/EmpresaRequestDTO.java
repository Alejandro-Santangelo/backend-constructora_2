package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para las peticiones de creación y actualización de empresas
 * Solo contiene los campos mínimos necesarios para el sistema multi-tenant
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear o actualizar una empresa")
public class EmpresaRequestDTO {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombreEmpresa;

    @NotBlank(message = "El cuit de la empresa es obligatorio")
    @Size(max = 20, message = "El CUIT no puede exceder 20 caracteres")
    private String cuit;

    @NotBlank(message = "La dirección fiscal de la empresa es obligatoria")
    private String direccionFiscal;

    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    private String telefono;

    @NotBlank(message = "El email de la empresa es obligatoria")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @Size(max = 200, message = "El representante legal no puede exceder 200 caracteres")
    private String representanteLegal;

    private Boolean activa = true;

}