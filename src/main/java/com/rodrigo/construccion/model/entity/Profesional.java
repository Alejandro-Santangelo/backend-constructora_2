package com.rodrigo.construccion.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.rodrigo.construccion.enums.TipoProfesional;

/**
 * Entidad Profesional
 * 
 * Representa los profesionales que trabajan en las obras.
 * Para otros tipos de empresa podría representar:
 * - Mueblería: Diseñadores, Carpinteros, etc.
 * - Seguros: Agentes, Peritos, etc.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profesionales", indexes = {
        @Index(name = "idx_profesionales_tipo", columnList = "tipo_profesional"),
        @Index(name = "idx_profesionales_activo", columnList = "activo")
})
public class Profesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesional")
    private Long id;

    @NotBlank(message = "El nombre del profesional es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @NotBlank(message = "El tipo de profesional es obligatorio")
    @Size(max = 50, message = "El tipo de profesional no puede exceder 50 caracteres")
    @Column(name = "tipo_profesional", nullable = false, length = 50)
    private String tipoProfesional;

    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    @Column(name = "telefono", length = 50)
    private String telefono;

    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 100, message = "La especialidad no puede exceder 100 caracteres")
    @Column(name = "especialidad", length = 100)
    private String especialidad;

    // Campos para profesionales libres en presupuestos
    @Column(name = "horas")
    private Integer horas;

    @Column(name = "dias")
    private Integer dias;

    @Column(name = "semanas")
    private Integer semanas;

    @Column(name = "meses")
    private Integer meses;

    @Column(name = "honorario_hora", precision = 10, scale = 2)
    private BigDecimal honorarioHora;

    @Column(name = "honorario_dia", precision = 10, scale = 2)
    private BigDecimal honorarioDia;

    @Column(name = "honorario_semana", precision = 10, scale = 2)
    private BigDecimal honorarioSemana;

    @Column(name = "honorario_mes", precision = 10, scale = 2)
    private BigDecimal honorarioMes;

    @PositiveOrZero(message = "El valor por hora debe ser mayor o igual a cero")
    @Column(name = "valor_hora_default", precision = 10, scale = 2)
    private BigDecimal valorHoraDefault = BigDecimal.ZERO;

    /**
     * Porcentaje de ganancia para el profesional
     * Permite definir el margen de ganancia aplicado a su valor hora
     */
    @Column(name = "porcentaje_ganancia", precision = 5, scale = 2)
    private BigDecimal porcentajeGanancia = BigDecimal.ZERO;

    /**
     * Importe de ganancia calculado (valorHoraDefault * porcentajeGanancia / 100)
     */
    @Column(name = "importe_ganancia", precision = 10, scale = 2)
    private BigDecimal importeGanancia = BigDecimal.ZERO;

    @Column(name = "activo")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Relaciones
    @OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProfesionalObra> obrasAsignadas = new ArrayList<>();

    @OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Honorario> honorarios = new ArrayList<>();

    @Column(name = "cuit", length = 20, nullable = true)
    private String cuit;

    /**
     * Rol personalizado ingresado por el usuario.
     * Solo se usa cuando tipoProfesional = 'Otro (personalizado)'
     * Ejemplo: "Medio Oficial Albañileria", "Ayudante de Carpintería", etc.
     */
    @Size(max = 100, message = "El rol personalizado no puede exceder 100 caracteres")
    @Column(name = "rol_personalizado", length = 100, nullable = true)
    private String rolPersonalizado;

    // Métodos de conveniencia
    public void addObraAsignada(ProfesionalObra profesionalObra) {
        obrasAsignadas.add(profesionalObra);
        profesionalObra.setProfesional(this);
    }

    public void removeObraAsignada(ProfesionalObra profesionalObra) {
        obrasAsignadas.remove(profesionalObra);
        profesionalObra.setProfesional(null);
    }

    public void addHonorario(Honorario honorario) {
        honorarios.add(honorario);
        honorario.setProfesional(this);
    }

    public void removeHonorario(Honorario honorario) {
        honorarios.remove(honorario);
        honorario.setProfesional(null);
    }

    /**
     * Método de conveniencia para actualizar el porcentaje y el importe de
     * ganancia.
     * Encapsula la lógica de cálculo en la propia entidad.
     * 
     * @param nuevoPorcentaje El nuevo porcentaje de ganancia como BigDecimal.
     */
    public void actualizarGanancia(BigDecimal nuevoPorcentaje) {
        this.setPorcentajeGanancia(nuevoPorcentaje);
        if (this.getValorHoraDefault() != null) {
            BigDecimal importe = this.getValorHoraDefault()
                    .multiply(nuevoPorcentaje)
                    .divide(new BigDecimal("100"));
            this.setImporteGanancia(importe);
        }
    }

    /**
     * Método de conveniencia para actualizar el valor por hora por un porcentaje.
     * Encapsula la lógica de cálculo en la propia entidad.
     *
     * @param porcentaje El porcentaje de aumento o disminución (ej: 10 para 10%, -5
     *                   para -5%).
     */
    public void actualizarValorHoraPorPorcentaje(double porcentaje) {
        if (this.valorHoraDefault == null) {
            this.valorHoraDefault = BigDecimal.ZERO;
        }
        // Se calcula el factor de multiplicación. Ej: para 10%, el factor es 1.10
        BigDecimal factor = BigDecimal.ONE.add(BigDecimal.valueOf(porcentaje).divide(new BigDecimal("100")));
        BigDecimal nuevoValor = this.valorHoraDefault.multiply(factor);

        // Aseguramos que el valor no sea negativo.
        this.setValorHoraDefault(nuevoValor.max(BigDecimal.ZERO));
    }

    /**
     * Getter para el tipo de profesional como Enum.
     * Este es el método que el resto de la aplicación usará.
     * Convierte el String de la BD a un Enum.
     */
    @Transient
    public TipoProfesional getTipoProfesionalEnum() {
        if (this.tipoProfesional == null) {
            return null;
        }
        return TipoProfesional.fromDisplayName(this.tipoProfesional);
    }

    /**
     * Setter para el tipo de profesional como Enum.
     * Este es el método que el resto de la aplicación usará.
     * Convierte el Enum a un String para guardarlo en la BD.
     */
    public void setTipoProfesionalEnum(TipoProfesional tipo) {
        this.tipoProfesional = Objects.requireNonNull(tipo, "El tipo de profesional no puede ser nulo.")
                .getDisplayName();
    }

}