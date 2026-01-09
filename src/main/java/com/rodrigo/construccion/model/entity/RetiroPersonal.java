package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad RetiroPersonal
 * 
 * Registra los retiros de dinero del propietario de la empresa constructora.
 * El monto retirado se descuenta del saldo disponible (cobros - asignaciones - retiros).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "retiros_personales", indexes = {
    @Index(name = "idx_retiros_empresa_id", columnList = "empresa_id"),
    @Index(name = "idx_retiros_fecha_retiro", columnList = "fecha_retiro"),
    @Index(name = "idx_retiros_estado", columnList = "estado"),
    @Index(name = "idx_retiros_tipo", columnList = "tipo_retiro"),
    @Index(name = "idx_retiros_empresa_estado", columnList = "empresa_id, estado")
})
public class RetiroPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "obra_id")
    private Long obraId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @NotNull(message = "La fecha de retiro es obligatoria")
    @Column(name = "fecha_retiro", nullable = false)
    private LocalDate fechaRetiro;

    @Column(length = 500)
    private String motivo;

    @Column(name = "tipo_retiro", length = 50)
    private String tipoRetiro = TIPO_GANANCIA;

    @NotNull(message = "El estado es obligatorio")
    @Column(length = 20, nullable = false)
    private String estado = ESTADO_ACTIVO;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // Auditoría
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "usuario_creacion_id")
    private Long usuarioCreacionId;

    @Column(name = "usuario_modificacion_id")
    private Long usuarioModificacionId;

    // ========== CONSTANTES DE ESTADO ==========
    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_ANULADO = "ANULADO";

    // ========== CONSTANTES DE TIPO RETIRO ==========
    public static final String TIPO_GANANCIA = "GANANCIA";
    public static final String TIPO_PRESTAMO = "PRESTAMO";
    public static final String TIPO_GASTO_PERSONAL = "GASTO_PERSONAL";

    // ========== MÉTODOS DE UTILIDAD ==========

    public boolean estaActivo() {
        return ESTADO_ACTIVO.equals(this.estado);
    }

    public boolean estaAnulado() {
        return ESTADO_ANULADO.equals(this.estado);
    }

    public void anular() {
        this.estado = ESTADO_ANULADO;
        this.fechaModificacion = LocalDateTime.now();
    }

    public boolean puedeSerAnulado() {
        return ESTADO_ACTIVO.equals(this.estado);
    }

    public boolean puedeSerEliminado() {
        return ESTADO_ACTIVO.equals(this.estado);
    }

    // ========== HOOKS DE JPA ==========

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaModificacion == null) {
            fechaModificacion = LocalDateTime.now();
        }
        if (estado == null || estado.isEmpty()) {
            estado = ESTADO_ACTIVO;
        }
        if (tipoRetiro == null || tipoRetiro.isEmpty()) {
            tipoRetiro = TIPO_GANANCIA;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
