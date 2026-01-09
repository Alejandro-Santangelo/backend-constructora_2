package com.rodrigo.construccion.model.entity;

import com.rodrigo.construccion.enums.EstadoPagoTrabajoExtra;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad TrabajoExtra
 * Representa mini-presupuestos diarios (trabajos extra) realizados en una obra
 */
@Entity
@Table(name = "trabajos_extra", indexes = {
    @Index(name = "idx_trabajos_extra_obra", columnList = "obra_id"),
    @Index(name = "idx_trabajos_extra_empresa", columnList = "empresa_id"),
    @Index(name = "idx_trabajos_extra_cliente", columnList = "cliente_id"),
    @Index(name = "idx_trabajos_extra_obra_empresa", columnList = "obra_id, empresa_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trabajo_extra")
    private Long id;

    @Column(name = "obra_id", nullable = false)
    private Long obraId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nombre", nullable = false, length = 255)
    private String nombre;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago_general", length = 50)
    @Builder.Default
    private EstadoPagoTrabajoExtra estadoPagoGeneral = EstadoPagoTrabajoExtra.PENDIENTE;

    // Datos de contacto del cliente
    @Column(name = "nombre_empresa", length = 255)
    private String nombreEmpresa;

    @Column(name = "nombre_solicitante", length = 255)
    private String nombreSolicitante;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "mail", length = 255)
    private String mail;

    @Column(name = "direccion_particular", length = 500)
    private String direccionParticular;

    // Dirección de la obra (desglosada)
    @Column(name = "direccion_obra_calle", length = 255)
    private String direccionObraCalle;

    @Column(name = "direccion_obra_altura", length = 50)
    private String direccionObraAltura;

    @Column(name = "direccion_obra_barrio", length = 100)
    private String direccionObraBarrio;

    @Column(name = "direccion_obra_torre", length = 50)
    private String direccionObraTorre;

    @Column(name = "direccion_obra_piso", length = 50)
    private String direccionObraPiso;

    @Column(name = "direccion_obra_departamento", length = 50)
    private String direccionObraDepartamento;

    @Column(name = "direccion_obra_localidad", length = 100)
    private String direccionObraLocalidad;

    @Column(name = "direccion_obra_provincia", length = 100)
    private String direccionObraProvincia;

    @Column(name = "direccion_obra_codigo_postal", length = 20)
    private String direccionObraCodigoPostal;

    // Datos del presupuesto
    @Column(name = "nombre_obra", length = 255)
    private String nombreObra;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_probable_inicio")
    private LocalDate fechaProbableInicio;

    @Column(name = "vencimiento")
    private LocalDate vencimiento;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "tiempo_estimado_terminacion")
    private Integer tiempoEstimadoTerminacion;

    @Column(name = "calculo_automatico_dias_habiles")
    @Builder.Default
    private Boolean calculoAutomaticoDiasHabiles = false;

    // Control de versiones y estado
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Column(name = "numero_presupuesto", length = 50)
    private String numeroPresupuesto;

    @Column(name = "estado", length = 50)
    @Builder.Default
    private String estado = "ENVIADO";

    // Totales del presupuesto
    @Column(name = "total_presupuesto", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPresupuesto = BigDecimal.ZERO;

    @Column(name = "total_honorarios", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalHonorarios = BigDecimal.ZERO;

    @Column(name = "total_mayores_costos", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalMayoresCostos = BigDecimal.ZERO;

    @Column(name = "total_final", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalFinal = BigDecimal.ZERO;

    @Column(name = "monto_total", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montoTotal = BigDecimal.ZERO;

    // Marcador de trabajo extra
    @Column(name = "es_trabajo_extra")
    @Builder.Default
    private Boolean esTrabajoExtra = true;

    // ============================================================================
    // CONFIGURACIÓN DE HONORARIOS
    // ============================================================================

    @Column(name = "honorarios_aplicar_a_todos")
    @Builder.Default
    private Boolean honorariosAplicarATodos = false;

    @Column(name = "honorarios_valor_general", precision = 15, scale = 2)
    private BigDecimal honorariosValorGeneral;

    @Column(name = "honorarios_tipo_general", length = 20)
    private String honorariosTipoGeneral;

    // Honorarios por categoría - Jornales
    @Column(name = "honorarios_jornales_activo")
    @Builder.Default
    private Boolean honorariosJornalesActivo = false;

    @Column(name = "honorarios_jornales_valor", precision = 15, scale = 2)
    private BigDecimal honorariosJornalesValor;

    @Column(name = "honorarios_jornales_tipo", length = 20)
    private String honorariosJornalesTipo;

    // Honorarios por categoría - Materiales
    @Column(name = "honorarios_materiales_activo")
    @Builder.Default
    private Boolean honorariosMaterialesActivo = false;

    @Column(name = "honorarios_materiales_valor", precision = 15, scale = 2)
    private BigDecimal honorariosMaterialesValor;

    @Column(name = "honorarios_materiales_tipo", length = 20)
    private String honorariosMaterialesTipo;

    // Honorarios por categoría - Profesionales
    @Column(name = "honorarios_profesionales_activo")
    @Builder.Default
    private Boolean honorariosProfesionalesActivo = false;

    @Column(name = "honorarios_profesionales_valor", precision = 15, scale = 2)
    private BigDecimal honorariosProfesionalesValor;

    @Column(name = "honorarios_profesionales_tipo", length = 20)
    private String honorariosProfesionalesTipo;

    // Honorarios por categoría - Otros Costos
    @Column(name = "honorarios_otros_costos_activo")
    @Builder.Default
    private Boolean honorariosOtrosCostosActivo = false;

    @Column(name = "honorarios_otros_costos_valor", precision = 15, scale = 2)
    private BigDecimal honorariosOtrosCostosValor;

    @Column(name = "honorarios_otros_costos_tipo", length = 20)
    private String honorariosOtrosCostosTipo;

    // Honorarios configuración presupuesto
    @Column(name = "honorarios_configuracion_presupuesto_activo")
    @Builder.Default
    private Boolean honorariosConfiguracionPresupuestoActivo = false;

    @Column(name = "honorarios_configuracion_presupuesto_valor", precision = 10, scale = 2)
    private BigDecimal honorariosConfiguracionPresupuestoValor;

    @Column(name = "honorarios_configuracion_presupuesto_tipo", length = 20)
    private String honorariosConfiguracionPresupuestoTipo;

    // ============================================================================
    // CONFIGURACIÓN DE MAYORES COSTOS
    // ============================================================================

    @Column(name = "mayores_costos_aplicar_valor_general")
    @Builder.Default
    private Boolean mayoresCostosAplicarValorGeneral = false;

    @Column(name = "mayores_costos_valor_general", precision = 15, scale = 2)
    private BigDecimal mayoresCostosValorGeneral;

    @Column(name = "mayores_costos_tipo_general", length = 20)
    private String mayoresCostosTipoGeneral;

    // Mayores Costos por categoría - Jornales
    @Column(name = "mayores_costos_jornales_activo")
    @Builder.Default
    private Boolean mayoresCostosJornalesActivo = false;

    @Column(name = "mayores_costos_jornales_valor", precision = 15, scale = 2)
    private BigDecimal mayoresCostosJornalesValor;

    @Column(name = "mayores_costos_jornales_tipo", length = 20)
    private String mayoresCostosJornalesTipo;

    // Mayores Costos por categoría - Materiales
    @Column(name = "mayores_costos_materiales_activo")
    @Builder.Default
    private Boolean mayoresCostosMaterialesActivo = false;

    @Column(name = "mayores_costos_materiales_valor", precision = 15, scale = 2)
    private BigDecimal mayoresCostosMaterialesValor;

    @Column(name = "mayores_costos_materiales_tipo", length = 20)
    private String mayoresCostosMaterialesTipo;

    // Mayores Costos por categoría - Profesionales
    @Column(name = "mayores_costos_profesionales_activo")
    @Builder.Default
    private Boolean mayoresCostosProfesionalesActivo = false;

    @Column(name = "mayores_costos_profesionales_valor", precision = 15, scale = 2)
    private BigDecimal mayoresCostosProfesionalesValor;

    @Column(name = "mayores_costos_profesionales_tipo", length = 20)
    private String mayoresCostosProfesionalesTipo;

    // Mayores Costos por categoría - Otros Costos
    @Column(name = "mayores_costos_otros_costos_activo")
    @Builder.Default
    private Boolean mayoresCostosOtrosCostosActivo = false;

    @Column(name = "mayores_costos_otros_costos_valor", precision = 15, scale = 2)
    private BigDecimal mayoresCostosOtrosCostosValor;

    @Column(name = "mayores_costos_otros_costos_tipo", length = 20)
    private String mayoresCostosOtrosCostosTipo;

    // Mayores Costos por categoría - Honorarios
    @Column(name = "mayores_costos_honorarios_activo")
    @Builder.Default
    private Boolean mayoresCostosHonorariosActivo = false;

    @Column(name = "mayores_costos_honorarios_valor", precision = 15, scale = 2)
    private BigDecimal mayoresCostosHonorariosValor;

    @Column(name = "mayores_costos_honorarios_tipo", length = 20)
    private String mayoresCostosHonorariosTipo;

    // Mayores Costos configuración presupuesto
    @Column(name = "mayores_costos_configuracion_presupuesto_activo")
    @Builder.Default
    private Boolean mayoresCostosConfiguracionPresupuestoActivo = false;

    @Column(name = "mayores_costos_configuracion_presupuesto_valor", precision = 15, scale = 2)
    private BigDecimal mayoresCostosConfiguracionPresupuestoValor;

    @Column(name = "mayores_costos_configuracion_presupuesto_tipo", length = 20)
    private String mayoresCostosConfiguracionPresupuestoTipo;

    // Mayores Costos importado
    @Column(name = "mayores_costos_general_importado")
    private Boolean mayoresCostosGeneralImportado;

    @Column(name = "mayores_costos_rubro_importado")
    private String mayoresCostosRubroImportado;

    @Column(name = "mayores_costos_nombre_rubro_importado")
    private String mayoresCostosNombreRubroImportado;

    @Column(name = "mayores_costos_explicacion", columnDefinition = "TEXT")
    private String mayoresCostosExplicacion;

    // ============================================================================
    // TOTALES ADICIONALES
    // ============================================================================

    @Column(name = "total_honorarios_calculado", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalHonorariosCalculado = BigDecimal.ZERO;

    @Column(name = "total_presupuesto_con_honorarios", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPresupuestoConHonorarios = BigDecimal.ZERO;

    @Column(name = "total_materiales", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalMateriales = BigDecimal.ZERO;

    @Column(name = "total_profesionales", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalProfesionales = BigDecimal.ZERO;

    @Column(name = "total_general", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalGeneral = BigDecimal.ZERO;

    // Honorario dirección
    @Column(name = "honorario_direccion_porcentaje", precision = 15, scale = 2)
    private BigDecimal honorarioDireccionPorcentaje;

    @Column(name = "honorario_direccion_importe", precision = 15, scale = 2)
    private BigDecimal honorarioDireccionImporte;

    @Column(name = "honorario_direccion_valor_fijo", precision = 15, scale = 2)
    private BigDecimal honorarioDireccionValorFijo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", insertable = false, updatable = false)
    private Obra obra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", insertable = false, updatable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", insertable = false, updatable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "trabajoExtra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrabajoExtraDia> dias = new ArrayList<>();

    @OneToMany(mappedBy = "trabajoExtra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrabajoExtroProfesional> profesionales = new ArrayList<>();

    @OneToMany(mappedBy = "trabajoExtra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrabajoExtraTarea> tareas = new ArrayList<>();

    // NUEVA RELACIÓN: Items calculadora (rubros)
    @OneToMany(mappedBy = "trabajoExtra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrabajoExtraItemCalculadora> itemsCalculadora = new ArrayList<>();
}
