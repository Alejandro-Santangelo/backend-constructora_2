package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Filter;

/**
 * Entidad para items de la calculadora de presupuestos.
 * Soporta dos modos de ingreso:
 * - Automático: cantidad_jornales × importe_jornal + materiales
 * - Manual: total ingresado directamente
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "items_calculadora_presupuesto")
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class ItemCalculadoraPresupuesto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presupuesto_no_cliente_id", nullable = false)
    @JsonBackReference
    private PresupuestoNoCliente presupuestoNoCliente;

    @Column(name = "tipo_profesional", nullable = false, length = 255)
    private String tipoProfesional;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "cantidad_jornales", precision = 10, scale = 2)
    private BigDecimal cantidadJornales;

    @Column(name = "importe_jornal", precision = 15, scale = 2)
    private BigDecimal importeJornal;

    @Column(name = "subtotal_mano_obra", precision = 15, scale = 2, nullable = true)
    private BigDecimal subtotalManoObra;

    @Column(name = "materiales", precision = 15, scale = 2)
    private BigDecimal materiales;

    @Column(name = "total_manual", precision = 15, scale = 2)
    private BigDecimal totalManual;
    
    @Column(name = "descripcion_total_manual", length = 500)
    private String descripcionTotalManual;
    
    @Column(name = "observaciones_total_manual", columnDefinition = "TEXT")
    private String observacionesTotalManual;

    @Column(name = "total", precision = 15, scale = 2, nullable = true)
    private BigDecimal total;

    @Column(name = "es_modo_manual", nullable = false)
    private Boolean esModoManual = false;

    @Column(name = "incluir_en_calculo_dias")
    @com.fasterxml.jackson.annotation.JsonProperty("incluirEnCalculoDias")
    private Boolean incluirEnCalculoDias;

    @Column(name = "trabaja_en_paralelo", nullable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("trabajaEnParalelo")
    private Boolean trabajaEnParalelo = true;

    @Column(name = "subtotal_materiales", precision = 15, scale = 2)
    private BigDecimal subtotalMateriales;

    // Relaciones con profesionales y materiales desglosados
    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ProfesionalCalculadora> profesionales = new ArrayList<>();

    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference  
    private List<MaterialCalculadora> materialesLista = new ArrayList<>();

    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<JornalCalculadora> jornales = new ArrayList<>();

    // ============================================================================
    // CAMPOS PARA GASTOS GENERALES (RELACIONAL - NO JSON)
    // ============================================================================

    @Column(name = "es_gasto_general", nullable = false)
    private Boolean esGastoGeneral = false;

    @Column(name = "subtotal_gastos_generales", precision = 15, scale = 2)
    private BigDecimal subtotalGastosGenerales;

    @Column(name = "descripcion_gastos_generales", length = 500)
    private String descripcionGastosGenerales;

    @Column(name = "observaciones_gastos_generales", columnDefinition = "TEXT")
    private String observacionesGastosGenerales;


    // ============================================================================
    // CAMPOS DE DESCRIPCIÓN/OBSERVACIONES POR CATEGORÍA
    // ============================================================================

    @Column(name = "descripcion_profesionales", length = 500)
    private String descripcionProfesionales;

    @Column(name = "observaciones_profesionales", columnDefinition = "TEXT")
    private String observacionesProfesionales;

    @Column(name = "descripcion_materiales", length = 500)
    private String descripcionMateriales;

    @Column(name = "observaciones_materiales", columnDefinition = "TEXT")
    private String observacionesMateriales;

    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PresupuestoGastoGeneral> gastosGenerales = new ArrayList<>();

    @OneToMany(mappedBy = "itemCalculadora", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<PagoConsolidado> pagosConsolidados = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonBackReference
    private Empresa empresa;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Valida la consistencia de los datos según el modo.
     * MODIFICADO: Permite datos parciales/incompletos para entrada incremental.
     * AGREGADO: Validación para gastos generales.
     * 
     * @throws IllegalStateException si los datos son inconsistentes
     */
    public void validar() {
        // ÚNICA VALIDACIÓN OBLIGATORIA: tipo de profesional
        if (tipoProfesional == null || tipoProfesional.trim().isEmpty()) {
            throw new IllegalStateException("El tipo de profesional no puede estar vacío");
        }

        // PERMITIR datos parciales/incompletos para entrada incremental
        // Los usuarios pueden guardar items solo con tipoProfesional y completar después

        if (Boolean.TRUE.equals(esGastoGeneral)) {
            // ========== MODO GASTOS GENERALES ==========
            validarGastosGenerales();
        } else if (Boolean.TRUE.equals(esModoManual)) {
            // ========== MODO MANUAL ==========
            // Modo manual: solo validar totalManual si tiene valor > 0
            if (totalManual != null && totalManual.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("En modo manual, si especifica total manual, debe ser mayor a 0");
            }
        } else {
            // ========== MODO AUTOMÁTICO ==========
            // Modo automático: validar coherencia solo si se proporcionan valores
            boolean tieneJornales = cantidadJornales != null && cantidadJornales.compareTo(BigDecimal.ZERO) > 0;
            boolean tieneImporteJornal = importeJornal != null && importeJornal.compareTo(BigDecimal.ZERO) > 0;

            // Validar coherencia: si especifica uno, debe especificar el otro
            if (tieneJornales && !tieneImporteJornal) {
                throw new IllegalStateException("Si especifica cantidad de jornales, debe especificar el importe por jornal");
            }

            if (!tieneJornales && tieneImporteJornal) {
                throw new IllegalStateException("Si especifica importe por jornal, debe especificar la cantidad de jornales");
            }

            // REMOVIDO: Ya no requerimos que tenga al menos jornales o materiales
            // Permite items parciales/incompletos para entrada incremental
        }
    }

    /**
     * Validaciones específicas para items de gastos generales.
     */
    private void validarGastosGenerales() {
        // Verificar que el tipo de profesional sea apropiado
        if (!tipoProfesional.toLowerCase().contains("gastos") && 
            !tipoProfesional.toLowerCase().contains("general")) {
            // ADVERTENCIA pero no error - permitir nombres flexibles
            // Podría ser "Gastos Generales", "Otros Gastos", etc.
        }

        // Validar coherencia: si es gasto general, no debe tener datos de profesionales normales
        if (Boolean.TRUE.equals(esGastoGeneral)) {
            if (cantidadJornales != null && cantidadJornales.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalStateException("Un item de gastos generales no debe tener cantidad de jornales");
            }
            
            if (importeJornal != null && importeJornal.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalStateException("Un item de gastos generales no debe tener importe por jornal");
            }
        }

        // Validar lista de gastos si está presente
        if (gastosGenerales != null) {
            for (PresupuestoGastoGeneral gasto : gastosGenerales) {
                if (gasto != null) {
                    gasto.validar(); // Delegar validación a cada gasto individual
                }
            }
        }

        // No es obligatorio tener gastos en el momento de validación
        // Permite creación incremental donde primero se crea el item y luego se agregan gastos
    }

    /**
     * Calcula y establece los valores totales según el modo.
     * Permite NULL para carga progresiva cuando no hay datos suficientes.
     * SIEMPRE calcula gastos generales primero (cualquier rubro puede tenerlos).
     */
    public void calcularTotales() {
        // PASO 1: SIEMPRE calcular gastos generales primero (si existen)
        calcularTotalesGastosGenerales();
        
        if (Boolean.TRUE.equals(esModoManual)) {
            // Modo manual: usar totalManual tal como viene (puede ser null)
            this.total = totalManual;
            this.subtotalManoObra = null; // En modo manual no aplica
        } else if (Boolean.TRUE.equals(esGastoGeneral)) {
            // Rubro tipo "Gastos Generales": el total YA fue calculado en calcularTotalesGastosGenerales()
            // No hacer nada más aquí
        } else {
            // Modo automático: calcular subtotal solo si tenemos datos completos
            if (cantidadJornales != null && importeJornal != null) {
                this.subtotalManoObra = cantidadJornales.multiply(importeJornal);
                
                // Calcular total: mano de obra + materiales + gastos generales
                BigDecimal totalMateriales = materiales != null ? materiales : BigDecimal.ZERO;
                BigDecimal totalGastos = subtotalGastosGenerales != null ? subtotalGastosGenerales : BigDecimal.ZERO;
                this.total = this.subtotalManoObra.add(totalMateriales).add(totalGastos);
            } else {
                // No hay datos suficientes para calcular - mantener null para carga progresiva
                this.subtotalManoObra = null;
                
                // Si solo tenemos materiales, ese sería el total parcial
                if (materiales != null && materiales.compareTo(BigDecimal.ZERO) > 0) {
                    this.total = materiales;
                } else {
                    this.total = null; // Permitir NULL para datos incompletos
                }
            }
        }
    }

    /**
     * Calcula el subtotal de gastos generales desde la lista.
     * SIEMPRE calcula si hay gastos en el array, sin importar el tipo de rubro.
     * Cualquier rubro puede tener gastos generales asociados.
     */
    public void calcularTotalesGastosGenerales() {
        // Calcular subtotal de gastos generales si existen
        if (gastosGenerales != null && !gastosGenerales.isEmpty()) {
            BigDecimal suma = gastosGenerales.stream()
                .map(PresupuestoGastoGeneral::getSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            this.subtotalGastosGenerales = suma;
        } else {
            this.subtotalGastosGenerales = BigDecimal.ZERO;
        }
        
        // Si es un rubro TIPO "Gastos Generales" (sin profesionales/materiales)
        if (Boolean.TRUE.equals(esGastoGeneral)) {
            this.total = this.subtotalGastosGenerales;
            this.subtotalManoObra = null;
            this.materiales = BigDecimal.ZERO;
        }
        // Si es un rubro normal, el subtotal se suma más adelante en calcularTotales()
    }

    /**
     * Verifica si este item es de gastos generales y tiene gastos cargados.
     * @return true si es gasto general con gastos, false en caso contrario
     */
    public boolean tieneGastosGenerales() {
        return Boolean.TRUE.equals(esGastoGeneral) && 
               gastosGenerales != null && 
               !gastosGenerales.isEmpty();
    }

    /**
     * Agrega un gasto general a este item.
     * Valida que el item esté configurado como gasto general.
     * 
     * @param gastoGeneral El gasto a agregar
     * @throws IllegalStateException si el item no es de gastos generales
     */
    public void agregarGastoGeneral(PresupuestoGastoGeneral gastoGeneral) {
        if (!Boolean.TRUE.equals(esGastoGeneral)) {
            throw new IllegalStateException("Solo se pueden agregar gastos generales a items configurados como gastos generales");
        }
        
        if (gastosGenerales == null) {
            gastosGenerales = new ArrayList<>();
        }
        
        // Establecer relación bidireccional
        gastoGeneral.setItemCalculadora(this);
        gastoGeneral.setEmpresa(this.empresa);
        
        // Asignar orden automático si no está configurado
        if (gastoGeneral.getOrden() == null) {
            Integer maxOrden = gastosGenerales.stream()
                .map(PresupuestoGastoGeneral::getOrden)
                .filter(orden -> orden != null)
                .max(Integer::compareTo)
                .orElse(0);
            gastoGeneral.setOrden(maxOrden + 1);
        }
        
        gastosGenerales.add(gastoGeneral);
        
        // Recalcular totales
        calcularTotalesGastosGenerales();
    }

    /**
     * Limpia todos los gastos generales de este item.
     */
    public void limpiarGastosGenerales() {
        if (gastosGenerales != null) {
            gastosGenerales.clear();
        }
        this.subtotalGastosGenerales = BigDecimal.ZERO;
        
        if (Boolean.TRUE.equals(esGastoGeneral)) {
            this.total = BigDecimal.ZERO;
        }
    }
}
