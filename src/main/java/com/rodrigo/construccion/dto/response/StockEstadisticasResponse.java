package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para estadísticas generales de stock de materiales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estadísticas generales del stock de materiales")
public class StockEstadisticasResponse {

    @Schema(description = "Total de registros de stock", example = "150")
    private Long totalRegistros;

    @Schema(description = "Total de materiales diferentes en stock", example = "45")
    private Long totalMaterialesDiferentes;

    @Schema(description = "Cantidad de materiales con stock bajo", example = "12")
    private Long materialesStockBajo;

    @Schema(description = "Cantidad de materiales agotados", example = "5")
    private Long materialesAgotados;

    @Schema(description = "Valor total del inventario", example = "125000.50")
    private BigDecimal valorTotalInventario;

    @Schema(description = "Cantidad de ubicaciones distintas", example = "8")
    private Long totalUbicaciones;

    @Schema(description = "Cantidad de materiales próximos a vencer (30 días)", example = "3")
    private Long materialesProximosVencer;
}

