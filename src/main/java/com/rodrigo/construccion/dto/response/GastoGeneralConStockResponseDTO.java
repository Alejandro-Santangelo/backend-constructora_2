package com.rodrigo.construccion.dto.response;

import java.math.BigDecimal;

public class GastoGeneralConStockResponseDTO {
    
    private Long id;
    private String nombreGastoGeneral;
    private String descripcion;
    private String unidadMedida;
    private String categoria;
    private BigDecimal precioUnitario;
    private BigDecimal cantidadDisponible;
    private BigDecimal cantidadAsignada;
    private BigDecimal cantidadRestante;
    private String estadoStock;
    private String observaciones;
    
    // Constructor vacío
    public GastoGeneralConStockResponseDTO() {}
    
    // Constructor completo
    public GastoGeneralConStockResponseDTO(Long id, String nombreGastoGeneral, String descripcion, 
                                          String unidadMedida, String categoria, BigDecimal precioUnitario, 
                                          BigDecimal cantidadDisponible, BigDecimal cantidadAsignada, 
                                          String observaciones) {
        this.id = id;
        this.nombreGastoGeneral = nombreGastoGeneral;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.categoria = categoria;
        this.precioUnitario = precioUnitario;
        this.cantidadDisponible = cantidadDisponible != null ? cantidadDisponible : BigDecimal.ZERO;
        this.cantidadAsignada = cantidadAsignada != null ? cantidadAsignada : BigDecimal.ZERO;
        this.cantidadRestante = this.cantidadDisponible.subtract(this.cantidadAsignada);
        this.estadoStock = determinarEstadoStock();
        this.observaciones = observaciones;
    }
    
    private String determinarEstadoStock() {
        if (cantidadDisponible == null || cantidadDisponible.compareTo(BigDecimal.ZERO) <= 0) {
            return "SIN_STOCK";
        }
        if (cantidadRestante.compareTo(BigDecimal.ZERO) <= 0) {
            return "AGOTADO";
        }
        if (cantidadRestante.compareTo(cantidadDisponible.multiply(new BigDecimal("0.2"))) <= 0) {
            return "STOCK_BAJO";
        }
        return "DISPONIBLE";
    }
    
    // Getters y setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombreGastoGeneral() {
        return nombreGastoGeneral;
    }
    
    public void setNombreGastoGeneral(String nombreGastoGeneral) {
        this.nombreGastoGeneral = nombreGastoGeneral;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getUnidadMedida() {
        return unidadMedida;
    }
    
    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public BigDecimal getCantidadDisponible() {
        return cantidadDisponible;
    }
    
    public void setCantidadDisponible(BigDecimal cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
        this.cantidadRestante = this.cantidadDisponible.subtract(
            this.cantidadAsignada != null ? this.cantidadAsignada : BigDecimal.ZERO
        );
        this.estadoStock = determinarEstadoStock();
    }
    
    public BigDecimal getCantidadAsignada() {
        return cantidadAsignada;
    }
    
    public void setCantidadAsignada(BigDecimal cantidadAsignada) {
        this.cantidadAsignada = cantidadAsignada;
        this.cantidadRestante = (this.cantidadDisponible != null ? this.cantidadDisponible : BigDecimal.ZERO)
            .subtract(this.cantidadAsignada != null ? this.cantidadAsignada : BigDecimal.ZERO);
        this.estadoStock = determinarEstadoStock();
    }
    
    public BigDecimal getCantidadRestante() {
        return cantidadRestante;
    }
    
    public void setCantidadRestante(BigDecimal cantidadRestante) {
        this.cantidadRestante = cantidadRestante;
    }
    
    public String getEstadoStock() {
        return estadoStock;
    }
    
    public void setEstadoStock(String estadoStock) {
        this.estadoStock = estadoStock;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}