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
 * - trabajoAdicionalPadreId: Opcional (NUEVA FUNCIONALIDAD - Anidación Recursiva)
 *   - null: Trabajo adicional raíz (sin padre adicional)
 *   - valor: Trabajo adicional hijo de otro trabajo adicional
 * 
 * JERARQUÍAS SOPORTADAS:
 * 1. Obra → Trabajo Adicional (directo)
 * 2. Obra → Trabajo Extra → Trabajo Adicional
 * 3. Obra → Trabajo Adicional Padre → Trabajo Adicional Hijo (NUEVO)
 * 4. Obra → Trabajo Extra → Trabajo Adicional Padre → Trabajo Adicional Hijo (NUEVO)
 * 
 * Nota: Un trabajo adicional hijo heredará automáticamente obraId y empresaId del padre.
 * 
 * Estados posibles: BORRADOR, PENDIENTE, EN_PROGRESO, COMPLETADO, CANCELADO
 * - BORRADOR: Permite edición libre (estado inicial)
 * - PENDIENTE: Listo para iniciar pero no comenzado
 * - EN_PROGRESO: En ejecución activa
 * - COMPLETADO: Finalizado exitosamente
 * - CANCELADO: Cancelado o descartado
 */
@Entity
@Table(name = "trabajos_adicionales", indexes = {
        @Index(name = "idx_trabajos_adicionales_obra", columnList = "obra_id"),
        @Index(name = "idx_trabajos_adicionales_trabajo_extra", columnList = "trabajo_extra_id"),
        @Index(name = "idx_trabajos_adicionales_empresa", columnList = "empresa_id"),
        @Index(name = "idx_trabajos_adicionales_estado", columnList = "estado"),
        @Index(name = "idx_trabajos_adicionales_padre", columnList = "trabajo_adicional_padre_id")
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

    @Column(name = "importe_gastos_generales", precision = 15, scale = 2)
    private BigDecimal importeGastosGenerales;

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

    // ========== HONORARIOS INDIVIDUALES POR CATEGORÍA (SISTEMA NUEVO) ==========
    
    @Column(name = "honorario_jornales", precision = 15, scale = 2)
    private BigDecimal honorarioJornales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorario_jornales", length = 10)
    private String tipoHonorarioJornales;

    @Column(name = "honorario_materiales", precision = 15, scale = 2)
    private BigDecimal honorarioMateriales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorario_materiales", length = 10)
    private String tipoHonorarioMateriales;

    @Column(name = "honorario_gastos_generales", precision = 15, scale = 2)
    private BigDecimal honorarioGastosGenerales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorario_gastos_generales", length = 10)
    private String tipoHonorarioGastosGenerales;

    @Column(name = "honorario_mayores_costos", precision = 15, scale = 2)
    private BigDecimal honorarioMayoresCostos;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorario_mayores_costos", length = 10)
    private String tipoHonorarioMayoresCostos;

    // ========== DESCUENTOS SOBRE IMPORTES BASE POR CATEGORÍA ==========

    @Column(name = "descuento_jornales", precision = 15, scale = 2)
    private BigDecimal descuentoJornales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_jornales", length = 10)
    private String tipoDescuentoJornales;

    @Column(name = "descuento_materiales", precision = 15, scale = 2)
    private BigDecimal descuentoMateriales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_materiales", length = 10)
    private String tipoDescuentoMateriales;

    @Column(name = "descuento_gastos_generales", precision = 15, scale = 2)
    private BigDecimal descuentoGastosGenerales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_gastos_generales", length = 10)
    private String tipoDescuentoGastosGenerales;

    @Column(name = "descuento_mayores_costos", precision = 15, scale = 2)
    private BigDecimal descuentoMayoresCostos;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_mayores_costos", length = 10)
    private String tipoDescuentoMayoresCostos;

    // ========== DESCUENTOS SOBRE HONORARIOS POR CATEGORÍA (NUEVOS) ==========

    @Column(name = "descuento_honorario_jornales", precision = 15, scale = 2)
    private BigDecimal descuentoHonorarioJornales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_honorario_jornales", length = 10)
    private String tipoDescuentoHonorarioJornales;

    @Column(name = "descuento_honorario_materiales", precision = 15, scale = 2)
    private BigDecimal descuentoHonorarioMateriales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_honorario_materiales", length = 10)
    private String tipoDescuentoHonorarioMateriales;

    @Column(name = "descuento_honorario_gastos_generales", precision = 15, scale = 2)
    private BigDecimal descuentoHonorarioGastosGenerales;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_honorario_gastos_generales", length = 10)
    private String tipoDescuentoHonorarioGastosGenerales;

    @Column(name = "descuento_honorario_mayores_costos", precision = 15, scale = 2)
    private BigDecimal descuentoHonorarioMayoresCostos;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_honorario_mayores_costos", length = 10)
    private String tipoDescuentoHonorarioMayoresCostos;

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

    /**
     * ID del trabajo adicional padre (OPCIONAL - NUEVA FUNCIONALIDAD)
     * Permite crear jerarquías de trabajos adicionales anidados.
     * - null: Trabajo adicional raíz (sin padre adicional)
     * - valor: Trabajo adicional hijo de otro trabajo adicional
     * 
     * Cuando tiene valor, hereda automáticamente:
     * - obraId del padre
     * - empresaId del padre
     * - trabajoExtraId del padre (si aplica)
     * 
     * IMPORTANTE: No puede tener valores en trabajoExtraId Y trabajoAdicionalPadreId simultáneamente.
     * La vinculación es: trabajoExtraId OR trabajoAdicionalPadreId (excluyente).
     */
    @Column(name = "trabajo_adicional_padre_id")
    private Long trabajoAdicionalPadreId;

    @NotNull(message = "El ID de la empresa es obligatorio")
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Builder.Default
    @Column(name = "estado", length = 50, nullable = false)
    private String estado = ESTADO_BORRADOR; // Inicia como borrador para permitir edición por etapas

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

    // ========== RELACIONES RECURSIVAS PARA ANIDACIÓN (NUEVA FUNCIONALIDAD) ==========
    
    /**
     * Referencia al trabajo adicional padre (si este es un trabajo adicional hijo)
     * Relación Many-to-One: muchos trabajos adicionales pueden tener el mismo padre
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_adicional_padre_id", insertable = false, updatable = false)
    private TrabajoAdicional trabajoAdicionalPadre;

    /**
     * Lista de trabajos adicionales hijos de este trabajo adicional
     * Permite crear estructuras jerárquicas de trabajos adicionales
     * Relación One-to-Many: un trabajo adicional puede tener múltiples hijos
     */
    @OneToMany(mappedBy = "trabajoAdicionalPadre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrabajoAdicional> trabajosAdicionalesHijos = new ArrayList<>();

    // ========== RELACIÓN CON PRESUPUESTOS TAREA LEVE (NUEVA FUNCIONALIDAD) ==========
    
    /**
     * Lista de presupuestos tipo TAREA_LEVE asociados a este trabajo adicional
     * Permite que un trabajo adicional (incluyendo los anidados) tenga múltiples tareas leves
     * Relación One-to-Many: un trabajo adicional puede tener múltiples presupuestos TAREA_LEVE
     * 
     * IMPORTANTE: Solo presupuestos con tipoPresupuesto = TAREA_LEVE deben usarse aquí
     */
    @OneToMany(mappedBy = "trabajoAdicional", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PresupuestoNoCliente> presupuestosTareasLeves = new ArrayList<>();

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

    // ========== MÉTODOS HELPER PARA ANIDACIÓN (NUEVA FUNCIONALIDAD) ==========
    
    /**
     * Método helper para agregar un trabajo adicional hijo manteniendo la relación bidireccional
     * @param hijo el trabajo adicional hijo a agregar
     */
    public void addTrabajoAdicionalHijo(TrabajoAdicional hijo) {
        trabajosAdicionalesHijos.add(hijo);
        hijo.setTrabajoAdicionalPadre(this);
        hijo.setTrabajoAdicionalPadreId(this.id);
    }

    /**
     * Método helper para remover un trabajo adicional hijo manteniendo la relación bidireccional
     * @param hijo el trabajo adicional hijo a remover
     */
    public void removeTrabajoAdicionalHijo(TrabajoAdicional hijo) {
        trabajosAdicionalesHijos.remove(hijo);
        hijo.setTrabajoAdicionalPadre(null);
        hijo.setTrabajoAdicionalPadreId(null);
    }

    /**
     * Limpiar todos los trabajos adicionales hijos
     */
    public void clearTrabajosAdicionalesHijos() {
        if (trabajosAdicionalesHijos != null) {
            trabajosAdicionalesHijos.forEach(h -> {
                h.setTrabajoAdicionalPadre(null);
                h.setTrabajoAdicionalPadreId(null);
            });
            trabajosAdicionalesHijos.clear();
        }
    }

    /**
     * Verifica si este trabajo adicional tiene un padre (es hijo de otro trabajo adicional)
     * @return true si tiene un padre
     */
    @Transient
    public boolean tieneTrabajoAdicionalPadre() {
        return trabajoAdicionalPadreId != null;
    }

    /**
     * Verifica si este trabajo adicional tiene hijos
     * @return true si tiene al menos un hijo
     */
    @Transient
    public boolean tieneTrabajosAdicionalesHijos() {
        return trabajosAdicionalesHijos != null && !trabajosAdicionalesHijos.isEmpty();
    }

    // ========== MÉTODOS HELPER PARA PRESUPUESTOS TAREA LEVE (NUEVA FUNCIONALIDAD) ==========
    
    /**
     * Método helper para agregar un presupuesto TAREA_LEVE manteniendo la relación bidireccional
     * @param presupuesto el presupuesto TAREA_LEVE a agregar
     */
    public void addPresupuestoTareaLeve(PresupuestoNoCliente presupuesto) {
        presupuestosTareasLeves.add(presupuesto);
        presupuesto.setTrabajoAdicional(this);
    }

    /**
     * Método helper para remover un presupuesto TAREA_LEVE manteniendo la relación bidireccional
     * @param presupuesto el presupuesto TAREA_LEVE a remover
     */
    public void removePresupuestoTareaLeve(PresupuestoNoCliente presupuesto) {
        presupuestosTareasLeves.remove(presupuesto);
        presupuesto.setTrabajoAdicional(null);
    }

    /**
     * Limpiar todos los presupuestos TAREA_LEVE
     */
    public void clearPresupuestosTareasLeves() {
        if (presupuestosTareasLeves != null) {
            presupuestosTareasLeves.forEach(p -> p.setTrabajoAdicional(null));
            presupuestosTareasLeves.clear();
        }
    }

    /**
     * Verifica si este trabajo adicional tiene presupuestos TAREA_LEVE asociados
     * @return true si tiene al menos un presupuesto TAREA_LEVE
     */
    @Transient
    public boolean tienePresupuestosTareasLeves() {
        return presupuestosTareasLeves != null && !presupuestosTareasLeves.isEmpty();
    }

    /**
     * Obtiene la cantidad de presupuestos TAREA_LEVE asociados
     * @return cantidad de presupuestos TAREA_LEVE
     */
    @Transient
    public int getCantidadPresupuestosTareasLeves() {
        return presupuestosTareasLeves != null ? presupuestosTareasLeves.size() : 0;
    }

    // === CONSTANTES DE ESTADO ===
    public static final String ESTADO_BORRADOR = "BORRADOR";
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_EN_PROGRESO = "EN_PROGRESO";
    public static final String ESTADO_COMPLETADO = "COMPLETADO";
    public static final String ESTADO_CANCELADO = "CANCELADO";

    /**
     * Verifica si el trabajo adicional está en estado BORRADOR.
     * Un trabajo adicional borrador permite modificaciones sin restricciones.
     * @return true si el trabajo adicional está en estado BORRADOR
     */
    @Transient
    public boolean esBorrador() {
        return ESTADO_BORRADOR.equals(this.estado);
    }

    /**
     * Verifica si el trabajo adicional está pendiente.
     * @return true si está en estado PENDIENTE
     */
    @Transient  
    public boolean estaPendiente() {
        return ESTADO_PENDIENTE.equals(this.estado);
    }

    /**
     * Verifica si el trabajo adicional puede ser editado.
     * Permite edición en estados BORRADOR y PENDIENTE.
     * @return true si es editable
     */
    @Transient
    public boolean esEditable() {
        return ESTADO_BORRADOR.equals(this.estado) || ESTADO_PENDIENTE.equals(this.estado);
    }
}
