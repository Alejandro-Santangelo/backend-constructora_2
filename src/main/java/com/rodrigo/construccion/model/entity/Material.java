package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Material
 * 
 * Representa los materiales utilizados en las obras.
 * Para otros tipos de empresa:
 * - Mueblería: Maderas, herrajes, telas, etc.
 * - Seguros: No aplica directamente
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "materiales")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long id;

    @Column(name = "empresa_id")
    private Long empresaId;

    @NotBlank(message = "El nombre del material es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Size(max = 50, message = "La unidad de medida no puede exceder 50 caracteres")
    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;

    @PositiveOrZero(message = "El precio unitario debe ser mayor o igual a cero")
    @Column(name = "precio_unitario", precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "activo")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Relaciones
    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<StockMaterial> stocks = new ArrayList<>();

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<MovimientoMaterial> movimientos = new ArrayList<>();

    // Métodos de conveniencia
    public void addStock(StockMaterial stock) {
        stocks.add(stock);
        stock.setMaterial(this);
    }

    public void removeStock(StockMaterial stock) {
        stocks.remove(stock);
        stock.setMaterial(null);
    }

    public void addMovimiento(MovimientoMaterial movimiento) {
        movimientos.add(movimiento);
        movimiento.setMaterial(this);
    }

    public void removeMovimiento(MovimientoMaterial movimiento) {
        movimientos.remove(movimiento);
        movimiento.setMaterial(null);
    }

    // Unidades de medida comunes
    public static final String UNIDAD_METRO = "Metro";
    public static final String UNIDAD_METRO2 = "Metro²";
    public static final String UNIDAD_METRO3 = "Metro³";
    public static final String UNIDAD_KILO = "Kilogramo";
    public static final String UNIDAD_LITRO = "Litro";
    public static final String UNIDAD_UNIDAD = "Unidad";
    public static final String UNIDAD_BOLSA = "Bolsa";
    public static final String UNIDAD_PAQUETE = "Paquete";
}