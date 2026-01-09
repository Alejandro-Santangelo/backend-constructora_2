package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rodrigo.construccion.enums.PresupuestoEstado;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
        @jakarta.persistence.Transient
        private java.math.BigDecimal totalHonorarios;

        @jakarta.persistence.Transient
        private java.math.BigDecimal totalMayoresCostos;

        @jakarta.persistence.Transient
        private java.math.BigDecimal totalFinal;
        public java.math.BigDecimal getTotalMayoresCostos() {
            return totalMayoresCostos;
        }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"presupuestosNoCliente", "hibernateLazyInitializer", "handler"})
    private Obra obra;

    // Relación con cliente (opcional - puede ser NULL si aún no es cliente formal)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"obras", "empresas", "hibernateLazyInitializer", "handler"})
    private Cliente cliente;

    // ========== TIPO DE PRESUPUESTO ==========
    /**
     * Tipo de presupuesto: TRADICIONAL o TRABAJOS_SEMANALES.
     * - TRADICIONAL: Flujo normal con estados (A enviar, Enviado, Aprobado, etc.)
     * - TRABAJOS_SEMANALES: Se aprueba automáticamente, no requiere envío al cliente
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "modo_presupuesto", nullable = false, length = 50)
    private com.rodrigo.construccion.model.enums.TipoPresupuesto tipoPresupuesto = 
        com.rodrigo.construccion.model.enums.TipoPresupuesto.TRADICIONAL;

    /**
     * Fecha de la última modificación del estado.
     * Se usa para evitar que el proceso automático sobrescriba cambios manuales recientes.
     * El proceso automático NO cambia el estado si fue modificado manualmente en las últimas 24 horas.
     */
    @Column(name = "fecha_ultima_modificacion_estado")
    private java.time.LocalDateTime fechaUltimaModificacionEstado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonBackReference
    private Empresa empresa;

    // Relación ONE-TO-ONE con costos iniciales por m²
    @jakarta.persistence.OneToOne(mappedBy = "presupuestoNoCliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PresupuestoCostoInicial costoInicial;

    // Relación con items de calculadora de presupuestos
    @OneToMany(mappedBy = "presupuestoNoCliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private Set<ItemCalculadoraPresupuesto> itemsCalculadora = new HashSet<>();

    /**
     * Calcula el total de honorarios según la configuración establecida.
     * 
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
    @com.fasterxml.jackson.annotation.JsonProperty("obraId")
    public Long getObraId() {
        return this.obra != null ? this.obra.getId() : null;
    }

    /**
     * Getter para serializar cliente_id en el JSON de respuesta.
     * El frontend necesita este campo para preservar la relación al editar.
     */
    @com.fasterxml.jackson.annotation.JsonProperty("clienteId")
    public Long getClienteId() {
        return this.cliente != null ? this.cliente.getId() : null;
    }

    /**
     * Calcula y asigna todos los campos calculados.
     * Este método debe llamarse antes de serializar la entidad a JSON y antes de guardar.
     * 
     * Flujo de cálculo:
     * 1. Calcula honorarios (sobre BASE de cada categoría)
     * 2. Calcula mayores costos (sobre BASE de cada categoría, NO sobre base+honorarios)
     * 3. Calcula total final (BASE + HONORARIOS + MAYORES_COSTOS)
     */
    public void calcularCamposCalculados() {
        // Solo recalcular si NO existe el valor persistido
        if (this.totalPresupuestoConHonorarios != null && this.totalPresupuestoConHonorarios.compareTo(BigDecimal.ZERO) > 0) {
            // Si ya existe el total persistido, lo usamos y no recalculamos
            this.totalFinal = this.totalPresupuestoConHonorarios;
            return;
        }
        // 1. Calcular honorarios (solo sobre base, NO sobre mayores costos)
        BigDecimal honorariosCalculados = calcularTotalHonorarios();
        this.totalHonorarios = honorariosCalculados; // Transient para JSON
        this.totalHonorariosCalculado = honorariosCalculados; // Persistido en BD
        // 2. Calcular mayores costos (sobre base de cada categoría, NO sobre base+honorarios)
        this.totalMayoresCostos = calcularTotalMayoresCostos();
        // 3. Calcular total final
        BigDecimal base = totalPresupuesto != null ? totalPresupuesto : 
            (totalGeneral != null ? BigDecimal.valueOf(totalGeneral) : BigDecimal.ZERO);
        BigDecimal honorarios = this.totalHonorariosCalculado != null ? this.totalHonorariosCalculado : BigDecimal.ZERO;
        BigDecimal mayoresCostos = this.totalMayoresCostos != null ? this.totalMayoresCostos : BigDecimal.ZERO;
        this.totalPresupuestoConHonorarios = base.add(honorarios).add(mayoresCostos);
        this.totalFinal = this.totalPresupuestoConHonorarios; // Transient para compatibilidad
    }

}