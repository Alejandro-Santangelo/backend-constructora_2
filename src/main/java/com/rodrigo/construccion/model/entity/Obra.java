package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.enums.TipoOrigen;
import com.rodrigo.construccion.enums.TipoPresupuesto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "obras", indexes = {
        @Index(name = "idx_obras_cliente", columnList = "id_cliente"),
        @Index(name = "idx_obras_estado", columnList = "estado")
})
@Filter(name = "empresaFilter", condition = "EXISTS (SELECT 1 FROM cliente_empresa ce WHERE ce.id_cliente = id_cliente AND ce.id_empresa = :empresaId)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Obra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_obra")
    private Long id;

    @Column(name = "es_obra_trabajo_extra")
    private Boolean esObraTrabajoExtra = false;

    @Column(name = "obra_origen_id")
    private Long obraOrigenId;

    @Column(name = "es_obra_manual")
    private Boolean esObraManual = false;
    
    @Column(name = "presupuesto_original_id")
    private Long presupuestoOriginalId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origen", length = 50)
    private TipoOrigen tipoOrigen;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    // DIRECCIÓN EN 6 CAMPOS SEPARADOS (igual que PresupuestoNoCliente)
    @Column(name = "direccion_obra_barrio", length = 100)
    private String direccionObraBarrio;

    @Column(name = "direccion_obra_calle", nullable = false)
    private String direccionObraCalle;

    @Column(name = "direccion_obra_altura", nullable = false)
    private String direccionObraAltura;

    @Column(name = "direccion_obra_torre", length = 50)
    private String direccionObraTorre;

    @Column(name = "direccion_obra_piso")
    private String direccionObraPiso;

    @Column(name = "direccion_obra_departamento")
    private String direccionObraDepartamento;

    @Column(name = "estado", length = 50)
    private String estado;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "presupuesto_estimado", precision = 15, scale = 2)
    private BigDecimal presupuestoEstimado;

    @Column(name = "presupuesto_jornales", precision = 15, scale = 2)
    private BigDecimal presupuestoJornales;

    @Column(name = "presupuesto_materiales", precision = 15, scale = 2)
    private BigDecimal presupuestoMateriales;

    @Column(name = "importe_gastos_generales_obra", precision = 15, scale = 2)
    private BigDecimal importeGastosGeneralesObra;

    @Column(name = "presupuesto_honorarios", precision = 15, scale = 2)
    private BigDecimal presupuestoHonorarios;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorarios_presupuesto", length = 20)
    private String tipoHonorarioPresupuesto;

    @Column(name = "presupuesto_mayores_costos", precision = 15, scale = 2)
    private BigDecimal presupuestoMayoresCostos;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_mayores_costos_presupuesto", length = 20)
    private String tipoMayoresCostosPresupuesto;

    // ========== HONORARIOS INDIVIDUALES POR CATEGORÍA (SISTEMA NUEVO) ==========
    
    @Column(name = "honorario_jornales_obra", precision = 15, scale = 2)
    private BigDecimal honorarioJornalesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorario_jornales_obra", length = 10)
    private String tipoHonorarioJornalesObra;

    @Column(name = "honorario_materiales_obra", precision = 15, scale = 2)
    private BigDecimal honorarioMaterialesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorario_materiales_obra", length = 10)
    private String tipoHonorarioMaterialesObra;

    @Column(name = "honorario_gastos_generales_obra", precision = 15, scale = 2)
    private BigDecimal honorarioGastosGeneralesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorario_gastos_generales_obra", length = 10)
    private String tipoHonorarioGastosGeneralesObra;

    @Column(name = "honorario_mayores_costos_obra", precision = 15, scale = 2)
    private BigDecimal honorarioMayoresCostosObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_honorario_mayores_costos_obra", length = 10)
    private String tipoHonorarioMayoresCostosObra;

    // ========== DESCUENTOS SOBRE IMPORTES BASE POR CATEGORÍA ==========

    @Column(name = "descuento_jornales_obra", precision = 15, scale = 2)
    private BigDecimal descuentoJornalesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_jornales_obra", length = 10)
    private String tipoDescuentoJornalesObra;

    @Column(name = "descuento_materiales_obra", precision = 15, scale = 2)
    private BigDecimal descuentoMaterialesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_materiales_obra", length = 10)
    private String tipoDescuentoMaterialesObra;

    @Column(name = "descuento_gastos_generales_obra", precision = 15, scale = 2)
    private BigDecimal descuentoGastosGeneralesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_gastos_generales_obra", length = 10)
    private String tipoDescuentoGastosGeneralesObra;

    @Column(name = "descuento_mayores_costos_obra", precision = 15, scale = 2)
    private BigDecimal descuentoMayoresCostosObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_mayores_costos_obra", length = 10)
    private String tipoDescuentoMayoresCostosObra;

    // ========== DESCUENTOS SOBRE HONORARIOS POR CATEGORÍA (NUEVOS) ==========

    @Column(name = "descuento_honorario_jornales_obra", precision = 15, scale = 2)
    private BigDecimal descuentoHonorarioJornalesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_honorario_jornales_obra", length = 10)
    private String tipoDescuentoHonorarioJornalesObra;

    @Column(name = "descuento_honorario_materiales_obra", precision = 15, scale = 2)
    private BigDecimal descuentoHonorarioMaterialesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_honorario_materiales_obra", length = 10)
    private String tipoDescuentoHonorarioMaterialesObra;

    @Column(name = "descuento_honorario_gastos_generales_obra", precision = 15, scale = 2)
    private BigDecimal descuentoHonorarioGastosGeneralesObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_honorario_gastos_generales_obra", length = 10)
    private String tipoDescuentoHonorarioGastosGeneralesObra;

    @Column(name = "descuento_honorario_mayores_costos_obra", precision = 15, scale = 2)
    private BigDecimal descuentoHonorarioMayoresCostosObra;

    /** Valores posibles: "fijo" | "porcentaje" */
    @Column(name = "tipo_descuento_honorario_mayores_costos_obra", length = 10)
    private String tipoDescuentoHonorarioMayoresCostosObra;

    @Column(name = "presupuesto_no_cliente_id")
    private Long presupuestoNoClienteId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Datos del cliente/solicitante (sincronizados desde presupuesto)
    @Column(name = "nombre_solicitante", length = 200)
    private String nombreSolicitante;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "mail", length = 150)
    private String mail;

    @Column(name = "direccion_particular", columnDefinition = "TEXT")
    private String direccionParticular;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Relaciones
    @JsonBackReference("cliente-obras")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = true)  // Permitir null para obras borrador sin cliente inicial
    private Cliente cliente;

    // Relación con presupuestos no cliente
    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PresupuestoNoCliente> presupuestosNoCliente = new ArrayList<>();

    @JsonManagedReference("obra-costos")
    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Costo> costos = new ArrayList<>();

    @JsonManagedReference("obra-honorarios")
    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Honorario> honorarios = new ArrayList<>();

    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CobroObra> cobros = new ArrayList<>();

    @JsonManagedReference("obra-pedidospago")
    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PedidoPago> pedidosPago = new ArrayList<>();

    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProfesionalObra> profesionalesAsignados = new ArrayList<>();

    @OneToMany(mappedBy = "obra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ObraMaterial> materialesAsignados = new ArrayList<>();

    // ========== CAMPOS CALCULADOS (NO PERSISTIDOS) ==========

    @Transient
    private BigDecimal totalCobrado;

    @Transient
    private BigDecimal totalPagadoProfesionales;

    @Transient
    private BigDecimal totalGastosCajaChica;

    @Transient
    private BigDecimal saldoObra;

    @Transient
    private BigDecimal pendienteCobro;

    /**
     * Getter para el estado como Enum.
     * Este es el método que el resto de la aplicación usará.
     * Convierte el String de la BD a un Enum.
     */
    @Transient
    public EstadoObra getEstadoEnum() {
        return EstadoObra.fromDisplayName(this.estado);
    }

    /**
     * Setter para el estado como Enum.
     * Este es el método que el resto de la aplicación usará.
     * Convierte el Enum a un String para guardarlo en la BD.
     */
    public void setEstado(EstadoObra estadoEnum) {
        if (estadoEnum != null) {
            this.estado = estadoEnum.getDisplayName();
        }
    }

    /**
     * Verifica si la obra está en estado BORRADOR.
     * Una obra borrador permite modificaciones sin restricciones.
     * @return true if la obra está en estado BORRADOR
     */
    @Transient
    public boolean esBorrador() {
        return EstadoObra.BORRADOR.getDisplayName().equals(this.estado);
    }

    /**
     * Helper para obtener la dirección completa formateada
     *
     * @return Dirección formateada como "Calle 1234 Piso 4 Depto A"
     */
    @Transient
    public String getDireccionCompleta() {
        return String.format("%s %s%s%s",
                direccionObraCalle != null ? direccionObraCalle : "",
                direccionObraAltura != null ? direccionObraAltura : "",
                direccionObraPiso != null ? " Piso " + direccionObraPiso : "",
                direccionObraDepartamento != null ? " Depto " + direccionObraDepartamento : ""
        ).trim();
    }

    // Getters de conveniencia para Multi-Tenant
    public Long getClienteId() {
        return cliente != null ? cliente.getId() : null;
    }

    /**
     * Getter de empresaId
     * Ahora es un campo directo en la base de datos
     */
    public Long getEmpresaId() {
        return this.empresaId;
    }

    /**
     * Calcula el total cobrado al cliente en esta obra
     */
    public BigDecimal calcularTotalCobrado() {
        if (cobros == null || cobros.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cobros.stream()
                .filter(cobro -> "COBRADO".equals(cobro.getEstado()))
                .map(CobroObra::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el pendiente de cobro (presupuesto - cobrado)
     */
    public BigDecimal calcularPendienteCobro() {
        BigDecimal presupuesto = presupuestoEstimado != null ? presupuestoEstimado : BigDecimal.ZERO;
        return presupuesto.subtract(calcularTotalCobrado());
    }

    /**
     * Calcula y asigna todos los campos calculados.
     * Debe llamarse antes de serializar la entidad.
     */
    public void calcularCamposCalculados() {
        this.totalCobrado = calcularTotalCobrado();
        this.pendienteCobro = calcularPendienteCobro();
        // totalPagadoProfesionales, totalGastosCajaChica y saldoObra se calcularán en el service
    }

}
