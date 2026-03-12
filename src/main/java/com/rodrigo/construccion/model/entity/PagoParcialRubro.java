package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity para registrar pagos parciales sobre items de rubros del presupuesto
 * Ejemplo: Pago de $500,000 sobre "Jornales" del rubro "Albañilería"
 */
@Entity
@Table(name = "pagos_parciales_rubros", indexes = {
    @Index(name = "idx_pagos_parciales_presupuesto", columnList = "presupuesto_id"),
    @Index(name = "idx_pagos_parciales_empresa", columnList = "empresa_id"),
    @Index(name = "idx_pagos_parciales_rubro", columnList = "presupuesto_id, nombre_rubro"),
    @Index(name = "idx_pagos_parciales_rubro_item", columnList = "presupuesto_id, nombre_rubro, tipo_item")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoParcialRubro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "presupuesto_id", nullable = false)
    private Long presupuestoId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    /**
     * Nombre del rubro del presupuesto
     * Ej: "Albañilería", "Plomería", "Pintura"
     */
    @Column(name = "nombre_rubro", nullable = false, length = 255)
    private String nombreRubro;

    /**
     * Tipo de item dentro del rubro
     * Valores: JORNALES, MATERIALES, GASTOS_GENERALES
     */
    @Column(name = "tipo_item", nullable = false, length = 50)
    private String tipoItem;

    /**
     * Monto del pago parcial
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    /**
     * Método de pago utilizado
     * Valores comunes: EFECTIVO, TRANSFERENCIA, CHEQUE, TARJETA
     */
    @Column(name = "metodo_pago", length = 50)
    @Builder.Default
    private String metodoPago = "EFECTIVO";

    /**
     * Observaciones adicionales del pago
     */
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    /**
     * Fecha en que se realizó el pago
     */
    @Column(name = "fecha_pago")
    @Builder.Default
    private LocalDate fechaPago = LocalDate.now();

    /**
     * Usuario que registró el pago
     */
    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    /**
     * Fecha y hora de registro en el sistema
     */
    @Column(name = "fecha_registro")
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    /**
     * Enum para tipos de items dentro de un rubro
     */
    public enum TipoItem {
        JORNALES,
        MATERIALES,
        GASTOS_GENERALES;

        public static boolean esValido(String tipo) {
            if (tipo == null) return false;
            try {
                TipoItem.valueOf(tipo.toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    /**
     * Enum para métodos de pago
     */
    public enum MetodoPago {
        EFECTIVO,
        TRANSFERENCIA,
        CHEQUE,
        TARJETA,
        OTRO;

        public static boolean esValido(String metodo) {
            if (metodo == null) return false;
            try {
                MetodoPago.valueOf(metodo.toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    @PrePersist
    protected void onCreate() {
        if (fechaPago == null) {
            fechaPago = LocalDate.now();
        }
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (metodoPago == null) {
            metodoPago = "EFECTIVO";
        }
    }
}
