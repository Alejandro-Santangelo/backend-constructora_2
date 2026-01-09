package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CobroObraResponseDTO {

    private Long id;
    private Long obraId;
    private String nombreObra;
    private String direccionObra;
    private Long presupuestoNoClienteId;
    private Long empresaId;

    // ========== CAMPOS DE DIRECCIÓN ==========
    private String calle;
    private String altura;
    private String barrio;
    private String torre;
    private String piso;
    private String depto;

    // ========== DATOS DEL COBRO ==========
    private String tipoCobro; // ANTICIPO, CERTIFICADO, PAGO_FINAL, AJUSTE
    private BigDecimal montoCobrar;
    private BigDecimal montoCobrado;
    private LocalDate fechaEmision;
    private LocalDate fechaCobro;
    private LocalDate fechaVencimiento;
    private BigDecimal monto;
    private String concepto;

    // Información del comprobante
    private String numeroRecibo;
    private String numeroFactura;
    private String tipoComprobante;

    // Método de pago y estado
    private String metodoPago;
    private String estado;

    // Campos adicionales
    private String observaciones;
    private String motivoAnulacion;
    private String comprobanteUrl;

    // Auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long usuarioCreacionId;
    private Long usuarioModificacionId;

    // Información calculada
    private Boolean esPendiente;
    private Boolean estaCobrado;
    private Boolean estaVencido;
    private Boolean tieneVencimiento;

    // ========== DISTRIBUCIÓN POR ÍTEMS ==========
    private String modoDistribucion;
    private BigDecimal montoProfesionales;
    private BigDecimal montoMateriales;
    private BigDecimal montoGastosGenerales;
    private BigDecimal porcentajeProfesionales;
    private BigDecimal porcentajeMateriales;
    private BigDecimal porcentajeGastosGenerales;

    // ========== ASIGNACIONES A OBRAS ==========
    // Lista de asignaciones creadas con este cobro
    private List<AsignacionCobroObraResponseDTO> asignaciones;
}
