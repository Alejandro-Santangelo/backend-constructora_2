package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para adelantos
 */
@Schema(description = "Datos de respuesta de un adelanto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdelantoResponseDTO {

    @Schema(description = "ID del adelanto", example = "789")
    private Long id;

    @Schema(description = "ID de la asignación profesional-obra", example = "123")
    private Long profesionalObraId;

    @Schema(description = "ID del profesional", example = "45")
    private Long profesionalId;

    @Schema(description = "Nombre del profesional", example = "Juan Carlos Pérez")
    private String nombreProfesional;

    @Schema(description = "Tipo de profesional", example = "Oficial Albañil")
    private String tipoProfesional;

    @Schema(description = "ID de la obra", example = "67")
    private Long obraId;

    @Schema(description = "Nombre de la obra", example = "Obra Central")
    private String nombreObra;

    @Schema(description = "ID de la empresa", example = "1")
    private Long empresaId;

    @Schema(description = "Monto original del adelanto", example = "50000.00")
    private BigDecimal montoOriginal;

    @Schema(description = "Saldo pendiente por descontar", example = "30000.00")
    private BigDecimal saldoPendiente;

    @Schema(description = "Monto ya descontado", example = "20000.00")
    private BigDecimal montoDescontado;

    @Schema(description = "Fecha del adelanto", example = "2024-03-15")
    private LocalDate fechaPago;

    @Schema(description = "Período del adelanto: 1_SEMANA, 2_SEMANAS, 1_MES, OBRA_COMPLETA", example = "1_SEMANA")
    private String periodoAdelanto;

    @Schema(description = "Estado: ACTIVO, COMPLETADO, CANCELADO", example = "ACTIVO")
    private String estado;

    @Schema(description = "Motivo/concepto del adelanto", example = "Adelanto por emergencia médica")
    private String motivo;

    @Schema(description = "Observaciones", example = "Se descontará en 4 pagos semanales")
    private String observaciones;

    @Schema(description = "Método de pago", example = "efectivo")
    private String metodoPago;

    @Schema(description = "Número de comprobante", example = "ADL-2024-001")
    private String numeroComprobante;

    @Schema(description = "Aprobado por", example = "Juan Pérez")
    private String aprobadoPor;

    @Schema(description = "Fecha de creación", example = "2024-03-15T10:30:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación", example = "2024-03-15T10:30:00")
    private LocalDateTime fechaModificacion;

    @Schema(description = "Porcentaje descontado", example = "40.00")
    private BigDecimal porcentajeDescontado;

    @Schema(description = "¿Está completamente descontado?", example = "false")
    private Boolean completamenteDescontado;

    @Schema(description = "ID del presupuesto asociado", example = "45")
    private Long presupuestoNoClienteId;

    // Datos adicionales del profesional
    @Schema(description = "Email del profesional", example = "juan.perez@email.com")
    private String emailProfesional;

    @Schema(description = "Teléfono del profesional", example = "+54 11 1234-5678")
    private String telefonoProfesional;

    @Schema(description = "CUIT del profesional", example = "20-12345678-9")
    private String cuitProfesional;
    
    @Schema(description = "Advertencia si excede límite recomendado (50% del total)", example = "ADVERTENCIA: Este adelanto excede el 50% del monto total asignado")
    private String advertencia;
    
    @Schema(description = "¿Excede el límite recomendado del 50%?", example = "true")
    private Boolean excedeLimiteRecomendado;
}
