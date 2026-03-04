package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoProfesionalObraRequestDTO {

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    @NotNull(message = "El ID del profesional en obra es obligatorio")
    private Long profesionalObraId;

    @NotNull(message = "El tipo de pago es obligatorio")
    private String tipoPago; // SEMANAL, ADELANTO, PREMIO, BONO, AJUSTE, LIQUIDACION_FINAL

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate fechaPago;

    // Período (para pagos semanales)
    private LocalDate periodoDesde;
    private LocalDate periodoHasta;

    // Semana (para pagos semanales)
    private Integer semana;

    // Montos - Frontend requirements
    // Nota: montoBruto O montoPagado deben venir (se valida en el servicio)
    // @Positive removido: permite null/0, validación manual en servicio
    private BigDecimal montoBruto; // Monto base antes de descuentos
    
    // @Positive removido: permite null/0, validación manual en servicio
    private BigDecimal montoPagado; // Alias para compatibilidad (se mapea a montoBruto si viene)

    @PositiveOrZero(message = "El descuento de adelantos debe ser mayor o igual a cero")
    private BigDecimal descuentoAdelantos;

    @PositiveOrZero(message = "El descuento de presentismo debe ser mayor o igual a cero")
    private BigDecimal descuentoPresentismo;

    private BigDecimal montoNeto; // Calculado automáticamente: montoBruto - descuentoAdelantos - descuentoPresentismo

    // Montos - Legacy (compatibilidad)
    private BigDecimal montoBase; // Backward compatibility
    private BigDecimal ajustes;
    private BigDecimal montoFinal; // Backward compatibility

    // ========== CAMPOS DE ADELANTOS ==========
    
    /**
     * Indica si este pago es un adelanto (true) o un pago regular (false)
     */
    private Boolean esAdelanto = false;

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
     * Empieza igual a montoFinal y se va reduciendo con cada descuento.
     */
    private BigDecimal saldoAdelantoPorDescontar;

    /**
     * Monto original del adelanto para referencia histórica
     */
    private BigDecimal montoOriginalAdelanto;

    // NOTA: adelantosAplicadosIds se maneja automáticamente mediante tabla relacional
    // No se envía desde el frontend

    /**
     * Fecha de referencia de la semana del adelanto
     */
    private LocalDate semanaReferencia;

    // Datos de premios/bonos
    private String premioTipo; // FIJO, PORCENTAJE
    private BigDecimal premioValor;
    private BigDecimal premioBase;
    private String premioConcepto;

    // Presentismo
    private Integer diasTrabajados;
    private Integer diasEsperados;
    private BigDecimal porcentajePresentismo; // Será calculado si no se proporciona

    // Información del comprobante
    private String numeroRecibo;
    private String metodoPago; // TRANSFERENCIA, EFECTIVO, CHEQUE
    private String numeroComprobante;

    private String estado; // PAGADO, ANULADO (por defecto PAGADO)

    // Campos adicionales
    private String concepto;
    private String observaciones;
    private String comprobanteUrl;
}
