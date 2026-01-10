package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Filter;

/**
 * Entidad para gastos generales individuales dentro de un item de calculadora.
 * Implementación completamente relacional sin uso de JSON.
 * 
 * Cada instancia representa un gasto específico como:
 * - Alquiler de andamios
 * - Seguros de obra
 * - Fletes
 * - Servicios temporales
 * etc.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "presupuesto_gasto_general")
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class PresupuestoGastoGeneral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gasto_general")
    private Long id;

    /**
     * Relación con el item de calculadora que contiene este gasto.
     * Un item puede tener múltiples gastos generales.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_calculadora_id", nullable = false)
    @JsonBackReference
    private ItemCalculadoraPresupuesto itemCalculadora;

    /**
     * Descripción específica del gasto.
     * Ejemplo: "Alquiler de andamios", "Seguros de obra"
     */
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    /**
     * Observaciones adicionales sobre el gasto general.
     */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Cantidad de unidades del gasto.
     * Se ignora en cálculos si sinCantidad = true.
     */
    @Column(name = "cantidad", precision = 10, scale = 2, nullable = false)
    private BigDecimal cantidad = BigDecimal.ONE;

    /**
     * Precio por unidad del gasto.
     * Se ignora en cálculos si sinPrecio = true.
     */
    @Column(name = "precio_unitario", precision = 15, scale = 2, nullable = false)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    /**
     * Resultado calculado: cantidad × precioUnitario.
     * Este valor debe sincronizarse automáticamente.
     */
    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Si es true, este gasto no maneja cantidad (precio fijo).
     * El cálculo ignora el campo cantidad.
     */
    @Column(name = "sin_cantidad", nullable = false)
    private Boolean sinCantidad = false;

    /**
     * Si es true, este gasto no maneja precio unitario.
     * Usado para gastos donde solo se ingresa un monto total.
     */
    @Column(name = "sin_precio", nullable = false)
    private Boolean sinPrecio = false;

    /**
     * Orden de visualización dentro del item.
     * Permite mantener el orden de ingreso desde el frontend.
     */
    @Column(name = "orden", nullable = false)
    private Integer orden = 1;

    @Column(name = "es_global", nullable = false)
    private Boolean esGlobal = false;

    /**
     * ID para vinculación con sistema de stock de gastos generales.
     * Permite conectar este gasto con el inventario de gastos generales.
     */
    @Column(name = "frontend_id", nullable = true)
    private Long frontendId;

    /**
     * Relación con empresa para filtrado multi-tenant.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonBackReference
    private Empresa empresa;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Calcular subtotal al crear
        calcularSubtotal();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Recalcular subtotal al actualizar
        calcularSubtotal();
    }

    /**
     * Calcula y establece el subtotal según las reglas de negocio.
     * - Si sinCantidad y sinPrecio = false: subtotal = cantidad × precioUnitario
     * - Si sinCantidad = true: subtotal = precioUnitario (ignora cantidad)
     * - Si sinPrecio = true: subtotal se mantiene como está (valor manual)
     */
    public void calcularSubtotal() {
        if (Boolean.TRUE.equals(sinPrecio)) {
            // Si no maneja precio, mantener subtotal actual (valor manual)
            return;
        }
        
        if (Boolean.TRUE.equals(sinCantidad)) {
            // Si no maneja cantidad, subtotal = precio unitario
            this.subtotal = this.precioUnitario != null ? this.precioUnitario : BigDecimal.ZERO;
        } else {
            // Caso normal: subtotal = cantidad × precio unitario
            BigDecimal cantidadCalc = this.cantidad != null ? this.cantidad : BigDecimal.ZERO;
            BigDecimal precioCalc = this.precioUnitario != null ? this.precioUnitario : BigDecimal.ZERO;
            this.subtotal = cantidadCalc.multiply(precioCalc);
        }
    }

    /**
     * Valida la consistencia de los datos del gasto.
     * @throws IllegalStateException si los datos son inconsistentes
     */
    public void validar() {
        // Descripción obligatoria
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalStateException("La descripción del gasto general es obligatoria");
        }

        // Validar coherencia de flags
        if (Boolean.TRUE.equals(sinCantidad) && Boolean.TRUE.equals(sinPrecio)) {
            throw new IllegalStateException("Un gasto no puede tener sinCantidad=true Y sinPrecio=true simultáneamente");
        }

        // Si no maneja cantidad, debe tener precio unitario
        if (Boolean.TRUE.equals(sinCantidad) && 
            (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalStateException("Si sinCantidad=true, debe especificar precioUnitario > 0");
        }

        // Si maneja cantidad, validar que sea positiva
        if (Boolean.FALSE.equals(sinCantidad) && 
            (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalStateException("Si sinCantidad=false, la cantidad debe ser > 0");
        }

        // Si maneja precio unitario, validar que sea positivo
        if (Boolean.FALSE.equals(sinPrecio) && 
            (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalStateException("Si sinPrecio=false, el precioUnitario debe ser > 0");
        }

        // Validar que el orden sea positivo
        if (orden == null || orden <= 0) {
            throw new IllegalStateException("El orden debe ser un número positivo");
        }

        // Validar subtotal positivo
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El subtotal debe ser mayor o igual a 0");
        }
    }

    /**
     * Método de conveniencia para configurar valores por defecto.
     */
    public void configurarDefaults() {
        if (cantidad == null) cantidad = BigDecimal.ONE;
        if (precioUnitario == null) precioUnitario = BigDecimal.ZERO;
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (sinCantidad == null) sinCantidad = false;
        if (sinPrecio == null) sinPrecio = false;
        if (orden == null) orden = 1;
        
        // Calcular subtotal con los valores configurados
        calcularSubtotal();
    }

    @Override
    public String toString() {
        return String.format("PresupuestoGastoGeneral{id=%d, descripcion='%s', cantidad=%s, precioUnitario=%s, subtotal=%s, orden=%d}", 
            id, descripcion, cantidad, precioUnitario, subtotal, orden);
    }
}