package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad para profesionales asignados a un trabajo adicional.
 * Puede representar:
 * - Profesional registrado: esRegistrado=true, profesionalId tiene valor
 * - Profesional ad-hoc: esRegistrado=false, profesionalId=null, todos los datos en esta tabla
 */
@Entity
@Table(name = "trabajos_adicionales_profesionales", indexes = {
        @Index(name = "idx_ta_profesionales_trabajo", columnList = "trabajo_adicional_id"),
        @Index(name = "idx_ta_profesionales_profesional", columnList = "profesional_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoAdicionalProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Relación con el trabajo adicional
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_adicional_id", nullable = false)
    @JsonBackReference
    @NotNull(message = "El trabajo adicional es obligatorio")
    private TrabajoAdicional trabajoAdicional;

    /**
     * ID del profesional si es registrado (de la tabla profesionales)
     * NULL si es profesional ad-hoc
     */
    @Column(name = "profesional_id")
    private Long profesionalId;

    /**
     * Nombre del profesional (requerido para ad-hoc, opcional para registrados)
     */
    @NotBlank(message = "El nombre del profesional es obligatorio")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    /**
     * Tipo/especialidad del profesional (requerido para ad-hoc, opcional para registrados)
     */
    @NotBlank(message = "El tipo de profesional es obligatorio")
    @Column(name = "tipo_profesional", length = 100, nullable = false)
    private String tipoProfesional;

    /**
     * Honorario por día (para ad-hoc principalmente, puede ser sobrescrito en registrados)
     */
    @Column(name = "honorario_dia", precision = 10, scale = 2)
    private BigDecimal honorarioDia;

    /**
     * Teléfono de contacto (opcional)
     */
    @Column(name = "telefono", length = 50)
    private String telefono;

    /**
     * Email de contacto (opcional)
     */
    @Column(name = "email")
    private String email;

    /**
     * Indica si es un profesional registrado en el catálogo (true) o ad-hoc (false)
     */
    @Builder.Default
    @Column(name = "es_registrado", nullable = false)
    private Boolean esRegistrado = true;

    @CreationTimestamp
    @Column(name = "fecha_asignacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;
}
