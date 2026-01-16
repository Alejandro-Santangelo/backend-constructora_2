package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_material", indexes = {
        @Index(name = "idx_stock_material", columnList = "id_material"),
        @Index(name = "idx_stock_ubicacion", columnList = "ubicacion"),
        @Index(name = "idx_stock_empresa", columnList = "id_empresa")
})
@Filter(name = "empresaFilter", condition = "id_empresa = :empresaId")
public class StockMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock_material")
    private Long id;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "ubicacion", length = 200)
    private String ubicacion;

    @NotNull(message = "La cantidad actual es obligatoria")
    @PositiveOrZero(message = "La cantidad actual debe ser mayor o igual a cero")
    @Column(name = "cantidad_actual", nullable = false)
    private Double cantidadActual = 0.0;

    @Column(name = "cantidad_minima")
    private Double cantidadMinima;

    @Column(name = "cantidad_maxima")
    private Double cantidadMaxima;

    @Column(name = "precio_unitario_promedio")
    private Double precioUnitarioPromedio;

    @Column(name = "valor_total")
    private Double valorTotal;

    @Column(name = "numero_lote", length = 100)
    private String numeroLote;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_ultimo_movimiento")
    private LocalDateTime fechaUltimoMovimiento;

    @Column(name = "estado", length = 50)
    private String estado = "ACTIVO";

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @UpdateTimestamp
    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_material", nullable = false)
    @JsonBackReference("material-stocks")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    // Getters de conveniencia
    public Long getMaterialId() {
        return material != null ? material.getId() : null;
    }

    public String getNombreMaterial() {
        return material != null ? material.getNombre() : null;
    }

    public String getUnidadMedida() {
        return material != null ? material.getUnidadMedida() : null;
    }

    public Long getEmpresaId() {
        return empresa != null ? empresa.getId() : null;
    }

    // Métodos de utilidad
    public boolean hayStock() {
        return cantidadActual != null && cantidadActual > 0;
    }

    public boolean stockBajo() {
        return cantidadMinima != null && cantidadActual != null && cantidadActual <= cantidadMinima;
    }

    public boolean estaAgotado() {
        return cantidadActual == null || cantidadActual <= 0;
    }

    public boolean estaProximoAVencer(int diasAntelacion) {
        if (fechaVencimiento == null) {
            return false;
        }
        LocalDate fechaLimite = LocalDate.now().plusDays(diasAntelacion);
        return fechaVencimiento.isBefore(fechaLimite) || fechaVencimiento.isEqual(fechaLimite);
    }

    @PrePersist
    protected void onCreate() {
        fechaUltimaActualizacion = LocalDateTime.now();
        if (estado == null) {
            estado = "ACTIVO";
        }
        if (cantidadActual == null) {
            cantidadActual = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaUltimaActualizacion = LocalDateTime.now();
        if (cantidadActual != null && precioUnitarioPromedio != null) {
            valorTotal = cantidadActual * precioUnitarioPromedio;
        }
    }
}
