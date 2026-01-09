package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response con datos de caja chica de obra")
public class CajaChicaObraResponseDTO {

    @Schema(description = "ID de la caja chica", example = "1")
    private Long id;

    @Schema(description = "ID de la empresa", example = "3")
    private Long empresaId;

    @Schema(description = "ID del presupuesto", example = "123")
    private Long presupuestoNoClienteId;

    @Schema(description = "ID del profesional en itemsCalculadora", example = "456")
    private Long profesionalObraId;

    @Schema(description = "Nombre del profesional")
    private String nombreProfesional;

    @Schema(description = "Tipo de profesional", example = "ALBANIL")
    private String tipoProfesional;

    @Schema(description = "Monto asignado", example = "5000.00")
    private BigDecimal monto;

    @Schema(description = "Fecha de asignación")
    private LocalDate fecha;

    @Schema(description = "Observaciones")
    private String observaciones;

    @Schema(description = "Estado", example = "ACTIVO", allowableValues = {"ACTIVO", "RENDIDO", "ANULADO"})
    private String estado;

    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha de última modificación")
    private LocalDateTime updatedAt;
}
