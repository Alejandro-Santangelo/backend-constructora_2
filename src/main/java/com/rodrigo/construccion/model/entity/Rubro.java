package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad Rubro
 * Catálogo maestro de rubros de construcción reutilizables entre presupuestos.
 * Permite estandarizar nombres y categorías de rubros.
 */
@Entity
@Table(name = "rubros", indexes = {
        @Index(name = "idx_rubros_nombre", columnList = "nombre"),
        @Index(name = "idx_rubros_activo", columnList = "activo"),
        @Index(name = "idx_rubros_categoria", columnList = "categoria")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rubro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El nombre del rubro es obligatorio")
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Categoría del rubro: 'estructura', 'instalaciones', 'terminaciones', 'servicios', 'otros'
     */
    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "modificado_en")
    private LocalDateTime modificadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        modificadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modificadoEn = LocalDateTime.now();
    }
}
