package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para profesionales de presupuesto con datos financieros calculados.
 * Específico para sistema de adelantos desde presupuestos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalPresupuestoFinancieroDTO {

    // IDs de referencia
    private Long id;                          // ID de profesional_calculadora
    private Long profesionalObraId;           // ID en asignaciones_profesional_obra (si está aprobado)
    private Long profesionalId;               // ID del profesional en tabla profesionales
    private Long profesionalCalculadoraId;    // Alias de id
    
    // Datos básicos del profesional
    private String nombre;
    private String tipoProfesional;
    private String tipo;                      // Alias de tipoProfesional
    private String telefono;
    private String email;
    private String cuit;
    
    // Datos de la obra/presupuesto
    private Long presupuestoId;
    private Long obraId;
    private String nombreObra;
    private String direccionObra;
    
    // Datos del presupuesto
    private BigDecimal cantidadJornales;
    private BigDecimal precioJornal;
    private BigDecimal importeJornal;         // Alias de precioJornal
    private BigDecimal jornal;                // Alias de precioJornal
    private BigDecimal precioTotal;           // cantidadJornales × precioJornal
    private BigDecimal montoTotal;            // Alias de precioTotal
    
    // Datos financieros calculados
    private BigDecimal totalPagado;           // Total de pagos realizados
    private Integer totalPagos;               // Cantidad de pagos hechos
    private BigDecimal saldoPendiente;        // precioTotal - totalPagado
    private BigDecimal porcentajePagado;      // (totalPagado / precioTotal) × 100
    
    // Datos de adelantos
    private BigDecimal totalAdelantos;        // Total de adelantos otorgados
    private BigDecimal totalAdelantosActivos; // Adelantos con saldo pendiente
    private Integer adelantosPendientes;      // Cantidad de adelantos activos
    private BigDecimal limiteAdelanto;        // 50% del precioTotal
    private BigDecimal adelantoDisponible;    // Cuánto más se puede adelantar
    private Boolean puedeOtorgarAdelanto;     // true si adelantoDisponible > 0
    
    // Campos adicionales
    private Long empresaId;
    private String estado;                    // Estado del profesional/asignación
    private String observaciones;
    
    // Alias para compatibilidad
    public Long getId() {
        return id;
    }
    
    public Long getProfesionalCalculadoraId() {
        return id;
    }
    
    public void setProfesionalCalculadoraId(Long id) {
        this.id = id;
    }
    
    public String getTipo() {
        return tipoProfesional;
    }
    
    public void setTipo(String tipo) {
        this.tipoProfesional = tipo;
    }
    
    public BigDecimal getImporteJornal() {
        return precioJornal;
    }
    
    public void setImporteJornal(BigDecimal importe) {
        this.precioJornal = importe;
    }
    
    public BigDecimal getJornal() {
        return precioJornal;
    }
    
    public void setJornal(BigDecimal jornal) {
        this.precioJornal = jornal;
    }
    
    public BigDecimal getMontoTotal() {
        return precioTotal;
    }
    
    public void setMontoTotal(BigDecimal monto) {
        this.precioTotal = monto;
    }
}
