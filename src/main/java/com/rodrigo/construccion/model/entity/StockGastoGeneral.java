package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_gastos_generales")
public class StockGastoGeneral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "gasto_general_id", nullable = false)
    private Long gastoGeneralId;
    
    @Column(name = "cantidad_disponible", nullable = false)
    private BigDecimal cantidadDisponible = BigDecimal.ZERO;
    
    @Column(name = "cantidad_minima")
    private BigDecimal cantidadMinima = BigDecimal.ZERO;
    
    @Column(name = "precio_unitario")
    private BigDecimal precioUnitario;
    
    @Column(name = "ubicacion")
    private String ubicacion;
    
    @Column(name = "observaciones")
    private String observaciones;
    
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gasto_general_id", insertable = false, updatable = false)
    private GastoGeneral gastoGeneral;
    
    // Constructor vacío
    public StockGastoGeneral() {}
    
    // Constructor con parámetros
    public StockGastoGeneral(Long gastoGeneralId, BigDecimal cantidadDisponible, BigDecimal cantidadMinima, 
                           BigDecimal precioUnitario, Long empresaId) {
        this.gastoGeneralId = gastoGeneralId;
        this.cantidadDisponible = cantidadDisponible;
        this.cantidadMinima = cantidadMinima;
        this.precioUnitario = precioUnitario;
        this.empresaId = empresaId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters y setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getGastoGeneralId() {
        return gastoGeneralId;
    }
    
    public void setGastoGeneralId(Long gastoGeneralId) {
        this.gastoGeneralId = gastoGeneralId;
    }
    
    public BigDecimal getCantidadDisponible() {
        return cantidadDisponible;
    }
    
    public void setCantidadDisponible(BigDecimal cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }
    
    public BigDecimal getCantidadMinima() {
        return cantidadMinima;
    }
    
    public void setCantidadMinima(BigDecimal cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public String getUbicacion() {
        return ubicacion;
    }
    
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public Long getEmpresaId() {
        return empresaId;
    }
    
    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public GastoGeneral getGastoGeneral() {
        return gastoGeneral;
    }
    
    public void setGastoGeneral(GastoGeneral gastoGeneral) {
        this.gastoGeneral = gastoGeneral;
    }
}