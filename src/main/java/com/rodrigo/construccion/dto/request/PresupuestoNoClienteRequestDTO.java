package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PresupuestoNoClienteRequestDTO {

    public PresupuestoNoClienteRequestDTO() {}

    // Campos simples
    @Schema(description = "ID de la empresa", example = "1")
    private Long idEmpresa;

    @Schema(description = "ID del cliente asociado al presupuesto (opcional)", example = "1")
    private Long idCliente;

    @Schema(description = "ID de la obra asociada al presupuesto (opcional para tipo TAREA_LEVE)", example = "1")
    private Long idObra;
    
    @Schema(description = "ID del trabajo adicional asociado al presupuesto (NUEVA FUNCIONALIDAD - solo para tipo TAREA_LEVE)", example = "5")
    private Long trabajoAdicionalId;
    
    @Schema(description = "Tipo de presupuesto: TRADICIONAL, TRABAJO_DIARIO, TRABAJO_EXTRA, TAREA_LEVE", example = "TRADICIONAL")
    private String tipoPresupuesto;
    
    @Schema(description = "Indica si el presupuesto es para un trabajo extra", example = "false")
    private Boolean esPresupuestoTrabajoExtra = false;
    
    @Schema(description = "Numero de presupuesto (opcional). Si se provee, se crea una nueva version para ese numero")
    private Long numeroPresupuesto;

    @Schema(description = "Nombre del solicitante (nullable)")
    private String nombreSolicitante;

    @Schema(description = "Direcci\u00f3n particular (nullable)")
    private String direccionParticular;

    // Direccion de la obra desglosada
    @Schema(description = "Calle de la obra")
    @NotBlank(message = "direccionObraCalle es obligatorio")
    private String direccionObraCalle;

    @Schema(description = "Altura de la obra")
    @NotBlank(message = "direccionObraAltura es obligatorio")
    private String direccionObraAltura;

    @Schema(description = "Piso de la obra (nullable)")
    private String direccionObraPiso;

    @Schema(description = "Departamento de la obra (nullable) puede ser letra o numero")
    private String direccionObraDepartamento;

    @Schema(description = "Barrio de la obra (nullable)")
    private String direccionObraBarrio;

    @Schema(description = "Torre o edificio de la obra (nullable)")
    private String direccionObraTorre;

    @Schema(description = "Descripcion (nullable)")
    private String descripcion;

    // Campos adicionales de descripción y observaciones
    @Schema(description = "Descripción técnica detallada del presupuesto (nullable)")
    private String descripcionDetallada;

    @Schema(description = "Notas internas para el equipo de trabajo (nullable)")
    private String observacionesInternas;

    @Schema(description = "Comentarios adicionales sobre el presupuesto (nullable)")
    private String notasAdicionales;

    @Schema(description = "Nombre de la obra asociada al presupuesto")
    @NotBlank(message = "El nombre de la obra es obligatorio")
    @jakarta.validation.constraints.Size(max = 255, message = "El nombre de la obra no puede superar 255 caracteres")
    private String nombreObra;

    @Schema(description = "Fecha de creación del presupuesto (yyyy-MM-dd)")
    private LocalDate fechaCreacion;


    @Schema(description = "Detalles técnicos específicos del proyecto (nullable)")
    private String especificacionesTecnicas;

    @Schema(description = "Observaciones y comentarios del cliente (nullable)")
    private String comentariosCliente;

    @Schema(description = "Requerimientos particulares del proyecto (nullable)")
    private String requisitosEspeciales;

    // Nuevos campos estructurados
    @Schema(description = "Profesionales necesarios. Lista con estructura")
    private List<ProfesionalNecesarioDTO> profesionales;

    @Schema(description = "Materiales. Lista con estructura")
    private List<MaterialDTO> materialesList;

    @Schema(description = "Tiempo estimado de terminacion (en dias) (nullable)")
    private Integer tiempoEstimadoTerminacion;


    @Schema(description = "Elementos nuevos para agregar al catálogo (Materiales, Profesionales, Gastos)")
    private List<ElementoCatalogoDTO> elementosParaCatalogo;

    @Schema(description = "Estado del presupuesto (nullable). Valores válidos: A enviar, Borrador, Modificado, Enviado, APROBADO, EN EJECUCION, TERMINADO. " +
                         "Si no se especifica, se asigna automáticamente según tipo de presupuesto: TRADICIONAL → 'A enviar', TRABAJOS_SEMANALES → 'APROBADO'",
            example = "A enviar",
            allowableValues = {"A enviar", "Borrador", "Modificado", "Enviado", "APROBADO", "EN EJECUCION", "TERMINADO"})
    private String estado;

    @Schema(description = "Fecha probable de inicio (nullable)")
    private LocalDate fechaProbableInicio;

    @Schema(description = "Telefono (nullable)")
    private String telefono;

    @Schema(description = "Mail (nullable)")
    private String mail;

    @Schema(description = "Vencimiento (nullable)")
    private LocalDate vencimiento;

    @Schema(description = "Observaciones (texto largo, nullable)")
    private String observaciones;

    @Schema(description = "Honorarios de dirección de obra - Valor fijo", example = "10000.00")
    private Double honorarioDireccionValorFijo;

    @Schema(description = "Honorarios de dirección de obra - Porcentaje sobre total de costos", example = "10.5")
    private Double honorarioDireccionPorcentaje;

    // ========== CONFIGURACIÓN DE HONORARIOS ==========
    @Schema(description = "Si true, aplica honorariosValorGeneral a todos los rubros", example = "true")
    private Boolean honorariosAplicarATodos;
    
    @Schema(description = "Valor general de honorarios cuando aplicarATodos es true", example = "15.5")
    private java.math.BigDecimal honorariosValorGeneral;
    
    @Schema(description = "Tipo de honorario general: porcentaje o fijo", example = "porcentaje")
    private String honorariosTipoGeneral;
    
    // Honorarios Profesionales
    @Schema(description = "Si los honorarios de profesionales están activos", example = "true")
    private Boolean honorariosProfesionalesActivo;
    
    @Schema(description = "Tipo de honorario para profesionales: porcentaje o fijo", example = "porcentaje")
    private String honorariosProfesionalesTipo;
    
    @Schema(description = "Valor del honorario para profesionales", example = "10.0")
    private java.math.BigDecimal honorariosProfesionalesValor;
    
    // Honorarios Materiales
    @Schema(description = "Si los honorarios de materiales están activos", example = "true")
    private Boolean honorariosMaterialesActivo;
    
    @Schema(description = "Tipo de honorario para materiales: porcentaje o fijo", example = "porcentaje")
    private String honorariosMaterialesTipo;
    
    @Schema(description = "Valor del honorario para materiales", example = "8.0")
    private java.math.BigDecimal honorariosMaterialesValor;
    
    // Honorarios Otros Costos
    @Schema(description = "Si los honorarios de otros costos están activos", example = "true")
    private Boolean honorariosOtrosCostosActivo;
    
    @Schema(description = "Tipo de honorario para otros costos: porcentaje o fijo", example = "fijo")
    private String honorariosOtrosCostosTipo;
    
    @Schema(description = "Valor del honorario para otros costos", example = "50000.00")
    private java.math.BigDecimal honorariosOtrosCostosValor;
    
    // Honorarios Jornales
    @Schema(description = "Si los honorarios de jornales están activos", example = "true")
    private Boolean honorariosJornalesActivo;
    
    @Schema(description = "Tipo de honorario para jornales: porcentaje o fijo", example = "porcentaje")
    private String honorariosJornalesTipo;
    
    @Schema(description = "Valor del honorario para jornales", example = "10.0")
    private java.math.BigDecimal honorariosJornalesValor;
    
    // Honorarios Configuración Presupuesto
    @Schema(description = "Si los honorarios de configuración de presupuesto están activos", example = "true")
    private Boolean honorariosConfiguracionPresupuestoActivo;
    
    @Schema(description = "Tipo de honorario para configuración de presupuesto: porcentaje o fijo", example = "porcentaje")
    private String honorariosConfiguracionPresupuestoTipo;
    
    @Schema(description = "Valor del honorario para configuración de presupuesto", example = "5.0")
    private java.math.BigDecimal honorariosConfiguracionPresupuestoValor;

    // ========== CONFIGURACIÓN DE CÁLCULO DE DÍAS HÁBILES ==========
    @Schema(description = "Indica si el cálculo de días hábiles es automático (true) o manual (false)", example = "false")
    private Boolean calculoAutomaticoDiasHabiles;

    // ========== CONFIGURACIÓN DE MAYORES COSTOS ==========
    @Schema(description = "Si true, aplica mayoresCostosValorGeneral a todos los rubros", example = "true")
    private Boolean mayoresCostosAplicarValorGeneral;
    
    @Schema(description = "Valor general de mayores costos cuando aplicarValorGeneral es true", example = "15.5")
    private Double mayoresCostosValorGeneral;
    
    @Schema(description = "Tipo de mayor costo general: porcentaje o fijo", example = "porcentaje")
    private String mayoresCostosTipoGeneral;
    
    @Schema(description = "Indica si el mayor costo general fue importado desde un rubro", example = "true")
    private Boolean mayoresCostosGeneralImportado;
    
    @Schema(description = "ID del rubro desde el cual se importó la configuración", example = "997")
    private String mayoresCostosRubroImportado;
    
    @Schema(description = "Nombre del rubro desde el cual se importó la configuración", example = "plomeria")
    private String mayoresCostosNombreRubroImportado;
    
    // Mayores Costos Profesionales
    @Schema(description = "Si los mayores costos de profesionales están activos", example = "true")
    private Boolean mayoresCostosProfesionalesActivo;
    
    @Schema(description = "Tipo de mayor costo para profesionales: porcentaje o fijo", example = "porcentaje")
    private String mayoresCostosProfesionalesTipo;
    
    @Schema(description = "Valor del mayor costo para profesionales", example = "10.0")
    private Double mayoresCostosProfesionalesValor;
    
    // Mayores Costos Materiales
    @Schema(description = "Si los mayores costos de materiales están activos", example = "true")
    private Boolean mayoresCostosMaterialesActivo;
    
    @Schema(description = "Tipo de mayor costo para materiales: porcentaje o fijo", example = "porcentaje")
    private String mayoresCostosMaterialesTipo;
    
    @Schema(description = "Valor del mayor costo para materiales", example = "10.0")
    private Double mayoresCostosMaterialesValor;
    
    // Mayores Costos Otros Costos
    @Schema(description = "Si los mayores costos de otros costos están activos", example = "true")
    private Boolean mayoresCostosOtrosCostosActivo;
    
    @Schema(description = "Tipo de mayor costo para otros costos: porcentaje o fijo", example = "porcentaje")
    private String mayoresCostosOtrosCostosTipo;
    
    @Schema(description = "Valor del mayor costo para otros costos", example = "10.0")
    private Double mayoresCostosOtrosCostosValor;
    
    // Mayores Costos Jornales
    @Schema(description = "Si los mayores costos de jornales están activos", example = "true")
    private Boolean mayoresCostosJornalesActivo;
    
    @Schema(description = "Tipo de mayor costo para jornales: porcentaje o fijo", example = "porcentaje")
    private String mayoresCostosJornalesTipo;
    
    @Schema(description = "Valor del mayor costo para jornales", example = "10.0")
    private Double mayoresCostosJornalesValor;
    
    // Mayores Costos Configuracion Presupuesto
    @Schema(description = "Si los mayores costos de configuración de presupuesto están activos", example = "true")
    private Boolean mayoresCostosConfiguracionPresupuestoActivo;
    
    @Schema(description = "Tipo de mayor costo para configuración de presupuesto: porcentaje o fijo", example = "porcentaje")
    private String mayoresCostosConfiguracionPresupuestoTipo;
    
    @Schema(description = "Valor del mayor costo para configuración de presupuesto", example = "10.0")
    private Double mayoresCostosConfiguracionPresupuestoValor;
    
    // Mayores Costos Honorarios
    @Schema(description = "Si los mayores costos de honorarios están activos", example = "true")
    private Boolean mayoresCostosHonorariosActivo;
    
    @Schema(description = "Tipo de mayor costo para honorarios: porcentaje o fijo", example = "porcentaje")
    private String mayoresCostosHonorariosTipo;
    
    @Schema(description = "Valor del mayor costo para honorarios", example = "10.0")
    private Double mayoresCostosHonorariosValor;
    
    // Explicación/justificación INTERNA de mayores costos (NO visible para cliente en PDFs)
    @Schema(description = "Explicación o justificación INTERNA de por qué se aplican mayores costos. Solo para uso interno de la empresa, NO se muestra en PDFs enviados al cliente.", example = "Se aplica mayor costo debido a...", maxLength = 2000)
    private String mayoresCostosExplicacion;

    // ========== CONFIGURACIÓN DE DESCUENTOS (Modelo Relacional) ==========
    /**
     * Los descuentos se aplican DESPUÉS de honorarios y mayores costos.
     * Siguiendo el mismo patrón relacional que honorarios y mayores_costos.
     */
    
    @Schema(description = "Explicación/justificación de por qué se aplican descuentos. Visible para el cliente en el PDF.", example = "Descuento por cliente frecuente", maxLength = 2000)
    private String descuentosExplicacion;
    
    // Descuentos sobre JORNALES
    @Schema(description = "Si el descuento sobre jornales está activo", example = "true")
    private Boolean descuentosJornalesActivo;
    
    @Schema(description = "Tipo de descuento para jornales: porcentaje o fijo", example = "porcentaje")
    private String descuentosJornalesTipo;
    
    @Schema(description = "Valor del descuento para jornales", example = "10.0")
    private Double descuentosJornalesValor;
    
    // Descuentos sobre MATERIALES
    @Schema(description = "Si el descuento sobre materiales está activo", example = "false")
    private Boolean descuentosMaterialesActivo;
    
    @Schema(description = "Tipo de descuento para materiales: porcentaje o fijo", example = "fijo")
    private String descuentosMaterialesTipo;
    
    @Schema(description = "Valor del descuento para materiales", example = "5000.0")
    private Double descuentosMaterialesValor;
    
    // Descuentos sobre HONORARIOS
    @Schema(description = "Si el descuento sobre honorarios está activo", example = "true")
    private Boolean descuentosHonorariosActivo;
    
    @Schema(description = "Tipo de descuento para honorarios: porcentaje o fijo", example = "porcentaje")
    private String descuentosHonorariosTipo;
    
    @Schema(description = "Valor del descuento para honorarios", example = "5.0")
    private Double descuentosHonorariosValor;
    
    // Descuentos sobre MAYORES COSTOS
    @Schema(description = "Si el descuento sobre mayores costos está activo", example = "false")
    private Boolean descuentosMayoresCostosActivo;
    
    @Schema(description = "Tipo de descuento para mayores costos: porcentaje o fijo", example = "porcentaje")
    private String descuentosMayoresCostosTipo;
    
    @Schema(description = "Valor del descuento para mayores costos", example = "0.0")
    private Double descuentosMayoresCostosValor;
    
    // ========== SUB-TIPOS DE DESCUENTOS SOBRE HONORARIOS ==========
    /**
     * Descuentos granulares sobre cada categoría de honorarios.
     * El frontend envía estos campos para aplicar descuentos específicos
     * sobre los honorarios de cada rubro.
     */
    
    // Descuentos sobre Honorarios de JORNALES
    @Schema(description = "Si el descuento sobre honorarios de jornales está activo", example = "true")
    private Boolean descuentosHonorariosJornalesActivo;
    
    @Schema(description = "Tipo de descuento para honorarios de jornales: porcentaje o fijo", example = "porcentaje")
    private String descuentosHonorariosJornalesTipo;
    
    @Schema(description = "Valor del descuento para honorarios de jornales", example = "0.0")
    private java.math.BigDecimal descuentosHonorariosJornalesValor;
    
    // Descuentos sobre Honorarios de PROFESIONALES
    @Schema(description = "Si el descuento sobre honorarios de profesionales está activo", example = "true")
    private Boolean descuentosHonorariosProfesionalesActivo;
    
    @Schema(description = "Tipo de descuento para honorarios de profesionales: porcentaje o fijo", example = "porcentaje")
    private String descuentosHonorariosProfesionalesTipo;
    
    @Schema(description = "Valor del descuento para honorarios de profesionales", example = "0.0")
    private java.math.BigDecimal descuentosHonorariosProfesionalesValor;
    
    // Descuentos sobre Honorarios de MATERIALES
    @Schema(description = "Si el descuento sobre honorarios de materiales está activo", example = "true")
    private Boolean descuentosHonorariosMaterialesActivo;
    
    @Schema(description = "Tipo de descuento para honorarios de materiales: porcentaje o fijo", example = "porcentaje")
    private String descuentosHonorariosMaterialesTipo;
    
    @Schema(description = "Valor del descuento para honorarios de materiales", example = "0.0")
    private java.math.BigDecimal descuentosHonorariosMaterialesValor;
    
    // Descuentos sobre Honorarios de OTROS COSTOS
    @Schema(description = "Si el descuento sobre honorarios de otros costos está activo", example = "true")
    private Boolean descuentosHonorariosOtrosActivo;
    
    @Schema(description = "Tipo de descuento para honorarios de otros costos: porcentaje o fijo", example = "porcentaje")
    private String descuentosHonorariosOtrosTipo;
    
    @Schema(description = "Valor del descuento para honorarios de otros costos", example = "0.0")
    private java.math.BigDecimal descuentosHonorariosOtrosValor;
    
    // Descuentos sobre Honorarios de GASTOS GENERALES
    @Schema(description = "Si el descuento sobre honorarios de gastos generales está activo", example = "true")
    private Boolean descuentosHonorariosGastosGeneralesActivo;
    
    @Schema(description = "Tipo de descuento para honorarios de gastos generales: porcentaje o fijo", example = "porcentaje")
    private String descuentosHonorariosGastosGeneralesTipo;
    
    @Schema(description = "Valor del descuento para honorarios de gastos generales", example = "0.0")
    private java.math.BigDecimal descuentosHonorariosGastosGeneralesValor;
    
    // Descuentos sobre Honorarios de CONFIGURACIÓN DE PRESUPUESTO
    @Schema(description = "Si el descuento sobre honorarios de configuración está activo", example = "true")
    private Boolean descuentosHonorariosConfiguracionActivo;
    
    @Schema(description = "Tipo de descuento para honorarios de configuración: porcentaje o fijo", example = "porcentaje")
    private String descuentosHonorariosConfiguracionTipo;
    
    @Schema(description = "Valor del descuento para honorarios de configuración", example = "0.0")
    private java.math.BigDecimal descuentosHonorariosConfiguracionValor;

    // ========== COSTOS INICIALES POR M² ==========
    @Schema(description = "Cálculo inicial de costos basado en metros cuadrados (opcional)")
    private com.rodrigo.construccion.dto.PresupuestoCostoInicialDTO costosIniciales;

    // ========== TOTALES ESPECÍFICOS DEL FRONTEND ==========
        @Schema(description = "Total base del presupuesto sin honorarios", example = "9500000")
    private java.math.BigDecimal totalPresupuesto;
    
        @Schema(description = "Total de honorarios calculados", example = "11650000")
    private java.math.BigDecimal totalHonorarios;
    
        @Schema(description = "TOTAL FINAL: presupuesto base + honorarios", example = "21150000")
        private java.math.BigDecimal totalPresupuestoConHonorarios;

    @Schema(description = "Total del presupuesto después de aplicar descuentos", example = "19150000")
    private java.math.BigDecimal totalConDescuentos;

    // ========== ITEMS DE CALCULADORA ==========
    @Schema(description = "Items ingresados mediante la calculadora rápida de presupuestos. Soporta modo automático (jornales + materiales) y modo manual (total directo).")
    private List<ItemCalculadoraPresupuestoDTO> itemsCalculadora;

    // ========== JORNALES ==========
    @Schema(description = "Jornales del presupuesto. Lista con estructura de rol, cantidad, valor unitario y subtotal")
    private List<JornalDTO> jornales;

    // ========== CONTROL DE VERSIONADO ==========
    @JsonProperty("_shouldCreateNewVersion")
    @Schema(description = "Flag enviado desde el frontend que indica si se debe crear una nueva versión del presupuesto (true) o actualizar la versión existente (false). Si es null, se crea nueva versión por defecto.", example = "true")
    private Boolean shouldCreateNewVersion;

}
