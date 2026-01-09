package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad GastoObraProfesional
 * 
 * Registra cada gasto que hace un profesional usando la caja chica asignada.
 * Al crear un gasto:
 * - Se valida que hay saldo suficiente
 * - Se descuenta del saldo_disponible del ProfesionalObra
 * - Se crea automáticamente un registro en Otros Costos del presupuesto de la obra
 */
@Entity
@Table(name = "gastos_obra_profesional", indexes = {
    @Index(name = "idx_gastos_profesional", columnList = "profesional_obra_id"),
    @Index(name = "idx_gastos_empresa", columnList = "empresa_id"),
    @Index(name = "idx_gastos_fecha", columnList = "fecha_hora")
})
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GastoObraProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "El profesional obra es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_obra_id", nullable = false)
    private ProfesionalObra profesionalObra;

    @NotNull(message = "El monto es obligatorio")
    @PositiveOrZero(message = "El monto debe ser mayor o igual a cero")
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "foto_ticket", columnDefinition = "TEXT")
    private String fotoTicket; // Base64 o URL

    @NotNull(message = "El ID de empresa es obligatorio")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Getters de conveniencia
    public Long getProfesionalObraId() {
        return profesionalObra != null ? profesionalObra.getId() : null;
    }

    public String getNombreProfesional() {
        return profesionalObra != null ? profesionalObra.getNombreProfesional() : null;
    }

    public String getDireccionObra() {
        return profesionalObra != null ? profesionalObra.getDireccionObraCompleta() : null;
    }

    /**
     * Hook pre-persist para asegurar que fechaHora se genera automáticamente
     */
    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}
