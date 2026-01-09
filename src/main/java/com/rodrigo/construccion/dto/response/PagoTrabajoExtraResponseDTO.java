package com.rodrigo.construccion.dto.response;

import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.enums.MetodoPago;
import com.rodrigo.construccion.enums.TipoPagoTrabajoExtra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para pagos de trabajos extra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoTrabajoExtraResponseDTO {

    private Long id;
    private Long trabajoExtraId;
    private String trabajoExtraNombre;
    private Long obraId;
    private String obraNombre;
    private Long presupuestoNoClienteId;
    private Long empresaId;

    private TipoPagoTrabajoExtra tipoPago;
    private String tipoPagoDisplay;

    // Referencias específicas
    private Long trabajoExtroProfesionalId;
    private String profesionalNombre;
    private Long trabajoExtraTareaId;
    private String tareaDescripcion;

    private String concepto;
    private BigDecimal montoBase;
    private BigDecimal descuentos;
    private BigDecimal bonificaciones;
    private BigDecimal montoFinal;

    private LocalDate fechaPago;
    private LocalDate fechaEmision;
    private EstadoPago estado;
    private String estadoDisplay;

    private MetodoPago metodoPago;
    private String metodoPagoDisplay;
    private String numeroComprobante;
    private String comprobanteUrl;

    private String observaciones;
    private String motivoAnulacion;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long usuarioCreacionId;
    private Long usuarioModificacionId;
}
