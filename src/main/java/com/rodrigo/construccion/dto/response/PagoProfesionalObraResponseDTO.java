package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoProfesionalObraResponseDTO {

    private Long id;
    private Long profesionalObraId;
    private Long presupuestoNoClienteId;
    private String nombreProfesional;
    private String direccionObra;
    private Long empresaId;

    // Tipo y fecha del pago
    private String tipoPago;
    private LocalDate fechaPago;

    // Período
    private LocalDate periodoDesde;
    private LocalDate periodoHasta;

    // Semana (para pagos semanales)
    private Integer semana;

    // Montos - Frontend requirements
    private BigDecimal montoBruto; // Monto base antes de descuentos
    private BigDecimal descuentoAdelantos;
    private BigDecimal descuentoPresentismo;
    private BigDecimal montoNeto; // Calculado: montoBruto - descuentoAdelantos - descuentoPresentismo
    private BigDecimal montoPagado; // Alias para compatibilidad (igual a montoNeto o montoFinal)

    // Montos - Legacy (compatibilidad)
    private BigDecimal montoBase;
    private BigDecimal ajustes;
    private BigDecimal montoFinal;

    // Datos de premios/bonos
    private String premioTipo;
    private BigDecimal premioValor;
    private BigDecimal premioBase;
    private String premioConcepto;
    private BigDecimal montoPremioCalculado;

    // Presentismo
    private Integer diasTrabajados;
    private Integer diasEsperados;
    private BigDecimal porcentajePresentismo;

    // Información del comprobante
    private String numeroRecibo;
    private String metodoPago;
    private String numeroComprobante;

    // Estado
    private String estado;

    // Campos adicionales
    private String concepto;
    private String observaciones;
    private String comprobanteUrl;

    // Auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long usuarioCreacionId;
    private Long usuarioModificacionId;

    // Información calculada
    private Boolean esPagoSemanal;
    private Boolean esAdelanto;
    private Boolean esPremio;
    private Boolean esBono;
    private Boolean estaPagado;
}
