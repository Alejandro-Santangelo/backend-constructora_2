package com.rodrigo.construccion.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity para registrar movimientos de caja chica (asignaciones y gastos)
 * Migrado desde JSONB a tabla relacional para mejor performance y escalabilidad
 */
@Entity
@Table(name = "caja_chica_movimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaChicaMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "presupuesto_id", nullable = false)
    private Long presupuestoId;

    @Column(name = "profesional_nombre", nullable = false, length = 100)
    private String profesionalNombre;

    @Column(name = "profesional_tipo", nullable = false, length = 50)
    private String profesionalTipo;

    /**
     * Tipo de movimiento: ASIGNACION (dinero entregado) o GASTO (dinero gastado)
     */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Hook que se ejecuta antes de persistir el entity
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Hook que se ejecuta antes de actualizar el entity
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
