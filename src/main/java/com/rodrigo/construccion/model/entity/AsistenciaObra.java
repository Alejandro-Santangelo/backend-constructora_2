package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/*
 * Control de presencia con geolocalización para calcular horas trabajadas.
 * Constraint: Solo puede haber 1 registro por profesional por día.
 */
@Entity
@Table(name = "asistencia_obra", 
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_asistencia_dia", columnNames = {"profesional_obra_id", "fecha"})
    },
    indexes = {
        @Index(name = "idx_asistencia_profesional", columnList = "profesional_obra_id"),
        @Index(name = "idx_asistencia_empresa", columnList = "empresa_id"),
        @Index(name = "idx_asistencia_fecha", columnList = "fecha")
    }
)
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "El profesional obra es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_obra_id", nullable = false)
    private ProfesionalObra profesionalObra;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "latitud_entrada")
    private Double latitudEntrada;

    @Column(name = "longitud_entrada")
    private Double longitudEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "latitud_salida")
    private Double latitudSalida;

    @Column(name = "longitud_salida")
    private Double longitudSalida;

    @PositiveOrZero(message = "Las horas trabajadas deben ser mayor o igual a cero")
    @Column(name = "horas_trabajadas", precision = 5, scale = 2)
    private BigDecimal horasTrabajadas;

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
     * Calcula automáticamente las horas trabajadas basándose en hora_entrada y hora_salida.
     * El resultado es en formato decimal (ej: 8.5 horas para 8 horas y 30 minutos)
     */
    public void calcularHorasTrabajadas() {
        if (horaEntrada != null && horaSalida != null) {
            Duration duration = Duration.between(horaEntrada, horaSalida);
            long minutos = duration.toMinutes();
            
            // Convertir minutos a horas decimales
            BigDecimal horas = BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            
            this.horasTrabajadas = horas;
        }
    }

    /**
     * Verifica si el check-in está completo (tiene hora de entrada)
     */
    @Transient
    public boolean tieneCheckIn() {
        return horaEntrada != null;
    }

    /**
     * Verifica si el check-out está completo (tiene hora de salida)
     */
    @Transient
    public boolean tieneCheckOut() {
        return horaSalida != null;
    }
}
