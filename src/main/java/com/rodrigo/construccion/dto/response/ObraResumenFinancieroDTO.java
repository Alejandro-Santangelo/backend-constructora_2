package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para el resumen financiero de una obra
 * Consolida información de presupuesto, cobros y pagos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObraResumenFinancieroDTO {

    private Long obraId;
    private String nombreObra;
    private String direccionObra;

    // Presupuesto
    private BigDecimal presupuestoEstimado;
    private BigDecimal totalPresupuestoConHonorarios; // Del presupuesto origen

    // Cobros al cliente
    private BigDecimal totalCobrado;
    private BigDecimal totalPendienteCobro;
    private BigDecimal totalVencido;
    private Integer cantidadCobrosPendientes;
    private Integer cantidadCobrosRealizados;

    // Pagos a profesionales
    private BigDecimal totalPagadoProfesionales;
    private BigDecimal totalPagosSemanales;
    private BigDecimal totalAdelantos;
    private BigDecimal totalPremiosBonos;
    private BigDecimal adelantosPendientesDescuento;

    // Pagos de materiales y gastos generales
    private BigDecimal totalPagadoMateriales;
    private BigDecimal totalPagadoGastosGenerales;
    private BigDecimal totalPagadoGeneral; // Suma de profesionales + materiales + gastos generales

    // Gastos de caja chica
    private BigDecimal totalGastosCajaChica;

    // Balances
    private BigDecimal saldoObra; // cobrado - (pagado + gastos)
    private BigDecimal margenActual; // cobrado - totalPagadoProfesionales
    private BigDecimal porcentajeEjecucionPresupuesto; // (cobrado / presupuesto) * 100
    private BigDecimal porcentajeEjecucionPagos; // (pagado / presupuesto) * 100

    // Estado general
    private String estadoFinanciero; // POSITIVO, NEUTRAL, NEGATIVO
    private Boolean tieneCobrosPendientes;
    private Boolean tieneCobrosVencidos;
}
