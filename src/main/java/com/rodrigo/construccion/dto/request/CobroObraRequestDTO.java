package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CobroObraRequestDTO {

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    // obraId es opcional - se puede crear cobro solo con presupuestoNoClienteId
    private Long obraId;

    @NotNull(message = "El ID del presupuesto es obligatorio")
    private Long presupuestoNoClienteId;

    // ========== CAMPOS DE DIRECCIÓN ==========
    // Estos campos se copian desde el presupuesto para permitir búsquedas directas
    private String direccionObraBarrio;
    private String direccionObraCalle;
    private String direccionObraAltura;
    private String direccionObraTorre;
    private String direccionObraPiso;
    private String direccionObraDepartamento;

    // ========== DATOS DEL COBRO ==========
    private String tipoCobro; // ANTICIPO, CERTIFICADO, PAGO_FINAL, AJUSTE

    private BigDecimal montoCobrar;

    private BigDecimal montoCobrado;

    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de cobro es obligatoria")
    private LocalDate fechaCobro;

    private LocalDate fechaVencimiento;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    // Frontend envía "descripcion" - se mapea a "concepto" en la entidad
    private String descripcion;

    // Frontend envía "numeroComprobante" - se mapea según tipo
    private String numeroComprobante;

    private String numeroFactura;

    private String tipoComprobante; // FACTURA_A, FACTURA_B, RECIBO, etc.

    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago; // TRANSFERENCIA, EFECTIVO, CHEQUE, TARJETA

    private String estado; // PENDIENTE, COBRADO, VENCIDO, ANULADO

    private String observaciones;

    private String comprobanteUrl;

    // ========== DISTRIBUCIÓN POR ÍTEMS ==========
    private String modoDistribucion; // GENERAL o POR_ITEMS

    private BigDecimal montoProfesionales;

    private BigDecimal montoMateriales;

    private BigDecimal montoGastosGenerales;

    private BigDecimal porcentajeProfesionales;

    private BigDecimal porcentajeMateriales;

    private BigDecimal porcentajeGastosGenerales;

    // ========== ASIGNACIONES A OBRAS ==========
    // Lista OPCIONAL de asignaciones para crear el cobro con distribución en un solo POST
    private List<AsignacionCobroObraRequestDTO> asignaciones;
}
