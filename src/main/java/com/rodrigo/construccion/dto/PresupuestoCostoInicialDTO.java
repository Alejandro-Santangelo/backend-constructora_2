package com.rodrigo.construccion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para transferir datos de costos iniciales por metros cuadrados
 * entre frontend y backend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoCostoInicialDTO {
    
    private Long id;

    @NotNull(message = "Los metros cuadrados son obligatorios")
    @DecimalMin(value = "0.01", message = "Los metros cuadrados deben ser mayor a 0")
    private BigDecimal metrosCuadrados;

    @NotNull(message = "El importe por metro es obligatorio")
    @DecimalMin(value = "0.01", message = "El importe por metro debe ser mayor a 0")
    private BigDecimal importePorMetro;

    @DecimalMin(value = "0.00", message = "El total estimado debe ser mayor o igual a 0")
    private BigDecimal totalEstimado;

    @NotNull(message = "El porcentaje de profesionales es obligatorio")
    @Min(value = 0, message = "El porcentaje de profesionales debe estar entre 0 y 100")
    @Max(value = 100, message = "El porcentaje de profesionales debe estar entre 0 y 100")
    private BigDecimal porcentajeProfesionales;

    @NotNull(message = "El porcentaje de materiales es obligatorio")
    @Min(value = 0, message = "El porcentaje de materiales debe estar entre 0 y 100")
    @Max(value = 100, message = "El porcentaje de materiales debe estar entre 0 y 100")
    private BigDecimal porcentajeMateriales;

    @NotNull(message = "El porcentaje de otros costos es obligatorio")
    @Min(value = 0, message = "El porcentaje de otros costos debe estar entre 0 y 100")
    @Max(value = 100, message = "El porcentaje de otros costos debe estar entre 0 y 100")
    private BigDecimal porcentajeOtrosCostos;

    @DecimalMin(value = "0.00", message = "El monto de profesionales debe ser mayor o igual a 0")
    private BigDecimal montoProfesionales;

    @DecimalMin(value = "0.00", message = "El monto de materiales debe ser mayor o igual a 0")
    private BigDecimal montoMateriales;

    @DecimalMin(value = "0.00", message = "El monto de otros costos debe ser mayor o igual a 0")
    private BigDecimal montoOtrosCostos;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaGuardado;
}
