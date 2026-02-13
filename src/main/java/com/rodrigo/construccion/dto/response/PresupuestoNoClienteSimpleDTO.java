package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodrigo.construccion.enums.PresupuestoEstado;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO simple para Presupuesto No Cliente
 * Usado para evitar referencias circulares cuando se incluye en ObraResponseDTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información básica de un presupuesto no cliente")
public class PresupuestoNoClienteSimpleDTO {

    @Schema(description = "ID único del presupuesto", example = "7")
    private Long id;

    @Schema(description = "Indica si es un presupuesto de trabajo extra", example = "true")
    private Boolean esPresupuestoTrabajoExtra;

    @Schema(description = "ID de la obra asociada", example = "10")
    private Long obraId;

    @Schema(description = "Nombre de la obra", example = "Estancia de Susana Gimenez Piscina")
    private String nombreObra;

    @Schema(description = "Estado del presupuesto", example = "APROBADO")
    private PresupuestoEstado estado;

    @Schema(description = "Fecha de emisión del presupuesto", example = "2026-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEmision;

    @Schema(description = "Fecha probable de inicio de obra", example = "2026-02-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaProbableInicio;

    @Schema(description = "Total del presupuesto base (sin honorarios)", example = "850000.00")
    private BigDecimal totalPresupuesto;

    @Schema(description = "Total de honorarios calculados", example = "127500.00")
    private BigDecimal totalHonorariosCalculado;

    @Schema(description = "Total final del presupuesto (con honorarios)", example = "977500.00")
    @JsonProperty("totalFinal")
    private BigDecimal totalPresupuestoConHonorarios;

    @Schema(description = "Nombre del solicitante", example = "Susana Gimenez")
    private String nombreSolicitante;

    @Schema(description = "Observaciones del presupuesto", example = "Proyecto piscina climatizada")
    private String observaciones;
}
