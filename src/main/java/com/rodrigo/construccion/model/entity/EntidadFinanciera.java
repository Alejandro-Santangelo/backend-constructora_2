package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.TipoEntidadFinanciera;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Registro unificado de entidades financieras.
 *
 * Actúa como puente polimórfico entre las 4 entidades del sistema
 * (obras principales, obras independientes, trabajos extra, trabajos adicionales)
 * y el sistema de cobros / estadísticas.
 *
 * Reglas clave:
 * - La combinación (empresa_id, tipo_entidad, entidad_id) es única.
 * - presupuesto_no_cliente_id es opcional: solo aplica a OBRA_PRINCIPAL y TRABAJO_EXTRA.
 * - Para eliminar: usar activo=false (soft-delete), nunca borrar físicamente.
 *
 * @see CobroEntidad
 */
@Entity
@Table(
    name = "entidades_financieras",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_entidad_financiera",
            columnNames = {"empresa_id", "tipo_entidad", "entidad_id"}
        )
    },
    indexes = {
        @Index(name = "idx_ef_empresa_id",    columnList = "empresa_id"),
        @Index(name = "idx_ef_tipo_entidad",  columnList = "tipo_entidad"),
        @Index(name = "idx_ef_empresa_tipo",  columnList = "empresa_id, tipo_entidad")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntidadFinanciera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la empresa propietaria de esta entidad. */
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    /** Tipo de entidad original (OBRA_PRINCIPAL, OBRA_INDEPENDIENTE, etc.). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entidad", nullable = false, length = 50)
    private TipoEntidadFinanciera tipoEntidad;

    /** ID de la entidad original en su tabla correspondiente. */
    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    /**
     * Referencia al presupuesto asociado.
     * Solo para OBRA_PRINCIPAL y TRABAJO_EXTRA.
     */
    @Column(name = "presupuesto_no_cliente_id")
    private Long presupuestoNoClienteId;

    /** Nombre cacheado de la entidad para listados sin joins adicionales. */
    @Column(name = "nombre_display", length = 300)
    private String nombreDisplay;

    /** Soft-delete: false = eliminada, true = activa. */
    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    /** Cobros registrados contra esta entidad financiera. */
    @OneToMany(mappedBy = "entidadFinanciera", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CobroEntidad> cobros = new ArrayList<>();
}
