package com.rodrigo.construccion.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar SOLO las fechas de presupuestos APROBADOS o EN_EJECUCION.
 * No modifica versión ni estado, solo campos de planificación temporal.
 */
public class ActualizarFechasDTO {
    
    @NotNull(message = "La fecha probable de inicio es obligatoria")
    private LocalDate fechaProbableInicio;
    
    @NotNull(message = "El tiempo estimado de terminación es obligatorio")
    @Min(value = 1, message = "El tiempo debe ser mayor a 0")
    @Max(value = 9999, message = "El tiempo no puede exceder 9999 días")
    private Integer tiempoEstimadoTerminacion;
    
    // Constructors
    public ActualizarFechasDTO() {
    }
    
    public ActualizarFechasDTO(LocalDate fechaProbableInicio, Integer tiempoEstimadoTerminacion) {
        this.fechaProbableInicio = fechaProbableInicio;
        this.tiempoEstimadoTerminacion = tiempoEstimadoTerminacion;
    }
    
    // Getters and Setters
    public LocalDate getFechaProbableInicio() {
        return fechaProbableInicio;
    }
    
    public void setFechaProbableInicio(LocalDate fechaProbableInicio) {
        this.fechaProbableInicio = fechaProbableInicio;
    }
    
    public Integer getTiempoEstimadoTerminacion() {
        return tiempoEstimadoTerminacion;
    }
    
    public void setTiempoEstimadoTerminacion(Integer tiempoEstimadoTerminacion) {
        this.tiempoEstimadoTerminacion = tiempoEstimadoTerminacion;
    }
}
