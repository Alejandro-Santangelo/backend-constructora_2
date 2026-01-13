package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * DTO de respuesta para estadísticas de proveedores
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estadísticas completas de proveedores de una empresa")
public class ProveedorEstadisticaResponseDTO {

    @Schema(description = "Total de proveedores en la empresa", example = "45")
    private long total;

    @Schema(description = "Cantidad de proveedores activos", example = "40")
    private long activos;

    @Schema(description = "Cantidad de proveedores inactivos", example = "5")
    private long inactivos;

    @Schema(description = "Porcentaje de proveedores activos", example = "88.89")
    private double porcentajeActivos;

    @Schema(description = "Distribución de proveedores por ciudad")
    private List<Map<String, Object>> distribucionPorCiudad;

    @Schema(description = "Distribución de proveedores por región")
    private List<Map<String, Object>> distribucionPorRegion;
}
