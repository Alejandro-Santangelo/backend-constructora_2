package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialDTO {
    
    private String tipoMaterial;

    @NotNull
    @PositiveOrZero
    private Double cantidad;

    @NotNull
    @PositiveOrZero
    private Double precioUnitario;

}
