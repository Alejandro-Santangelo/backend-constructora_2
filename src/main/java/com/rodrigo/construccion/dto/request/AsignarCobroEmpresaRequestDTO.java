package com.rodrigo.construccion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para asignar un cobro empresa a una o varias obras
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignarCobroEmpresaRequestDTO {

    @NotEmpty(message = "Debe incluir al menos una asignación")
    @Valid
    private List<AsignacionObraDTO> asignaciones;
}
