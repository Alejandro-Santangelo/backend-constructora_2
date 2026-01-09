package com.rodrigo.construccion.model.entity;

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

/* Entidad Jornal - Representa las horas trabajadas por un profesional en una obra específica.*/
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "jornales", indexes = {
        @Index(name = "idx_jornales_asignacion", columnList = "id_asignacion"),
        @Index(name = "idx_jornales_fecha", columnList = "fecha")
})
@Filter(name = "empresaFilter", condition = "EXISTS (SELECT 1 FROM asignaciones_profesional_obra apo JOIN obras o ON apo.obra_id = o.id_obra JOIN cliente_empresa ce ON o.id_cliente = ce.id_cliente WHERE apo.id = id_asignacion AND ce.id_empresa = :empresaId)")
public class Jornal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jornal")
    private Long id;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "Las horas trabajadas son obligatorias")
    @Positive(message = "Las horas trabajadas deben ser mayor a cero")
    @Column(name = "horas_trabajadas", nullable = false, precision = 5, scale = 2)
    private BigDecimal horasTrabajadas;

    @NotNull(message = "El valor por hora es obligatorio")
    @Positive(message = "El valor por hora debe ser mayor a cero")
    @Column(name = "valor_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_asignacion", nullable = false)
    private ProfesionalObra asignacion;

    @Column(name = "incluir_en_calculo_dias")
    private Boolean incluirEnCalculoDias = false;

    // Métodos calculados
    public BigDecimal getMontoTotal() {
        if (horasTrabajadas != null && valorHora != null) {
            return horasTrabajadas.multiply(valorHora);
        }
        return BigDecimal.ZERO;
    }

    // Getters de conveniencia
    public Long getAsignacionId() {
        return asignacion != null ? asignacion.getId() : null;
    }

    public Long getProfesionalId() {
        return asignacion != null ? asignacion.getProfesionalId() : null;
    }

    // NOTA: Ya no hay obraId, solo dirección de obra
    public String getDireccionObra() {
        return asignacion != null ? asignacion.getDireccionObraCompleta() : null;
    }

    public Boolean getIncluirEnCalculoDias() {
        return incluirEnCalculoDias;
    }

    public void setIncluirEnCalculoDias(Boolean incluirEnCalculoDias) {
        this.incluirEnCalculoDias = incluirEnCalculoDias;
    }
}
