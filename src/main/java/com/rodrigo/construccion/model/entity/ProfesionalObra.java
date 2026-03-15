package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rodrigo.construccion.enums.RolEnObra;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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

/**
 * Entidad ProfesionalObra
 * Representa profesionales asignados a obras (tabla separada y dedicada).
 * Para asignaciones a rubros específicos ver AsignacionProfesionalObra.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asignaciones_profesional_obra")
@Filter(name = "empresaFilter", condition = "empresa_id = :empresaId")
public class ProfesionalObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_fin")
    private LocalDate fechaHasta;

    @Size(max = 100, message = "El rol en obra no puede exceder 100 caracteres")
    @Column(name = "profesional_tipo", length = 100)
    private String rolEnObra;

    @PositiveOrZero(message = "El valor por hora debe ser mayor o igual a cero")
    @Column(name = "importe_jornal", precision = 10, scale = 2)
    private BigDecimal valorHoraAsignado;
    
    @Column(name = "cantidad_jornales", precision = 10, scale = 2)
    private BigDecimal cantidadJornales;
    
    @Column(name = "importe_jornal", precision = 15, scale = 2, insertable = false, updatable = false)
    private BigDecimal importeJornal;
    
    @Column(name = "jornales_utilizados", precision = 10, scale = 2)
    private BigDecimal jornalesUtilizados;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "estado")
    private String estado;

    @Column(name = "modalidad")
    private String modalidad;

    // Nota: activo no existe en asignaciones_profesional_obra, se mapea desde estado
    @Transient
    private Boolean activo = true;

    // CAMPOS DE CAJA CHICA - no existen en asignaciones_profesional_obra
    @Transient
    private BigDecimal montoAsignado = BigDecimal.ZERO;

    @Transient
    private BigDecimal saldoDisponible = BigDecimal.ZERO;

    // CAMPOS DE DIRECCIÓN DE OBRA - no se usan en asignaciones_profesional_obra (usa obra_id FK)
    @Transient
    private String direccionObraCalle;

    @Transient
    private String direccionObraAltura;

    @Transient
    private String direccionObraPiso;

    @Transient
    private String direccionObraDepartamento;

    // Campo para multi-tenancy directo
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime fechaCreacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    @JsonBackReference
    private Profesional profesional;

    // Relación con Obra usando el campo obra_id (FK en base de datos)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id", nullable = false)
    @JsonBackReference
    private Obra obra;

    @OneToMany(mappedBy = "asignacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Jornal> jornales = new ArrayList<>();

    @OneToMany(mappedBy = "profesionalObra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PagoProfesionalObra> pagos = new ArrayList<>();

    // Métodos de conveniencia
    public void addJornal(Jornal jornal) {
        jornales.add(jornal);
        jornal.setAsignacion(this);
    }

    public void removeJornal(Jornal jornal) {
        jornales.remove(jornal);
        jornal.setAsignacion(null);
    }

    public void addPago(PagoProfesionalObra pago) {
        pagos.add(pago);
        pago.setProfesionalObra(this);
    }

    public void removePago(PagoProfesionalObra pago) {
        pagos.remove(pago);
        pago.setProfesionalObra(null);
    }

    // Getters de conveniencia
    public Long getProfesionalId() {
        return profesional != null ? profesional.getId() : null;
    }

    public String getNombreProfesional() {
        return profesional != null ? profesional.getNombre() : null;
    }

    public Long getIdObra() {
        return obra != null ? obra.getId() : null;
    }

    public String getNombreObra() {
        return obra != null ? obra.getNombre() : null;
    }

    /**
     * Helper para obtener la dirección completa de la obra formateada
     *
     * @return Dirección formateada como "Calle 1234 Piso 4 Depto A"
     */
    @Transient
    public String getDireccionObraCompleta() {
        return String.format("%s %s%s%s",
                direccionObraCalle != null ? direccionObraCalle : "",
                direccionObraAltura != null ? direccionObraAltura : "",
                direccionObraPiso != null ? " Piso " + direccionObraPiso : "",
                direccionObraDepartamento != null ? " Depto " + direccionObraDepartamento : ""
        ).trim();
    }

    /**
     * Getter para el estado como Enum.
     * Este es el método que el resto de la aplicación usará.
     * Convierte el String de la BD a un Enum.
     */
    @Transient
    public RolEnObra getRolEnObraEnum() {
        return RolEnObra.fromDisplayName(this.rolEnObra);
    }

    /**
     * Setter para el estado como Enum.
     * Este es el método que el resto de la aplicación usará.
     * Convierte el Enum a un String para guardarlo en la BD.
     */
    public void setRolEnObraEnum(RolEnObra rolEnObra) {
        if (rolEnObra != null) {
            this.rolEnObra = rolEnObra.getDisplayName();
        }
    }

    /**
     * Método ejecutado después de cargar la entidad desde la BD.
     * Calcula el campo activo basándose en el estado.
     */
    @PostLoad
    private void calcularCamposDerivados() {
        // Calcular activo desde estado ("activo", "completado", "cancelado", etc.)
        this.activo = "activo".equalsIgnoreCase(this.estado);
        
        // Si la dirección no está en campos propios, obtenerla de la relación obra
        if (obra != null && direccionObraCalle == null) {
            this.direccionObraCalle = obra.getDireccionObraCalle();
            this.direccionObraAltura = obra.getDireccionObraAltura();
            this.direccionObraPiso = obra.getDireccionObraPiso();
            this.direccionObraDepartamento = obra.getDireccionObraDepartamento();
        }
    }

    /**
     * Getter personalizado para activo que asegura el cálculo si es null
     */
    public Boolean getActivo() {
        if (activo == null) {
            activo = "activo".equalsIgnoreCase(this.estado);
        }
        return activo;
    }
}
