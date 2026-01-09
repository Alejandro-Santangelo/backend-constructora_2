package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para anular un cobro empresa
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnularCobroEmpresaRequestDTO {

    @NotBlank(message = "El motivo de anulación es obligatorio")
    private String motivo;
}
