package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElementoCatalogoDTO {

    @Schema(description = "Tipo de elemento: MATERIAL, GASTO_GENERAL, JORNAL, PROFESIONAL", example = "MATERIAL")
    private String tipo;

    @Schema(description = "Nombre del elemento", example = "Cemento")
    private String nombre;

    @Schema(description = "Descripción del elemento", example = "Cemento Portland")
    private String descripcion;

    @Schema(description = "Categoría o Rubro del elemento", example = "Albañilería")
    private String categoria;

    @Schema(description = "Cantidad (generalmente 1 para catálogo)", example = "1")
    private Integer cantidad;

    @Schema(description = "Precio unitario (para Materiales/Gastos)", example = "5000")
    private BigDecimal precioUnitario;

    @Schema(description = "Valor unitario (para Jornales/Profesionales)", example = "8000")
    private BigDecimal valorUnitario;

    @Schema(description = "Unidad de medida (para Materiales/Gastos)", example = "bolsa")
    private String unidadMedida;

    @Schema(description = "Unidad (para Profesionales)", example = "jornales")
    private String unidad;

    @Schema(description = "Rol (para Jornales)", example = "Oficial Albañil")
    private String rol;

    @Schema(description = "Tipo de Profesional (para Profesionales)", example = "Electricista")
    private String tipoProfesional;

    @Schema(description = "Nombre del Profesional asociado (si aplica)", example = "Juan Pérez")
    private String nombreProfesional;

    @Schema(description = "Teléfono (para Profesionales)", example = "1234567890")
    private String telefono;

    @Schema(description = "Rubro de origen del presupuesto", example = "Albañilería")
    private String rubroOrigen;
}
