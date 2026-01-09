package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de respuesta para asignaciones de profesionales a rubros de obras
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de asignación de profesional a rubro de obra")
public class AsignacionProfesionalObraDTO {

    @Schema(description = "ID de la asignación", example = "1")
    private Long id;

    @Schema(description = "ID del profesional", example = "45")
    private Long profesionalId;

    @Schema(description = "Nombre del profesional", example = "Juan Pérez")
    private String profesionalNombre;

    @Schema(description = "Tipo de profesional", example = "Albañil")
    private String profesionalTipo;

    @Schema(description = "ID de la obra", example = "70")
    private Long obraId;

    @Schema(description = "Nombre de la obra", example = "Casa de Paula")
    private String obraNombre;

    @Schema(description = "ID del rubro", example = "1689")
    private Long rubroId;

    @Schema(description = "ID del item específico dentro del rubro", example = "697")
    private Long itemId;

    @Schema(description = "Nombre del rubro", example = "Albañilería")
    private String rubroNombre;

    @Schema(description = "ID del presupuesto asociado", example = "611")
    private Long presupuestoNoClienteId;

    @Schema(description = "Tipo de asignación", example = "JORNAL", allowableValues = {"PROFESIONAL", "JORNAL"})
    private String tipoAsignacion;

    @Schema(description = "Cantidad de jornales asignados", example = "10")
    private Integer cantidadJornales;

    @Schema(description = "Jornales ya utilizados", example = "3")
    private Integer jornalesUtilizados;

    @Schema(description = "Jornales restantes", example = "7")
    private Integer jornalesRestantes;

    @Schema(description = "Fecha de inicio", example = "2025-12-01")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de fin", example = "2025-12-15")
    private LocalDate fechaFin;

    @Schema(description = "Estado de la asignación", example = "ACTIVO", allowableValues = {"ACTIVO", "FINALIZADO", "CANCELADO"})
    private String estado;

    @Schema(description = "Observaciones", example = "Asignado para trabajos de cimientos")
    private String observaciones;

    @Schema(description = "Modalidad de asignación", example = "total", allowableValues = {"total", "semanal"})
    private String modalidad;

    @Schema(description = "Semanas objetivo para completar la obra", example = "10")
    private Integer semanasObjetivo;
}
