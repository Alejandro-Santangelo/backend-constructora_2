package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cobro registrado contra una {@link EntidadFinanciera}.
 *
 * Sistema simplificado de cobros para obras independientes (manuales) y
 * trabajos adicionales. Para estadísticas de obras principales y trabajos extra,
 * se consultan los sistemas existentes (CobroObra, etc.).
 *
 * Restricciones:
 * - monto debe ser positivo (validado en BD y servicio).
 * - Si se elimina la entidad financiera padre, los cobros se eliminan en cascada.
 */
@Entity
@Table(
    name = "cobros_entidad",
    indexes = {
        @Index(name = "idx_ce_entidad_financiera", columnList = "entidad_financiera_id"),
        @Index(name = "idx_ce_empresa_id",         columnList = "empresa_id"),
        @Index(name = "idx_ce_fecha_cobro",        columnList = "fecha_cobro")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CobroEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Entidad financiera a la que pertenece este cobro. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entidad_financiera_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_cobro_entidad_financiera"))
    private EntidadFinanciera entidadFinanciera;

    /** Empresa propietaria (redundante para queries sin join). */
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    /** Importe cobrado. Debe ser > 0. */
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    /** Fecha efectiva del cobro. */
    @Column(name = "fecha_cobro", nullable = false)
    private LocalDate fechaCobro;

    /** Método de pago (TRANSFERENCIA, EFECTIVO, CHEQUE, etc.). */
    @Column(name = "metodo_pago", length = 100)
    private String metodoPago;

    /** Número de referencia, comprobante o cheque. */
    @Column(name = "referencia", length = 200)
    private String referencia;

    /** Notas adicionales del cobro. */
    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    /** Usuario que registró el cobro. */
    @Column(name = "creado_por", length = 150)
    private String creadoPor;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
}
