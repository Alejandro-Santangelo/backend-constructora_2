package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.List;

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

    // ========== CAMPOS DE ADELANTOS ==========
    
    /**
     * Indica si este pago es un adelanto (true) o un pago regular (false)
     */
    private Boolean esAdelantoRegistrado; // Campo de BD: es_adelanto

    /**
     * Tipo/período del adelanto: 1_SEMANA, 2_SEMANAS, 1_MES, OBRA_COMPLETA
     */
    private String periodoAdelanto;

    /**
     * Estado del adelanto: ACTIVO, COMPLETADO, CANCELADO
     */
    private String estadoAdelanto;

    /**
     * Saldo pendiente por descontar del adelanto.
     */
    private BigDecimal saldoAdelantoPorDescontar;

    /**
     * Monto original del adelanto para referencia histórica
     */
    private BigDecimal montoOriginalAdelanto;

    /**
     * IDs de adelantos que fueron descontados en este pago regular.
     * Obtenidos desde tabla relacional pago_adelantos_aplicados.
     */
    private List<Long> adelantosAplicadosIds;

    /**
     * Fecha de referencia de la semana del adelanto
     */
    private LocalDate semanaReferencia;

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
