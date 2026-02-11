package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar un profesional desglosado dentro de un item de calculadora.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profesional desglosado de un item de calculadora")
public class ProfesionalCalculadoraDTO {

    @Schema(description = "ID único del profesional en el frontend", example = "1730882345678")
    private Long id;

    @Schema(description = "ID de la asignación profesional-obra (se asigna al aprobar el presupuesto)", example = "789")
    private Long profesionalObraId;

    @Schema(description = "Tipo de profesional", example = "Oficial Pintor", requiredMode = Schema.RequiredMode.REQUIRED)  
    private String tipo;

    @Schema(description = "Nombre del profesional", example = "Juan Pérez")
    private String nombre;

    @Schema(description = "Indica si es un item global del presupuesto híbrido", example = "false")
    private Boolean esGlobal = false;

    @Schema(description = "Descripción del trabajo específico", example = "Pintura de paredes y techos")
    private String descripcion;

    @Schema(description = "Observaciones sobre el profesional", example = "Requiere andamios")
    private String observaciones;

    @Schema(description = "Teléfono del profesional", example = "+54911234567")
    private String telefono;

    @Schema(description = "Unidad de medida", example = "jornales", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unidad;

    @Schema(description = "Cantidad de jornales", example = "2.0")
    private BigDecimal cantidadJornales;

    @Schema(description = "Importe por jornal", example = "60000.00")
    private BigDecimal importeJornal;

    @Schema(description = "Subtotal calculado (cantidadJornales * importeJornal)", example = "120000.00")
    private BigDecimal subtotal;

    @Schema(description = "Indica si no tiene cantidad definida", example = "true")
    private Boolean sinCantidad;

    @Schema(description = "Indica si no tiene importe definido", example = "true") 
    private Boolean sinImporte;
}