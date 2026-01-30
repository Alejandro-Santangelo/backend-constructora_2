package com.rodrigo.construccion.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialRequestDTO {
    @NotBlank(message = "El nombre del material es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String nombre;
    private String descripcion;
    private String unidadMedida;
    private BigDecimal precioUnitario;
}
