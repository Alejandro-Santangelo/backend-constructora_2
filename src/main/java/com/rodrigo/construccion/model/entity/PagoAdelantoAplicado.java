package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que registra qué adelantos fueron aplicados/descontados en cada pago regular.
 * Implementación 100% relacional, reemplaza la columna JSONB adelantos_aplicados_ids.
 * 
 * @author Sistema
 * @version 1.0
 * @since 4 de marzo de 2026
 */
@Entity
@Table(name = "pago_adelantos_aplicados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoAdelantoAplicado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Pago regular donde se descontó el adelanto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private PagoProfesionalObra pago;

    /**
     * Adelanto que fue descontado (es_adelanto = true)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adelanto_id", nullable = false)
    private PagoProfesionalObra adelanto;

    /**
     * Monto del adelanto que se descontó en este pago
     */
    @Column(name = "monto_descontado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoDescontado;

    /**
     * Fecha en que se aplicó el descuento
     */
    @Column(name = "fecha_aplicacion")
    private LocalDateTime fechaAplicacion;

    @PrePersist
    protected void onCreate() {
        if (fechaAplicacion == null) {
            fechaAplicacion = LocalDateTime.now();
        }
    }

    /**
     * Constructor de conveniencia
     */
    public PagoAdelantoAplicado(PagoProfesionalObra pago, PagoProfesionalObra adelanto, BigDecimal montoDescontado) {
        this.pago = pago;
        this.adelanto = adelanto;
        this.montoDescontado = montoDescontado;
    }
}
