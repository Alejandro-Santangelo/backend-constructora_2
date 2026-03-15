package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta para profesionales asignados a obra con datos financieros
 * Usado específicamente para el sistema de adelantos y pagos
 */
@Schema(description = "Datos financieros de un profesional asignado a una obra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalObraFinancieroDTO {

    @Schema(description = "ID de la asignación profesional-obra", example = "123")
    private Long id;

    @Schema(description = "ID de la asignación (alias de id)", example = "123")
    private Long profesionalObraId;

    @Schema(description = "ID del profesional", example = "45")
    private Long profesionalId;

    @Schema(description = "Nombre completo del profesional", example = "Juan Carlos Pérez")
    private String nombre;

    @Schema(description = "Nombre completo del profesional (alias)", example = "Juan Carlos Pérez")
    private String nombreCompleto;

    @Schema(description = "Tipo de profesional", example = "Oficial Albañil")
    private String tipoProfesional;

    @Schema(description = "Tipo de profesional (alias)", example = "Oficial Albañil")
    private String tipo;

    @Schema(description = "Rol en la obra", example = "Oficial")
    private String rolEnObra;

    @Schema(description = "Cantidad de jornales asignados (puede ser decimal)", example = "20.5")
    private BigDecimal cantidadJornales;

    @Schema(description = "Precio por jornal", example = "15000.00")
    private BigDecimal precioJornal;

    @Schema(description = "Precio por jornal (alias)", example = "15000.00")
    private BigDecimal jornal;

    @Schema(description = "Importe por jornal (alias)", example = "15000.00")
    private BigDecimal importeJornal;

    @Schema(description = "Precio total asignado (cantidadJornales × precioJornal)", example = "300000.00")
    private BigDecimal precioTotal;

    @Schema(description = "Precio total asignado (alias)", example = "300000.00")
    private BigDecimal precio;

    @Schema(description = "Monto total asignado (alias)", example = "300000.00")
    private BigDecimal montoTotal;

    @Schema(description = "Jornales utilizados hasta el momento (puede ser decimal)", example = "5.5")
    private BigDecimal jornalesUtilizados;

    @Schema(description = "Fecha de inicio de la asignación", example = "2024-01-15")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de fin de la asignación", example = "2024-02-15")
    private LocalDate fechaFin;

    @Schema(description = "Estado de la asignación", example = "activo")
    private String estado;

    @Schema(description = "Modalidad de la asignación", example = "jornales")
    private String modalidad;

    @Schema(description = "ID de la empresa", example = "1")
    private Long empresaId;

    @Schema(description = "ID de la obra", example = "67")
    private Long obraId;

    @Schema(description = "Nombre de la obra", example = "Obra Central")
    private String nombreObra;

    @Schema(description = "Observaciones", example = "Profesional con experiencia")
    private String observaciones;

    @Schema(description = "Email del profesional", example = "juan.perez@email.com")
    private String email;

    @Schema(description = "Teléfono del profesional", example = "+54 11 1234-5678")
    private String telefono;

    @Schema(description = "Especialidad del profesional", example = "Albañilería fina")
    private String especialidad;

    @Schema(description = "CUIT del profesional", example = "20-12345678-9")
    private String cuit;

    @Schema(description = "Categoría del profesional: EMPLEADO, INDEPENDIENTE, CONTRATISTA", example = "INDEPENDIENTE")
    private String categoria;

    @Schema(description = "Total pagado al profesional en esta obra", example = "150000.00")
    private BigDecimal totalPagado;

    @Schema(description = "Total de adelantos otorgados", example = "50000.00")
    private BigDecimal totalAdelantos;

    @Schema(description = "Saldo pendiente de pago", example = "100000.00")
    private BigDecimal saldoPendiente;
}
