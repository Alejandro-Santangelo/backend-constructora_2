package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad para gestionar trabajos adicionales asociados a obras.
 * 
 * REGLAS DE VINCULACIÓN:
 * - obraId: SIEMPRE obligatorio (obra padre)
 * - trabajoExtraId: Opcional
 *   - null: Trabajo adicional creado directamente desde la obra
 *   - valor: Trabajo adicional creado desde un trabajo extra de la obra
 * 
 * Esto permite trazabilidad: Obra Padre → [Trabajo Extra] → Trabajo Adicional
 * 
 * Estados posibles: PENDIENTE, EN_PROGRESO, COMPLETADO, CANCELADO
 */
@Entity
@Table(name = "trabajos_adicionales", indexes = {
        @Index(name = "idx_trabajos_adicionales_obra", columnList = "obra_id"),
        @Index(name = "idx_trabajos_adicionales_trabajo_extra", columnList = "trabajo_extra_id"),
        @Index(name = "idx_trabajos_adicionales_empresa", columnList = "empresa_id"),
        @Index(name = "idx_trabajos_adicionales_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoAdicional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "El nombre del trabajo adicional es obligatorio")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser mayor a cero")
    @Column(name = "importe", precision = 15, scale = 2, nullable = false)
    private BigDecimal importe;

    @Column(name = "importe_jornales", precision = 15, scale = 2)
    private BigDecimal importeJornales;

    @Column(name = "importe_materiales", precision = 15, scale = 2)
    private BigDecimal importeMateriales;

    @Column(name = "importe_honorarios", precision = 15, scale = 2)
    private BigDecimal importeHonorarios;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorarios", length = 20)
    private String tipoHonorarios;

    @Column(name = "importe_mayores_costos", precision = 15, scale = 2)
    private BigDecimal importeMayoresCostos;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_mayores_costos", length = 20)
    private String tipoMayoresCostos;

    @NotNull(message = "Los días necesarios son obligatorios")
    @Positive(message = "Los días necesarios deben ser al menos 1")
    @Column(name = "dias_necesarios", nullable = false)
    private Integer diasNecesarios;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    /**
     * ID de la obra padre (SIEMPRE obligatorio)
     * Todo trabajo adicional pertenece a una obra
     */
    @NotNull(message = "El ID de la obra es obligatorio")
    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    /**
     * ID del trabajo extra asociado (OPCIONAL)
     * - null: Trabajo adicional creado directamente desde la obra
     * - valor: Trabajo adicional creado desde un trabajo extra
     * Si tiene valor, ese trabajo extra debe pertenecer a la obra indicada en obraId
     */
    @Column(name = "trabajo_extra_id")
    private Long trabajoExtraId;

    @NotNull(message = "El ID de la empresa es obligatorio")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Builder.Default
    @Column(name = "estado", length = 50, nullable = false)
    private String estado = "PENDIENTE";

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /**
     * Lista de profesionales asignados al trabajo adicional
     * Cascada ALL para que se guarden/actualicen/eliminen automáticamente
     */
    @OneToMany(mappedBy = "trabajoAdicional", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<TrabajoAdicionalProfesional> profesionales = new ArrayList<>();

    /**
     * Método helper para agregar profesionales manteniendo la relación bidireccional
     */
    public void addProfesional(TrabajoAdicionalProfesional profesional) {
        profesionales.add(profesional);
        profesional.setTrabajoAdicional(this);
    }

    /**
     * Método helper para remover profesionales manteniendo la relación bidireccional
     */
    public void removeProfesional(TrabajoAdicionalProfesional profesional) {
        profesionales.remove(profesional);
        profesional.setTrabajoAdicional(null);
    }

    /**
     * Limpiar todos los profesionales
     */
    public void clearProfesionales() {
        if (profesionales != null) {
            profesionales.forEach(p -> p.setTrabajoAdicional(null));
            profesionales.clear();
        }
    }
}
