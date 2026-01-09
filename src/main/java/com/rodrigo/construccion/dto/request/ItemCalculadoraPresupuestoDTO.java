package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para transferir datos de items de la calculadora de presupuestos.
 * Soporta dos modos: automático (jornales) y manual (total directo).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemCalculadoraPresupuestoDTO {

    @Schema(description = "ID del item (solo en respuestas, ignorado en creación)", example = "123")
    private Long id;

    @Schema(description = "Descripción del trabajo o tipo de profesional", 
            example = "colocacion de durlock", 
            required = true)
    @NotNull(message = "El tipo de profesional es obligatorio")
    private String tipoProfesional;

    @Schema(description = "Descripción detallada del rubro", 
            example = "Colocación de durlock en paredes interiores del dormitorio")
    private String descripcion;

    @Schema(description = "Observaciones adicionales del rubro", 
            example = "Incluye estructura metálica y cinta para juntas")
    private String observaciones;

    @Schema(description = "Cantidad de jornales/días de trabajo. NULL en modo manual.", 
            example = "2")
    private BigDecimal cantidadJornales;

    @Schema(description = "Precio por jornal. NULL en modo manual.", 
            example = "60000")
    private BigDecimal importeJornal;

    @Schema(description = "Resultado de cantidad_jornales × importe_jornal. 0 en modo manual.", 
            example = "120000")
    private BigDecimal subtotalManoObra;

    @Schema(description = "Importe de materiales asociados al item. NULL si no aplica.", 
            example = "120000")
    private BigDecimal materiales;

    @Schema(description = "Total ingresado manualmente. Solo tiene valor cuando esModoManual = true.", 
            example = "350000")
    private BigDecimal totalManual;
    
    @Schema(description = "Descripción específica para el total manual", 
            example = "Trabajo completo de instalación eléctrica")
    private String descripcionTotalManual;
    
    @Schema(description = "Observaciones específicas para el total manual", 
            example = "Incluye materiales y mano de obra")
    private String observacionesTotalManual;

    @Schema(description = "Total final del item", 
            example = "240000")
    private BigDecimal total;

    @Schema(description = "true = total ingresado manualmente, false = calculado con jornales", 
            example = "false", 
            required = true)
    @NotNull(message = "El modo (manual/automático) es obligatorio")
    private Boolean esModoManual;

    @Schema(description = "Indica si este ítem debe incluirse en el cálculo automático de días hábiles", 
            example = "true",
            defaultValue = "true")
    private Boolean incluirEnCalculoDias;

    @Schema(description = "Indica si el rubro/item se incluye en el cálculo automático de días hábiles del presupuesto", 
            example = "true",
            defaultValue = "true")
    @com.fasterxml.jackson.annotation.JsonProperty("trabajaEnParalelo")
    private Boolean trabajaEnParalelo;

    @Schema(description = "Array de profesionales desglosados para este item")
    private List<ProfesionalCalculadoraDTO> profesionales;

    @Schema(description = "Array de materiales desglosados para este item")  
    private List<MaterialCalculadoraDTO> materialesLista;

    @Schema(description = "Subtotal calculado de los materiales desglosados", example = "87500.00")
    private BigDecimal subtotalMateriales;

    @Schema(description = "Array de jornales desglosados para este item")
    private List<JornalCalculadoraDTO> jornales;

    // ============================================================================
    // CAMPOS PARA GASTOS GENERALES (RELACIONAL - NO JSON)
    // ============================================================================

    @Schema(description = "Indica si este item representa gastos generales", 
            example = "true",
            defaultValue = "false")
    private Boolean esGastoGeneral = false;

    @Schema(description = "Array de gastos generales individuales para este item")
    private List<GastoGeneralDTO> gastosGenerales;

    @Schema(description = "Suma total de todos los gastos generales", 
            example = "700000")
    private BigDecimal subtotalGastosGenerales;

    @Schema(description = "Descripción general opcional para los gastos generales", 
            example = "Gastos varios de obra")
    private String descripcionGastosGenerales;

    @Schema(description = "Observaciones adicionales para los gastos generales", 
            example = "Precios sujetos a variación según proveedor")
    private String observacionesGastosGenerales;

    // ============================================================================
    // CAMPOS DE DESCRIPCIÓN/OBSERVACIONES POR CATEGORÍA
    // ============================================================================

    @Schema(description = "Descripción general para los profesionales del rubro", 
            example = "Mano de obra especializada en pintura")
    private String descripcionProfesionales;

    @Schema(description = "Observaciones adicionales para los profesionales", 
            example = "Requiere experiencia mínima de 3 años")
    private String observacionesProfesionales;

    @Schema(description = "Descripción general para los materiales del rubro", 
            example = "Materiales de primera calidad")
    private String descripcionMateriales;

    @Schema(description = "Observaciones adicionales para los materiales", 
            example = "Marcas aprobadas por el cliente")
    private String observacionesMateriales;

}
