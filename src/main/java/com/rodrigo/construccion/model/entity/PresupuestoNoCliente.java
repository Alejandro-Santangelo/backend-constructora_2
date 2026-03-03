package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.*;
import com.rodrigo.construccion.enums.PresupuestoEstado;
import com.rodrigo.construccion.enums.TipoPresupuesto;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "presupuesto_no_cliente")
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class PresupuestoNoCliente {
    // Campos transient solo para cálculos temporales
    @Transient
    private BigDecimal totalHonorarios;

    @Transient
    private BigDecimal totalMayoresCostos;

    @Transient
    private BigDecimal totalFinal;

    public BigDecimal getTotalMayoresCostos() {
        return totalMayoresCostos;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "es_presupuesto_trabajo_extra")
    private Boolean esPresupuestoTrabajoExtra = false;

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaEmision;

    private String nombreSolicitante;
    private String direccionParticular;
    private String direccionObra;
    private String descripcion;
    private String profesionalesNecesarios;
    private Integer tiempoEstimadoTerminacion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaProbableInicio;

    private String telefono;
    private String mail;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate vencimiento;

    private String observaciones;

    // Campos adicionales de descripción y observaciones
    @Column(name = "descripcion_detallada", columnDefinition = "TEXT")
    private String descripcionDetallada;

    @Column(name = "observaciones_internas", columnDefinition = "TEXT")
    private String observacionesInternas;

    @Column(name = "notas_adicionales", columnDefinition = "TEXT")
    private String notasAdicionales;

    @Column(name = "especificaciones_tecnicas", columnDefinition = "TEXT")
    private String especificacionesTecnicas;

    @Column(name = "comentarios_cliente", columnDefinition = "TEXT")
    private String comentariosCliente;

    @Column(name = "requisitos_especiales", columnDefinition = "TEXT")
    private String requisitosEspeciales;

    // Nuevos campos solicitados
    @Column(name = "numero_presupuesto")
    private Long numeroPresupuesto;

    @Column(name = "numero_version")
    private Integer numeroVersion;

    @Column(name = "fecha_creacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaCreacion;

    @Column(name = "nombre_obra", nullable = true, length = 255)
    private String nombreObra;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private PresupuestoEstado estado;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_presupuesto", nullable = false, length = 50)
    private TipoPresupuesto tipoPresupuesto = TipoPresupuesto.PRINCIPAL;
    
    @Column(name = "modo_presupuesto", nullable = false, length = 50)
    private String modoPresupuesto = "TRADICIONAL";

    // Direccion obra desglosada
    @Column(name = "direccion_obra_calle", nullable = false)
    private String direccionObraCalle;

    @Column(name = "direccion_obra_altura", nullable = false)
    private String direccionObraAltura;

    @Column(name = "direccion_obra_piso")
    private String direccionObraPiso;

    @Column(name = "direccion_obra_departamento")
    private String direccionObraDepartamento;

    @Column(name = "direccion_obra_barrio")
    private String direccionObraBarrio;

    @Column(name = "direccion_obra_torre")
    private String direccionObraTorre;

    // Totales calculados (campos legacy)
    @Column(name = "total_profesionales")
    private Double totalProfesionales;

    @Column(name = "total_materiales")
    private Double totalMateriales;

    @Column(name = "total_general")
    private Double totalGeneral;

    // ========== TOTALES ESPECÍFICOS DEL FRONTEND ==========
    @Column(name = "total_presupuesto", precision = 15, scale = 2)
    private BigDecimal totalPresupuesto;  // Total base sin honorarios

    @Column(name = "total_honorarios_calculado", precision = 15, scale = 2)
    private BigDecimal totalHonorariosCalculado;   // Total de honorarios calculados

    @Column(name = "total_presupuesto_con_honorarios", precision = 15, scale = 2)
    private BigDecimal totalPresupuestoConHonorarios; // ⭐ TOTAL FINAL (lo más importante)

    @Column(name = "total_con_descuentos", precision = 15, scale = 2)
    private BigDecimal totalConDescuentos; // Total después de aplicar descuentos

    @com.fasterxml.jackson.annotation.JsonProperty("totalFinal")
    public BigDecimal getTotalPresupuestoConHonorarios() {
        return this.totalPresupuestoConHonorarios;
    }

    // Honorarios de dirección de obra
    @Column(name = "honorario_direccion_valor_fijo")
    private Double honorarioDireccionValorFijo;

    @Column(name = "honorario_direccion_porcentaje")
    private Double honorarioDireccionPorcentaje;

    @Column(name = "honorario_direccion_importe")
    private Double honorarioDireccionImporte;

    // ========== CONFIGURACIÓN DE HONORARIOS ==========
    @Column(name = "honorarios_aplicar_a_todos")
    private Boolean honorariosAplicarATodos = true;

    @Column(name = "honorarios_valor_general", precision = 15, scale = 2)
    private BigDecimal honorariosValorGeneral;

    @Column(name = "honorarios_tipo_general", length = 20)
    private String honorariosTipoGeneral = "porcentaje";

    // Honorarios Profesionales
    @Column(name = "honorarios_profesionales_activo")
    private Boolean honorariosProfesionalesActivo = true;

    @Column(name = "honorarios_profesionales_tipo", length = 20)
    private String honorariosProfesionalesTipo = "porcentaje";

    @Column(name = "honorarios_profesionales_valor", precision = 15, scale = 2)
    private BigDecimal honorariosProfesionalesValor;

    // Honorarios Materiales
    @Column(name = "honorarios_materiales_activo")
    private Boolean honorariosMaterialesActivo = true;

    @Column(name = "honorarios_materiales_tipo", length = 20)
    private String honorariosMaterialesTipo = "porcentaje";

    @Column(name = "honorarios_materiales_valor", precision = 15, scale = 2)
    private BigDecimal honorariosMaterialesValor;

    // Honorarios Otros Costos
    @Column(name = "honorarios_otros_costos_activo")
    private Boolean honorariosOtrosCostosActivo = true;

    @Column(name = "honorarios_otros_costos_tipo", length = 20)
    private String honorariosOtrosCostosTipo = "porcentaje";

    @Column(name = "honorarios_otros_costos_valor", precision = 15, scale = 2)
    private BigDecimal honorariosOtrosCostosValor;

    // Honorarios Jornales
    @Column(name = "honorarios_jornales_activo")
    private Boolean honorariosJornalesActivo = true;

    @Column(name = "honorarios_jornales_tipo", length = 20)
    private String honorariosJornalesTipo = "porcentaje";

    @Column(name = "honorarios_jornales_valor", precision = 15, scale = 2)
    private BigDecimal honorariosJornalesValor;

    // Honorarios Configuración Presupuesto
    @Column(name = "honorarios_configuracion_presupuesto_activo")
    private Boolean honorariosConfiguracionPresupuestoActivo = true;

    @Column(name = "honorarios_configuracion_presupuesto_tipo", length = 20)
    private String honorariosConfiguracionPresupuestoTipo = "porcentaje";

    @Column(name = "honorarios_configuracion_presupuesto_valor", precision = 10, scale = 2)
    private BigDecimal honorariosConfiguracionPresupuestoValor;

    // ========== CONFIGURACIÓN DE MAYORES COSTOS ==========
    @Column(name = "mayores_costos_aplicar_valor_general")
    private Boolean mayoresCostosAplicarValorGeneral;

    @Column(name = "mayores_costos_valor_general")
    private Double mayoresCostosValorGeneral;

    @Column(name = "mayores_costos_tipo_general", length = 20)
    private String mayoresCostosTipoGeneral = "porcentaje";

    @Column(name = "mayores_costos_general_importado")
    private Boolean mayoresCostosGeneralImportado;

    @Column(name = "mayores_costos_rubro_importado")
    private String mayoresCostosRubroImportado;

    @Column(name = "mayores_costos_nombre_rubro_importado")
    private String mayoresCostosNombreRubroImportado;

    // Mayores Costos Profesionales
    @Column(name = "mayores_costos_profesionales_activo")
    private Boolean mayoresCostosProfesionalesActivo;

    @Column(name = "mayores_costos_profesionales_tipo", length = 20)
    private String mayoresCostosProfesionalesTipo = "porcentaje";

    @Column(name = "mayores_costos_profesionales_valor")
    private Double mayoresCostosProfesionalesValor;

    // Mayores Costos Materiales
    @Column(name = "mayores_costos_materiales_activo")
    private Boolean mayoresCostosMaterialesActivo;

    @Column(name = "mayores_costos_materiales_tipo", length = 20)
    private String mayoresCostosMaterialesTipo = "porcentaje";

    @Column(name = "mayores_costos_materiales_valor")
    private Double mayoresCostosMaterialesValor;

    // Mayores Costos Otros Costos
    @Column(name = "mayores_costos_otros_costos_activo")
    private Boolean mayoresCostosOtrosCostosActivo;

    @Column(name = "mayores_costos_otros_costos_tipo", length = 20)
    private String mayoresCostosOtrosCostosTipo = "porcentaje";

    @Column(name = "mayores_costos_otros_costos_valor")
    private Double mayoresCostosOtrosCostosValor;

    // Mayores Costos Jornales
    @Column(name = "mayores_costos_jornales_activo")
    private Boolean mayoresCostosJornalesActivo;

    @Column(name = "mayores_costos_jornales_tipo", length = 20)
    private String mayoresCostosJornalesTipo = "porcentaje";

    @Column(name = "mayores_costos_jornales_valor")
    private Double mayoresCostosJornalesValor;

    // Mayores Costos Configuracion Presupuesto
    @Column(name = "mayores_costos_configuracion_presupuesto_activo")
    private Boolean mayoresCostosConfiguracionPresupuestoActivo;

    @Column(name = "mayores_costos_configuracion_presupuesto_tipo", length = 20)
    private String mayoresCostosConfiguracionPresupuestoTipo = "porcentaje";

    @Column(name = "mayores_costos_configuracion_presupuesto_valor")
    private Double mayoresCostosConfiguracionPresupuestoValor;

    // Mayores Costos Honorarios
    @Column(name = "mayores_costos_honorarios_activo")
    private Boolean mayoresCostosHonorariosActivo;

    @Column(name = "mayores_costos_honorarios_tipo", length = 20)
    private String mayoresCostosHonorariosTipo = "porcentaje";

    @Column(name = "mayores_costos_honorarios_valor")
    private Double mayoresCostosHonorariosValor;

    // Explicación/justificación INTERNA de mayores costos (NO visible para cliente)
    @Column(name = "mayores_costos_explicacion", columnDefinition = "TEXT")
    private String mayoresCostosExplicacion;

    // ========== CONFIGURACIÓN DE DESCUENTOS (Modelo Relacional) ==========
    /**
     * Los descuentos se aplican DESPUÉS de honorarios y mayores costos,
     * restando importes del total consolidado.
     * Modelo relacional siguiendo el patrón de honorarios/mayores_costos.
     */
    
    // Explicación general de descuentos (visible en PDF para el cliente)
    @Column(name = "descuentos_explicacion", columnDefinition = "TEXT")
    private String descuentosExplicacion;
    
    // Descuentos sobre JORNALES (aplicados sobre subtotal de jornales SIN honorarios)
    @Column(name = "descuentos_jornales_activo")
    private Boolean descuentosJornalesActivo = false;
    
    @Column(name = "descuentos_jornales_tipo", length = 20)
    private String descuentosJornalesTipo = "porcentaje";
    
    @Column(name = "descuentos_jornales_valor", precision = 15, scale = 2)
    private BigDecimal descuentosJornalesValor;
    
    // Descuentos sobre MATERIALES (aplicados sobre subtotal de materiales SIN honorarios)
    @Column(name = "descuentos_materiales_activo")
    private Boolean descuentosMaterialesActivo = false;
    
    @Column(name = "descuentos_materiales_tipo", length = 20)
    private String descuentosMaterialesTipo = "porcentaje";
    
    @Column(name = "descuentos_materiales_valor", precision = 15, scale = 2)
    private BigDecimal descuentosMaterialesValor;
    
    // Descuentos sobre HONORARIOS (aplicados sobre total de honorarios)
    @Column(name = "descuentos_honorarios_activo")
    private Boolean descuentosHonorariosActivo = false;
    
    @Column(name = "descuentos_honorarios_tipo", length = 20)
    private String descuentosHonorariosTipo = "porcentaje";
    
    @Column(name = "descuentos_honorarios_valor", precision = 15, scale = 2)
    private BigDecimal descuentosHonorariosValor;
    
    // Descuentos sobre MAYORES COSTOS (aplicados sobre total de mayores costos)
    @Column(name = "descuentos_mayores_costos_activo")
    private Boolean descuentosMayoresCostosActivo = false;
    
    @Column(name = "descuentos_mayores_costos_tipo", length = 20)
    private String descuentosMayoresCostosTipo = "porcentaje";
    
    @Column(name = "descuentos_mayores_costos_valor", precision = 15, scale = 2)
    private BigDecimal descuentosMayoresCostosValor;
    
    // ========== SUB-TIPOS DE DESCUENTOS SOBRE HONORARIOS ==========
    /**
     * Descuentos granulares sobre cada categoría de honorarios.
     * Estos campos permiten aplicar descuentos específicos sobre los honorarios 
     * de cada rubro (jornales, profesionales, materiales, etc.).
     */
    
    // Descuentos sobre Honorarios de JORNALES
    @Column(name = "descuentos_honorarios_jornales_activo")
    private Boolean descuentosHonorariosJornalesActivo = true;
    
    @Column(name = "descuentos_honorarios_jornales_tipo", length = 20)
    private String descuentosHonorariosJornalesTipo = "porcentaje";
    
    @Column(name = "descuentos_honorarios_jornales_valor", precision = 15, scale = 2)
    private BigDecimal descuentosHonorariosJornalesValor;
    
    // Descuentos sobre Honorarios de PROFESIONALES
    @Column(name = "descuentos_honorarios_profesionales_activo")
    private Boolean descuentosHonorariosProfesionalesActivo = true;
    
    @Column(name = "descuentos_honorarios_profesionales_tipo", length = 20)
    private String descuentosHonorariosProfesionalesTipo = "porcentaje";
    
    @Column(name = "descuentos_honorarios_profesionales_valor", precision = 15, scale = 2)
    private BigDecimal descuentosHonorariosProfesionalesValor;
    
    // Descuentos sobre Honorarios de MATERIALES
    @Column(name = "descuentos_honorarios_materiales_activo")
    private Boolean descuentosHonorariosMaterialesActivo = true;
    
    @Column(name = "descuentos_honorarios_materiales_tipo", length = 20)
    private String descuentosHonorariosMaterialesTipo = "porcentaje";
    
    @Column(name = "descuentos_honorarios_materiales_valor", precision = 15, scale = 2)
    private BigDecimal descuentosHonorariosMaterialesValor;
    
    // Descuentos sobre Honorarios de OTROS COSTOS
    @Column(name = "descuentos_honorarios_otros_activo")
    private Boolean descuentosHonorariosOtrosActivo = true;
    
    @Column(name = "descuentos_honorarios_otros_tipo", length = 20)
    private String descuentosHonorariosOtrosTipo = "porcentaje";
    
    @Column(name = "descuentos_honorarios_otros_valor", precision = 15, scale = 2)
    private BigDecimal descuentosHonorariosOtrosValor;
    
    // Descuentos sobre Honorarios de GASTOS GENERALES
    @Column(name = "descuentos_honorarios_gastos_generales_activo")
    private Boolean descuentosHonorariosGastosGeneralesActivo = true;
    
    @Column(name = "descuentos_honorarios_gastos_generales_tipo", length = 20)
    private String descuentosHonorariosGastosGeneralesTipo = "porcentaje";
    
    @Column(name = "descuentos_honorarios_gastos_generales_valor", precision = 15, scale = 2)
    private BigDecimal descuentosHonorariosGastosGeneralesValor;
    
    // Descuentos sobre Honorarios de CONFIGURACIÓN DE PRESUPUESTO
    @Column(name = "descuentos_honorarios_configuracion_activo")
    private Boolean descuentosHonorariosConfiguracionActivo = true;
    
    @Column(name = "descuentos_honorarios_configuracion_tipo", length = 20)
    private String descuentosHonorariosConfiguracionTipo = "porcentaje";
    
    @Column(name = "descuentos_honorarios_configuracion_valor", precision = 15, scale = 2)
    private BigDecimal descuentosHonorariosConfiguracionValor;

    // Campos transient para cálculos temporales de descuentos
    @Transient
    private BigDecimal totalDescuentos;

    @Transient
    private BigDecimal totalSinDescuentos;

    // ========== CONFIGURACIÓN DE CÁLCULO DE DÍAS HÁBILES ==========
    /**
     * Indica si el cálculo de días hábiles es automático (true) o manual (false).
     * - true: El sistema calcula automáticamente los días hábiles
     * - false: El usuario ingresa manualmente los días hábiles
     */
    @Column(name = "calculo_automatico_dias_habiles", nullable = false)
    private Boolean calculoAutomaticoDiasHabiles = false;

    // Relación con obra creada al aprobar presupuesto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id")
    @JsonIgnoreProperties({"presupuestosNoCliente", "hibernateLazyInitializer", "handler"})
    private Obra obra;

    /**
     * Relación con TrabajoAdicional (NUEVA FUNCIONALIDAD)
     * Para presupuestos tipo TAREA_LEVE, pueden vincularse a:
     * - Una Obra (comportamiento actual): obra_id tiene valor, trabajo_adicional_id es NULL
     * - Un TrabajoAdicional (nuevo): trabajo_adicional_id tiene valor, obra_id es NULL
     * 
     * IMPORTANTE: obra_id y trabajo_adicional_id son mutuamente excluyentes para TAREA_LEVE
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_adicional_id")
    @JsonIgnoreProperties({"presupuestosTareasLeves", "hibernateLazyInitializer", "handler"})
    private TrabajoAdicional trabajoAdicional;

    // Relación con cliente (opcional - puede ser NULL si aún no es cliente formal)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnoreProperties({"obras", "empresas", "hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    // ========== TIPO DE PRESUPUESTO ==========
    /**

     * Fecha de la última modificación del estado.
     * Se usa para evitar que el proceso automático sobrescriba cambios manuales recientes.
     * El proceso automático NO cambia el estado si fue modificado manualmente en las últimas 24 horas.
     */
    @Column(name = "fecha_ultima_modificacion_estado")
    private LocalDateTime fechaUltimaModificacionEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonBackReference
    private Empresa empresa;

    // Relación ONE-TO-ONE con costos iniciales por m²
    @OneToOne(mappedBy = "presupuestoNoCliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private PresupuestoCostoInicial costoInicial;

    // Relación con items de calculadora de presupuestos
    @OneToMany(mappedBy = "presupuestoNoCliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @JsonManagedReference
    private Set<ItemCalculadoraPresupuesto> itemsCalculadora = new HashSet<>();

    /**
     * Calcula el total de honorarios según la configuración establecida.
     * <p>
     * Lógica:
     * - Si honorariosAplicarATodos es true: aplica honorariosValorGeneral sobre totalPresupuesto
     * - Si es false: calcula por separado para cada categoría (jornales, profesionales, materiales, otros costos, configuración)
     * - Los honorarios se calculan SOLO sobre la BASE, NO sobre mayores costos
     *
     * @return Total de honorarios calculado
     */
    public BigDecimal calcularTotalHonorarios() {
        BigDecimal total = BigDecimal.ZERO;

        // Usar totalPresupuesto si existe, sino usar totalGeneral (legacy)
        BigDecimal baseTotal = totalPresupuesto != null ? totalPresupuesto :
                (totalGeneral != null ? BigDecimal.valueOf(totalGeneral) : BigDecimal.ZERO);

        if (Boolean.TRUE.equals(honorariosAplicarATodos)) {
            // Modo: Aplicar a todos
            if (honorariosValorGeneral != null && honorariosValorGeneral.compareTo(BigDecimal.ZERO) > 0) {
                if ("VALOR_FIJO".equalsIgnoreCase(honorariosTipoGeneral) || "fijo".equalsIgnoreCase(honorariosTipoGeneral)) {
                    total = honorariosValorGeneral;
                } else {
                    // Porcentaje sobre base total
                    total = baseTotal.multiply(honorariosValorGeneral).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                }
            }
        } else {
            // Modo: Individual por rubro

            // Obtener totales base por categoría
            BigDecimal totalJornales = calcularTotalJornales();
            BigDecimal totalProf = totalProfesionales != null ? BigDecimal.valueOf(totalProfesionales) : BigDecimal.ZERO;
            BigDecimal totalMat = totalMateriales != null ? BigDecimal.valueOf(totalMateriales) : BigDecimal.ZERO;
            BigDecimal totalConfig = calcularTotalConfiguracion();

            // Calcular totalOtrosCostos (lo que queda de la base)
            BigDecimal totalOtros = baseTotal.subtract(totalJornales).subtract(totalProf)
                    .subtract(totalMat).subtract(totalConfig);
            if (totalOtros.compareTo(BigDecimal.ZERO) < 0) {
                totalOtros = BigDecimal.ZERO;
            }

            // Honorarios sobre JORNALES
            if (Boolean.TRUE.equals(honorariosJornalesActivo) && honorariosJornalesValor != null) {
                if ("VALOR_FIJO".equalsIgnoreCase(honorariosJornalesTipo) || "fijo".equalsIgnoreCase(honorariosJornalesTipo)) {
                    total = total.add(honorariosJornalesValor);
                } else {
                    total = total.add(totalJornales.multiply(honorariosJornalesValor)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
                }
            }

            // Honorarios sobre PROFESIONALES
            if (Boolean.TRUE.equals(honorariosProfesionalesActivo) && honorariosProfesionalesValor != null) {
                if ("VALOR_FIJO".equalsIgnoreCase(honorariosProfesionalesTipo) || "fijo".equalsIgnoreCase(honorariosProfesionalesTipo)) {
                    total = total.add(honorariosProfesionalesValor);
                } else {
                    total = total.add(totalProf.multiply(honorariosProfesionalesValor)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
                }
            }

            // Honorarios sobre MATERIALES
            if (Boolean.TRUE.equals(honorariosMaterialesActivo) && honorariosMaterialesValor != null) {
                if ("VALOR_FIJO".equalsIgnoreCase(honorariosMaterialesTipo) || "fijo".equalsIgnoreCase(honorariosMaterialesTipo)) {
                    total = total.add(honorariosMaterialesValor);
                } else {
                    total = total.add(totalMat.multiply(honorariosMaterialesValor)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
                }
            }

            // Honorarios sobre OTROS COSTOS
            if (Boolean.TRUE.equals(honorariosOtrosCostosActivo) && honorariosOtrosCostosValor != null) {
                if ("VALOR_FIJO".equalsIgnoreCase(honorariosOtrosCostosTipo) || "fijo".equalsIgnoreCase(honorariosOtrosCostosTipo)) {
                    total = total.add(honorariosOtrosCostosValor);
                } else {
                    total = total.add(totalOtros.multiply(honorariosOtrosCostosValor)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
                }
            }

            // Honorarios sobre CONFIGURACIÓN PRESUPUESTO
            if (Boolean.TRUE.equals(honorariosConfiguracionPresupuestoActivo) && honorariosConfiguracionPresupuestoValor != null) {
                if ("VALOR_FIJO".equalsIgnoreCase(honorariosConfiguracionPresupuestoTipo) || "fijo".equalsIgnoreCase(honorariosConfiguracionPresupuestoTipo)) {
                    total = total.add(honorariosConfiguracionPresupuestoValor);
                } else {
                    total = total.add(totalConfig.multiply(honorariosConfiguracionPresupuestoValor)
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
                }
            }
        }

        return total;
    }

    /**
     * Calcula el total de mayores costos según la configuración establecida.
     * Los mayores costos se calculan sobre BASE de cada categoría (NO sobre base+honorarios).
     *
     * @return Total de mayores costos calculado
     */
    public BigDecimal calcularTotalMayoresCostos() {
        BigDecimal total = BigDecimal.ZERO;

        // Obtener totales base por categoría
        BigDecimal totalJornales = calcularTotalJornales();
        BigDecimal totalProf = totalProfesionales != null ? BigDecimal.valueOf(totalProfesionales) : BigDecimal.ZERO;
        BigDecimal totalMat = totalMateriales != null ? BigDecimal.valueOf(totalMateriales) : BigDecimal.ZERO;
        BigDecimal totalConfig = calcularTotalConfiguracion();

        BigDecimal baseTotal = totalPresupuesto != null ? totalPresupuesto :
                (totalGeneral != null ? BigDecimal.valueOf(totalGeneral) : BigDecimal.ZERO);

        BigDecimal totalOtros = baseTotal.subtract(totalJornales).subtract(totalProf)
                .subtract(totalMat).subtract(totalConfig);
        if (totalOtros.compareTo(BigDecimal.ZERO) < 0) {
            totalOtros = BigDecimal.ZERO;
        }

        // Mayores Costos sobre JORNALES (solo sobre BASE, no sobre honorarios)
        if (Boolean.TRUE.equals(mayoresCostosJornalesActivo) && mayoresCostosJornalesValor != null) {
            total = total.add(aplicarMayorCosto(totalJornales, mayoresCostosJornalesValor, mayoresCostosJornalesTipo));
        }

        // Mayores Costos sobre PROFESIONALES (solo sobre BASE, no sobre honorarios)
        if (Boolean.TRUE.equals(mayoresCostosProfesionalesActivo) && mayoresCostosProfesionalesValor != null) {
            total = total.add(aplicarMayorCosto(totalProf, mayoresCostosProfesionalesValor, mayoresCostosProfesionalesTipo));
        }

        // Mayores Costos sobre MATERIALES (solo sobre BASE, no sobre honorarios)
        if (Boolean.TRUE.equals(mayoresCostosMaterialesActivo) && mayoresCostosMaterialesValor != null) {
            total = total.add(aplicarMayorCosto(totalMat, mayoresCostosMaterialesValor, mayoresCostosMaterialesTipo));
        }

        // Mayores Costos sobre OTROS COSTOS (solo sobre BASE, no sobre honorarios)
        if (Boolean.TRUE.equals(mayoresCostosOtrosCostosActivo) && mayoresCostosOtrosCostosValor != null) {
            total = total.add(aplicarMayorCosto(totalOtros, mayoresCostosOtrosCostosValor, mayoresCostosOtrosCostosTipo));
        }

        // Mayores Costos sobre CONFIGURACIÓN PRESUPUESTO (solo sobre BASE, no sobre honorarios)
        if (Boolean.TRUE.equals(mayoresCostosConfiguracionPresupuestoActivo) && mayoresCostosConfiguracionPresupuestoValor != null) {
            total = total.add(aplicarMayorCosto(totalConfig, mayoresCostosConfiguracionPresupuestoValor, mayoresCostosConfiguracionPresupuestoTipo));
        }

        // Mayores Costos sobre HONORARIOS (total de todos los honorarios)
        if (Boolean.TRUE.equals(mayoresCostosHonorariosActivo) && mayoresCostosHonorariosValor != null) {
            BigDecimal totalHonorariosCalculados = calcularTotalHonorarios();
            total = total.add(aplicarMayorCosto(totalHonorariosCalculados, mayoresCostosHonorariosValor, mayoresCostosHonorariosTipo));
        }

        return total;
    }

    /**
     * Método auxiliar para calcular honorarios sobre una categoría específica.
     */
    private BigDecimal calcularHonorariosPorCategoria(BigDecimal baseCategoria, Boolean activo,
                                                      BigDecimal valor, String tipo) {
        if (!Boolean.TRUE.equals(activo) || valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if ("VALOR_FIJO".equalsIgnoreCase(tipo) || "fijo".equalsIgnoreCase(tipo)) {
            return valor;
        } else {
            return baseCategoria.multiply(valor).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Método auxiliar para aplicar mayor costo sobre una base.
     */
    private BigDecimal aplicarMayorCosto(BigDecimal base, Double valor, String tipo) {
        if (valor == null || valor <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal valorBD = BigDecimal.valueOf(valor);

        if ("VALOR_FIJO".equalsIgnoreCase(tipo) || "fijo".equalsIgnoreCase(tipo)) {
            return valorBD;
        } else {
            return base.multiply(valorBD).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Calcula el total de jornales desde la colección de jornales.
     * LEGACY: Este método usaba la tabla presupuesto_no_cliente_jornal que fue eliminada.
     * Ahora los jornales están en jornal_calculadora vinculada a items_calculadora_presupuesto.
     */
    private BigDecimal calcularTotalJornales() {
        // DEPRECADO: Ya no se usa la colección jornales legacy
        // Ahora los jornales están en items_calculadora -> jornal_calculadora
        return BigDecimal.ZERO;
    }

    /**
     * Calcula el total de configuración del presupuesto.
     * Esto puede venir de campos específicos o calcularse según la lógica de negocio.
     */
    private BigDecimal calcularTotalConfiguracion() {
        // Por ahora retornamos 0, se puede implementar según lógica de negocio
        return BigDecimal.ZERO;
    }

    /**
     * Getter para serializar obra_id en el JSON de respuesta.
     * El frontend necesita este campo para preservar la relación al editar.
     */
    @JsonProperty("obraId")
    public Long getObraId() {
        return this.obra != null ? this.obra.getId() : null;
    }

    /**
     * Getter para serializar trabajo_adicional_id en el JSON de respuesta.
     * El frontend necesita este campo para presupuestos TAREA_LEVE vinculados a trabajos adicionales.
     */
    @JsonProperty("trabajoAdicionalId")
    public Long getTrabajoAdicionalId() {
        return this.trabajoAdicional != null ? this.trabajoAdicional.getId() : null;
    }

    /**
     * Getter para serializar cliente_id en el JSON de respuesta.
     * El frontend necesita este campo para preservar la relación al editar.
     */
    @JsonProperty("clienteId")
    public Long getClienteId() {
        return this.cliente != null ? this.cliente.getId() : null;
    }

    /**
     * Calcula el descuento individual de una categoría.
     * 
     * @param baseCategoria Base sobre la cual se aplica el descuento
     * @param activo Si el descuento está activo
     * @param tipo Tipo de descuento: "porcentaje" o "fijo"
     * @param valor Valor del descuento
     * @return Monto del descuento calculado
     */
    private BigDecimal calcularDescuentoCategoria(BigDecimal baseCategoria, Boolean activo, String tipo, BigDecimal valor) {
        if (baseCategoria == null) {
            return BigDecimal.ZERO;
        }
        
        // Si activo es false, no aplicar descuento
        if (Boolean.FALSE.equals(activo)) {
            return BigDecimal.ZERO;
        }
        
        // Si valor es null o cero, no hay descuento
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (tipo == null) {
            tipo = "porcentaje"; // Default
        }
        
        if ("fijo".equalsIgnoreCase(tipo) || "VALOR_FIJO".equalsIgnoreCase(tipo)) {
            // Descuento fijo: no puede exceder la base
            return valor.min(baseCategoria);
        } else {
            // Descuento porcentual: calcular % sobre la base
            // El porcentaje debe estar entre 0 y 100
            BigDecimal porcentaje = valor.min(BigDecimal.valueOf(100));
            return baseCategoria.multiply(porcentaje).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Calcula el total de descuentos según la configuración establecida.
     * Los descuentos se calculan sobre diferentes bases:
     * - Jornales: sobre subtotal de jornales SIN honorarios
     * - Materiales: sobre subtotal de materiales SIN honorarios
     * - Honorarios: sobre total de honorarios calculados
     * - Mayores Costos: sobre total de mayores costos calculados
     * 
     * @return Total de descuentos calculado
     */
    public BigDecimal calcularTotalDescuentos() {
        BigDecimal descuentoTotal = BigDecimal.ZERO;
        
        // Obtener bases para cada categoría
        BigDecimal totalJornales = calcularTotalJornales();
        BigDecimal totalMaterialesBase = this.totalMateriales != null ? BigDecimal.valueOf(this.totalMateriales) : BigDecimal.ZERO;
        BigDecimal totalHonorariosCalc = this.totalHonorariosCalculado != null ? 
                                         this.totalHonorariosCalculado : calcularTotalHonorarios();
        BigDecimal totalMayoresCostosCalc = this.totalMayoresCostos != null ? 
                                            this.totalMayoresCostos : calcularTotalMayoresCostos();
        
        // Calcular descuento sobre JORNALES (base sin honorarios)
        BigDecimal descuentoJornales = calcularDescuentoCategoria(
            totalJornales, 
            descuentosJornalesActivo, 
            descuentosJornalesTipo, 
            descuentosJornalesValor
        );
        descuentoTotal = descuentoTotal.add(descuentoJornales);
        
        // Calcular descuento sobre MATERIALES (base sin honorarios)
        BigDecimal descuentoMateriales = calcularDescuentoCategoria(
            totalMaterialesBase, 
            descuentosMaterialesActivo, 
            descuentosMaterialesTipo, 
            descuentosMaterialesValor
        );
        descuentoTotal = descuentoTotal.add(descuentoMateriales);
        
        // Calcular descuento sobre HONORARIOS (total de honorarios)
        BigDecimal descuentoHonorarios = calcularDescuentoCategoria(
            totalHonorariosCalc, 
            descuentosHonorariosActivo, 
            descuentosHonorariosTipo, 
            descuentosHonorariosValor
        );
        descuentoTotal = descuentoTotal.add(descuentoHonorarios);
        
        // Calcular descuento sobre MAYORES COSTOS (total de mayores costos)
        BigDecimal descuentoMayoresCostos = calcularDescuentoCategoria(
            totalMayoresCostosCalc, 
            descuentosMayoresCostosActivo, 
            descuentosMayoresCostosTipo, 
            descuentosMayoresCostosValor
        );
        descuentoTotal = descuentoTotal.add(descuentoMayoresCostos);
        
        return descuentoTotal;
    }

    /**
     * Valida que la configuración de descuentos sea correcta.
     * Verifica que:
     * - Los porcentajes estén entre 0 y 100
     * - Los valores fijos no excedan la base correspondiente
     * - El total de descuentos no exceda el subtotal sin descuentos
     * 
     * @return Mensaje de error si la validación falla, null si es correcta
     */
    public String validarDescuentos() {
        // Si no hay ningún descuento activo, no validar
        if (Boolean.FALSE.equals(descuentosJornalesActivo) &&
            Boolean.FALSE.equals(descuentosMaterialesActivo) &&
            Boolean.FALSE.equals(descuentosHonorariosActivo) &&
            Boolean.FALSE.equals(descuentosMayoresCostosActivo)) {
            return null;
        }
        
        // Calcular bases
        BigDecimal base = totalPresupuesto != null ? totalPresupuesto :
                (totalGeneral != null ? BigDecimal.valueOf(totalGeneral) : BigDecimal.ZERO);
        BigDecimal honorarios = this.totalHonorariosCalculado != null ? 
                               this.totalHonorariosCalculado : calcularTotalHonorarios();
        BigDecimal mayoresCostos = this.totalMayoresCostos != null ? 
                                   this.totalMayoresCostos : calcularTotalMayoresCostos();
        BigDecimal subtotalSinDescuentos = base.add(honorarios).add(mayoresCostos);
        
        // Calcular total de descuentos
        BigDecimal totalDesc = calcularTotalDescuentos();
        
        // Validar que el total final no sea negativo
        if (totalDesc.compareTo(subtotalSinDescuentos) > 0) {
            return "El total de descuentos (" + totalDesc + ") excede el subtotal sin descuentos (" + 
                   subtotalSinDescuentos + "). El total final no puede ser negativo.";
        }
        
        // Validar cada categoría individualmente
        String errorJornales = validarCategoria(
            descuentosJornalesActivo, 
            descuentosJornalesTipo, 
            descuentosJornalesValor, 
            "Jornales", 
            calcularTotalJornales()
        );
        if (errorJornales != null) return errorJornales;
        
        BigDecimal totalMat = totalMateriales != null ? BigDecimal.valueOf(totalMateriales) : BigDecimal.ZERO;
        String errorMateriales = validarCategoria(
            descuentosMaterialesActivo, 
            descuentosMaterialesTipo, 
            descuentosMaterialesValor, 
            "Materiales", 
            totalMat
        );
        if (errorMateriales != null) return errorMateriales;
        
        String errorHonorarios = validarCategoria(
            descuentosHonorariosActivo, 
            descuentosHonorariosTipo, 
            descuentosHonorariosValor, 
            "Honorarios", 
            honorarios
        );
        if (errorHonorarios != null) return errorHonorarios;
        
        String errorMayoresCostos = validarCategoria(
            descuentosMayoresCostosActivo, 
            descuentosMayoresCostosTipo, 
            descuentosMayoresCostosValor, 
            "Mayores Costos", 
            mayoresCostos
        );
        if (errorMayoresCostos != null) return errorMayoresCostos;
        
        return null; // Todo OK
    }

    /**
     * Valida una categoría individual de descuento.
     */
    private String validarCategoria(Boolean activo, String tipo, BigDecimal valor, String nombreCategoria, BigDecimal base) {
        if (Boolean.FALSE.equals(activo)) {
            return null; // Categoría desactivada, no validar
        }
        
        if (valor == null) {
            return null; // Sin valor, no hay descuento
        }
        
        // Validar que el valor sea positivo
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            return "El valor de descuento para " + nombreCategoria + " no puede ser negativo.";
        }
        
        if (tipo == null) {
            tipo = "porcentaje";
        }
        
        if ("porcentaje".equalsIgnoreCase(tipo)) {
            // Validar que el porcentaje esté entre 0 y 100
            if (valor.compareTo(BigDecimal.valueOf(100)) > 0) {
                return "El porcentaje de descuento para " + nombreCategoria + " no puede exceder 100%.";
            }
        } else if ("fijo".equalsIgnoreCase(tipo) || "VALOR_FIJO".equalsIgnoreCase(tipo)) {
            // Validar que el valor fijo no exceda la base
            if (valor.compareTo(base) > 0) {
                return "El valor fijo de descuento para " + nombreCategoria + " (" + valor + 
                       ") excede la base de la categoría (" + base + ").";
            }
        }
        
        return null; // Válido
    }

    /**
     * Calcula y asigna todos los campos calculados.
     * Este método debe llamarse antes de serializar la entidad a JSON y antes de guardar.
     * <p>
     * Flujo de cálculo:
     * 1. Calcula honorarios (sobre BASE de cada categoría)
     * 2. Calcula mayores costos (sobre BASE de cada categoría, NO sobre base+honorarios)
     * 3. Calcula subtotal sin descuentos (BASE + HONORARIOS + MAYORES_COSTOS)
     * 4. Calcula descuentos (sobre diferentes bases según categoría)
     * 5. Calcula total final (SUBTOTAL SIN DESCUENTOS - DESCUENTOS)
     */
    public void calcularCamposCalculados() {
        // Solo recalcular si NO existe el valor persistido
        if (this.totalPresupuestoConHonorarios != null && this.totalPresupuestoConHonorarios.compareTo(BigDecimal.ZERO) > 0) {
            // Si ya existe el total persistido, lo usamos
            // Pero aún así calculamos descuentos si existen
            this.totalFinal = this.totalPresupuestoConHonorarios;
            
            // Calcular descuentos si hay configuración
            BigDecimal descuentos = calcularTotalDescuentos();
            if (descuentos.compareTo(BigDecimal.ZERO) > 0) {
                this.totalDescuentos = descuentos;
                this.totalSinDescuentos = this.totalPresupuestoConHonorarios;
                this.totalFinal = this.totalPresupuestoConHonorarios.subtract(descuentos);
                this.totalPresupuestoConHonorarios = this.totalFinal; // Actualizar el total final
            }
            return;
        }
        
        // 1. Calcular honorarios (solo sobre base, NO sobre mayores costos)
        BigDecimal honorariosCalculados = calcularTotalHonorarios();
        this.totalHonorarios = honorariosCalculados; // Transient para JSON
        this.totalHonorariosCalculado = honorariosCalculados; // Persistido en BD
        
        // 2. Calcular mayores costos (sobre base de cada categoría, NO sobre base+honorarios)
        this.totalMayoresCostos = calcularTotalMayoresCostos();
        
        // 3. Calcular subtotal sin descuentos (BASE + HONORARIOS + MAYORES_COSTOS)
        BigDecimal base = totalPresupuesto != null ? totalPresupuesto :
                (totalGeneral != null ? BigDecimal.valueOf(totalGeneral) : BigDecimal.ZERO);
        BigDecimal honorarios = this.totalHonorariosCalculado != null ? this.totalHonorariosCalculado : BigDecimal.ZERO;
        BigDecimal mayoresCostos = this.totalMayoresCostos != null ? this.totalMayoresCostos : BigDecimal.ZERO;
        
        BigDecimal subtotalSinDescuentos = base.add(honorarios).add(mayoresCostos);
        
        // 4. Calcular descuentos
        BigDecimal descuentos = calcularTotalDescuentos();
        
        // 5. Calcular total final
        if (descuentos.compareTo(BigDecimal.ZERO) > 0) {
            // Hay descuentos aplicados
            this.totalDescuentos = descuentos;
            this.totalSinDescuentos = subtotalSinDescuentos;
            this.totalPresupuestoConHonorarios = subtotalSinDescuentos.subtract(descuentos);
        } else {
            // No hay descuentos
            this.totalDescuentos = BigDecimal.ZERO;
            this.totalSinDescuentos = null; // No mostrar en frontend si no hay descuentos
            this.totalPresupuestoConHonorarios = subtotalSinDescuentos;
        }
        
        this.totalFinal = this.totalPresupuestoConHonorarios; // Transient para compatibilidad
    }

}