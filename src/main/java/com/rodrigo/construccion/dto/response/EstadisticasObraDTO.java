package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que contiene estadísticas básicas sobre las obras")
public class EstadisticasObraDTO {
    @Schema(description = "Número total de obras registradas en el sistema", example = "50")
    private long totalObras;

    @Schema(description = "Descripción general de las estadísticas presentadas", example = "Estadísticas básicas de obras")
    private String descripcion;
}