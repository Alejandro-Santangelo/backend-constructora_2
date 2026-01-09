package com.rodrigo.construccion.dto.response;

import com.rodrigo.construccion.enums.EstadoPagoTrabajoExtra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta con resumen de pagos de un trabajo extra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenPagosTrabajoExtraDTO {

    private Long trabajoExtraId;
    private String trabajoExtraNombre;

    // Totales
    private BigDecimal importeTotalProfesionales;
    private BigDecimal importeTotalTareas;
    private BigDecimal importeTotal; // profesionales + tareas

    private BigDecimal montoPagadoProfesionales;
    private BigDecimal montoPagadoTareas;
    private BigDecimal montoPagadoTotal; // profesionales + tareas

    private BigDecimal montoPendiente; // importeTotal - montoPagadoTotal

    // Estados
    private EstadoPagoTrabajoExtra estadoPagoGeneral;
    private String estadoPagoGeneralDisplay;

    // Contadores
    private Long totalPagos;
    private Long totalPagosProfesionales;
    private Long totalPagosTareas;
    private Long totalPagosGenerales;

    // Profesionales
    private Long totalProfesionales;
    private Long profesionalesPendientes;
    private Long profesionalesPagadosParcial;
    private Long profesionalesPagadosTotal;

    // Tareas
    private Long totalTareas;
    private Long tareasPendientes;
    private Long tareasPagadasParcial;
    private Long tareasPagadasTotal;

    // Porcentajes
    private BigDecimal porcentajePagado; // (montoPagadoTotal / importeTotal) * 100
}
