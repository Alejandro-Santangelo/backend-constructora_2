package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para crear o actualizar asignaciones de cobro a obra
 * Los campos de distribución por items son OPCIONALES
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionCobroObraRequestDTO {

    @NotNull(message = "El ID del cobro es obligatorio")
    private Long cobroObraId;

    @NotNull(message = "El ID de la obra es obligatorio")
    private Long obraId;

    private Long presupuestoNoClienteId;

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    @NotNull(message = "El monto asignado es obligatorio")
    @Positive(message = "El monto asignado debe ser mayor a cero")
    private BigDecimal montoAsignado;

    // Distribución por ítems - TODOS OPCIONALES
    private BigDecimal montoProfesionales;
    private BigDecimal montoMateriales;
    private BigDecimal montoGastosGenerales;

    private BigDecimal porcentajeProfesionales;
    private BigDecimal porcentajeMateriales;
    private BigDecimal porcentajeGastosGenerales;

    private String estado;
    private String observaciones;
}
