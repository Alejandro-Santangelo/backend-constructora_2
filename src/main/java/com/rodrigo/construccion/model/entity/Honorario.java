package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * Representa los honorarios pagados a profesionales por su trabajo en obras.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "honorarios", indexes = {
        @Index(name = "idx_honorarios_obra", columnList = "id_obra")
})
@Filter(name = "empresaFilter", condition = "EXISTS (SELECT 1 FROM obras o JOIN cliente_empresa ce ON o.id_cliente = ce.id_cliente WHERE o.id_obra = id_obra AND ce.id_empresa = :empresaId)")
public class Honorario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_honorario")
    private Long id;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Relaciones
    @JsonBackReference("obra-honorarios")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_obra", nullable = false)
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesional")
    private Profesional profesional;

    // Getters de conveniencia
    public Long getObraId() {
        return obra != null ? obra.getId() : null;
    }

    public String getNombreObra() {
        return obra != null ? obra.getNombre() : null;
    }

    public Long getProfesionalId() {
        return profesional != null ? profesional.getId() : null;
    }

    public String getNombreProfesional() {
        return profesional != null ? profesional.getNombre() : null;
    }
}
