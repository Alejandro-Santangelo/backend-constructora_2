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
import java.time.LocalDateTime;

/**
 * Entidad AsignacionCobroObra
 * 
 * Permite distribuir un cobro entre múltiples obras.
 * Cada asignación puede tener su propia distribución por items (profesionales, materiales, gastos generales).
 * 
 * IMPORTANTE - ASIGNACIÓN FLEXIBLE:
 * - El monto_asignado puede ser PARCIAL (no es necesario asignar todo el cobro de una vez)
 * - La suma de items (profesionales + materiales + gastos + trabajos extra) puede ser:
 *   1. CERO: monto asignado sin detalle por items (saldo general)
 *   2. MENOR al monto_asignado: permite "saldo a favor" sin distribuir a items específicos
 *   3. IGUAL al monto_asignado: distribución completa por items
 * - Los cobros pueden asignarse en múltiples operaciones (ejemplo: cobrar $10M, asignar $5M ahora, $5M después)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asignaciones_cobro_obra", indexes = {
    @Index(name = "idx_asignaciones_cobro_obra", columnList = "cobro_obra_id"),
    @Index(name = "idx_asignaciones_obra", columnList = "obra_id"),
    @Index(name = "idx_asignaciones_empresa", columnList = "empresa_id"),
    @Index(name = "idx_asignaciones_estado", columnList = "estado"),
    @Index(name = "idx_asignaciones_presupuesto", columnList = "presupuesto_no_cliente_id")
})
public class AsignacionCobroObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cobro_obra_id", nullable = false)
    @NotNull(message = "El cobro es obligatorio")
    private CobroObra cobroObra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    @NotNull(message = "La obra es obligatoria")
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id")
    private PresupuestoNoCliente presupuestoNoCliente;

    @NotNull(message = "La empresa es obligatoria")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    // Monto asignado de este cobro a esta obra
    @NotNull(message = "El monto asignado es obligatorio")
    @Positive(message = "El monto asignado debe ser mayor a cero")
    @Column(name = "monto_asignado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoAsignado;

    // Distribución por ítems - TODOS OPCIONALES
    // Pueden ser NULL si no se asigna a ese item específico
    @Column(name = "monto_profesionales", precision = 15, scale = 2)
    private BigDecimal montoProfesionales;

    @Column(name = "monto_materiales", precision = 15, scale = 2)
    private BigDecimal montoMateriales;

    @Column(name = "monto_gastos_generales", precision = 15, scale = 2)
    private BigDecimal montoGastosGenerales;

    @Column(name = "monto_trabajos_extra", precision = 15, scale = 2)
    private BigDecimal montoTrabajosExtra;

    @Column(name = "porcentaje_profesionales", precision = 5, scale = 2)
    private BigDecimal porcentajeProfesionales;

    @Column(name = "porcentaje_materiales", precision = 5, scale = 2)
    private BigDecimal porcentajeMateriales;

    @Column(name = "porcentaje_gastos_generales", precision = 5, scale = 2)
    private BigDecimal porcentajeGastosGenerales;

    @Column(name = "porcentaje_trabajos_extra", precision = 5, scale = 2)
    private BigDecimal porcentajeTrabajosExtra;

    // Estado y metadatos
    @NotNull(message = "El estado es obligatorio")
    @Column(name = "estado", length = 20, nullable = false)
    private String estado = ESTADO_ACTIVA;

    @Column(name = "observaciones", columnDefinition = "TEXT")
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

    // Constantes de estado
    public static final String ESTADO_ACTIVA = "ACTIVA";
    public static final String ESTADO_ANULADA = "ANULADA";

    /**
     * Verifica si la suma de distribución por items coincide con el monto asignado
     */
    public boolean validarDistribucion() {
        BigDecimal total = BigDecimal.ZERO;
        
        if (montoProfesionales != null) {
            total = total.add(montoProfesionales);
        }
        if (montoMateriales != null) {
            total = total.add(montoMateriales);
        }
        if (montoGastosGenerales != null) {
            total = total.add(montoGastosGenerales);
        }
        if (montoTrabajosExtra != null) {
            total = total.add(montoTrabajosExtra);
        }
        
        // Si no hay distribución, es válido (se asigna el monto general sin detallar)
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        
        // CAMBIO: Permitir asignaciones parciales - la suma puede ser MENOR O IGUAL al monto asignado
        // Esto permite dejar "saldo a favor" sin asignar a ítems específicos
        return total.compareTo(montoAsignado) <= 0;
    }

    /**
     * Marca la asignación como anulada
     */
    public void anular() {
        this.estado = ESTADO_ANULADA;
        this.fechaModificacion = LocalDateTime.now();
    }

    /**
     * Verifica si la asignación está activa
     */
    public boolean estaActiva() {
        return ESTADO_ACTIVA.equals(this.estado);
    }

    /**
     * Calcula el saldo sin asignar a ítems específicos
     * (monto asignado - suma de distribución por ítems)
     * 
     * @return saldo disponible dentro de esta asignación para distribuir a ítems
     */
    public BigDecimal calcularSaldoSinDistribuir() {
        BigDecimal totalDistribuido = BigDecimal.ZERO;
        
        if (montoProfesionales != null) {
            totalDistribuido = totalDistribuido.add(montoProfesionales);
        }
        if (montoMateriales != null) {
            totalDistribuido = totalDistribuido.add(montoMateriales);
        }
        if (montoGastosGenerales != null) {
            totalDistribuido = totalDistribuido.add(montoGastosGenerales);
        }
        if (montoTrabajosExtra != null) {
            totalDistribuido = totalDistribuido.add(montoTrabajosExtra);
        }
        
        return montoAsignado.subtract(totalDistribuido);
    }
}
