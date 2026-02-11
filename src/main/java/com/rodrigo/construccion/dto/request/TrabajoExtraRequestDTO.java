package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO Request para crear o actualizar un trabajo extra por día
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request para crear o actualizar un trabajo extra por día")
public class TrabajoExtraRequestDTO {

    @NotNull(message = "El ID de la obra es obligatorio")
    @Schema(description = "ID de la obra padre", example = "70", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long obraId;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre descriptivo del trabajo extra", example = "Trabajo Extra - Instalación adicional", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Schema(description = "Observaciones del trabajo extra", example = "Trabajo urgente solicitado por el cliente")
    private String observaciones;

    // Datos de contacto del cliente
    @Schema(description = "Nombre de la empresa del cliente")
    private String nombreEmpresa;

    @Schema(description = "Nombre del solicitante del trabajo")
    private String nombreSolicitante;

    @Schema(description = "Teléfono de contacto")
    private String telefono;

    @Schema(description = "Email de contacto")
    private String mail;

    @Schema(description = "Dirección particular del cliente")
    private String direccionParticular;

    // Dirección de la obra (desglosada)
    @Schema(description = "Calle de la obra")
    private String direccionObraCalle;

    @Schema(description = "Altura/número de la obra")
    private String direccionObraAltura;

    @Schema(description = "Barrio de la obra")
    private String direccionObraBarrio;

    @Schema(description = "Torre de la obra")
    private String direccionObraTorre;

    @Schema(description = "Piso de la obra")
    private String direccionObraPiso;

    @Schema(description = "Departamento de la obra")
    private String direccionObraDepartamento;

    @Schema(description = "Localidad de la obra")
    private String direccionObraLocalidad;

    @Schema(description = "Provincia de la obra")
    private String direccionObraProvincia;

    @Schema(description = "Código postal de la obra")
    private String direccionObraCodigoPostal;

    // Datos del presupuesto
    @Schema(description = "Nombre descriptivo de la obra")
    private String nombreObra;

    @Schema(description = "Descripción detallada del trabajo")
    private String descripcion;

    @Schema(description = "Fecha probable de inicio", example = "2026-01-15")
    private LocalDate fechaProbableInicio;

    @Schema(description = "Fecha de vencimiento del presupuesto", example = "2026-02-15")
    private LocalDate vencimiento;

    @Schema(description = "Fecha de creación del presupuesto", example = "2026-01-08")
    private LocalDate fechaCreacion;

    @Schema(description = "Fecha de emisión del presupuesto", example = "2026-01-08")
    private LocalDate fechaEmision;

    @Schema(description = "Tiempo estimado de terminación en días", example = "30")
    private Integer tiempoEstimadoTerminacion;

    @Schema(description = "Indica si se calculan días hábiles automáticamente", example = "false")
    private Boolean calculoAutomaticoDiasHabiles;

    // Control de versiones y estado
    @Schema(description = "Versión del presupuesto", example = "1")
    private Integer version;

    @Schema(description = "Número único del presupuesto", example = "TE-2026-001")
    private String numeroPresupuesto;

    @Schema(description = "Estado del presupuesto: ENVIADO, APROBADO, RECHAZADO, EN_REVISION", example = "ENVIADO")
    private String estado;

    // Totales del presupuesto
    @Schema(description = "Total del presupuesto base", example = "500000.00")
    private BigDecimal totalPresupuesto;

    @Schema(description = "Total de honorarios", example = "100000.00")
    private BigDecimal totalHonorarios;

    @Schema(description = "Total de mayores costos", example = "50000.00")
    private BigDecimal totalMayoresCostos;

    @Schema(description = "Total final del presupuesto", example = "650000.00")
    private BigDecimal totalFinal;

    @Schema(description = "Monto total (alias para compatibilidad)", example = "650000.00")
    private BigDecimal montoTotal;

    // Marcador de trabajo extra
    @Schema(description = "Indica si es un trabajo extra", example = "true")
    private Boolean esTrabajoExtra;

    @Schema(description = "Lista de fechas (días) en que se realiza el trabajo", example = "[\"2025-12-03\", \"2025-12-04\"]")
    private List<LocalDate> dias;

    @Valid
    @Schema(description = "Lista de profesionales asignados al trabajo extra")
    private List<TrabajoExtraProfesionalDTO> profesionales;

    @Valid
    @Schema(description = "Lista de tareas del trabajo extra")
    private List<TrabajoExtraTareaDTO> tareas;
    // ============================================================================
    // NUEVO: Sistema de rubros e items (como PresupuestoNoCliente)
    // ============================================================================

    @Valid
    @Schema(description = "Lista de rubros/items del trabajo extra con desglose completo")
    private List<TrabajoExtraItemCalculadoraDTO> itemsCalculadora;

    @Valid
    @Schema(description = "Configuración de honorarios")
    private HonorariosConfigDTO honorarios;

    @Valid
    @Schema(description = "Configuración de mayores costos")
    private MayoresCostosConfigDTO mayoresCostos;

    @Schema(description = "Honorario de dirección - porcentaje")
    private BigDecimal honorarioDireccionPorcentaje;

    @Schema(description = "Honorario de dirección - importe fijo")
    private BigDecimal honorarioDireccionImporte;

    @Schema(description = "Honorario de dirección - valor fijo")
    private BigDecimal honorarioDireccionValorFijo;

    // ============================================================================
    // CAMPOS PLANOS DE HONORARIOS (para compatibilidad con frontend)
    // ============================================================================
    
    @Schema(description = "Aplicar honorarios a todos los rubros")
    private Boolean honorariosAplicarATodos;
    
    @Schema(description = "Valor general de honorarios")
    private BigDecimal honorariosValorGeneral;
    
    @Schema(description = "Tipo general de honorarios: PORCENTAJE o FIJO")
    private String honorariosTipoGeneral;
    
    @Schema(description = "Honorarios jornales activo")
    private Boolean honorariosJornalesActivo;
    
    @Schema(description = "Honorarios jornales tipo")
    private String honorariosJornalesTipo;
    
    @Schema(description = "Honorarios jornales valor")
    private BigDecimal honorariosJornalesValor;
    
    @Schema(description = "Honorarios materiales activo")
    private Boolean honorariosMaterialesActivo;
    
    @Schema(description = "Honorarios materiales tipo")
    private String honorariosMaterialesTipo;
    
    @Schema(description = "Honorarios materiales valor")
    private BigDecimal honorariosMaterialesValor;
    
    @Schema(description = "Honorarios profesionales activo")
    private Boolean honorariosProfesionalesActivo;
    
    @Schema(description = "Honorarios profesionales tipo")
    private String honorariosProfesionalesTipo;
    
    @Schema(description = "Honorarios profesionales valor")
    private BigDecimal honorariosProfesionalesValor;
    
    @Schema(description = "Honorarios otros costos activo")
    private Boolean honorariosOtrosCostosActivo;
    
    @Schema(description = "Honorarios otros costos tipo")
    private String honorariosOtrosCostosTipo;
    
    @Schema(description = "Honorarios otros costos valor")
    private BigDecimal honorariosOtrosCostosValor;
    
    @Schema(description = "Honorarios configuración presupuesto activo")
    private Boolean honorariosConfiguracionPresupuestoActivo;
    
    @Schema(description = "Honorarios configuración presupuesto tipo")
    private String honorariosConfiguracionPresupuestoTipo;
    
    @Schema(description = "Honorarios configuración presupuesto valor")
    private BigDecimal honorariosConfiguracionPresupuestoValor;

    // ============================================================================
    // CAMPOS PLANOS DE MAYORES COSTOS (para compatibilidad con frontend)
    // ============================================================================
    
    @Schema(description = "Aplicar valor general de mayores costos")
    private Boolean mayoresCostosAplicarValorGeneral;
    
    @Schema(description = "Valor general de mayores costos")
    private BigDecimal mayoresCostosValorGeneral;
    
    @Schema(description = "Tipo general de mayores costos")
    private String mayoresCostosTipoGeneral;
    
    @Schema(description = "Mayores costos jornales activo")
    private Boolean mayoresCostosJornalesActivo;
    
    @Schema(description = "Mayores costos jornales tipo")
    private String mayoresCostosJornalesTipo;
    
    @Schema(description = "Mayores costos jornales valor")
    private BigDecimal mayoresCostosJornalesValor;
    
    @Schema(description = "Mayores costos materiales activo")
    private Boolean mayoresCostosMaterialesActivo;
    
    @Schema(description = "Mayores costos materiales tipo")
    private String mayoresCostosMaterialesTipo;
    
    @Schema(description = "Mayores costos materiales valor")
    private BigDecimal mayoresCostosMaterialesValor;
    
    @Schema(description = "Mayores costos profesionales activo")
    private Boolean mayoresCostosProfesionalesActivo;
    
    @Schema(description = "Mayores costos profesionales tipo")
    private String mayoresCostosProfesionalesTipo;
    
    @Schema(description = "Mayores costos profesionales valor")
    private BigDecimal mayoresCostosProfesionalesValor;
    
    @Schema(description = "Mayores costos otros costos activo")
    private Boolean mayoresCostosOtrosCostosActivo;
    
    @Schema(description = "Mayores costos otros costos tipo")
    private String mayoresCostosOtrosCostosTipo;
    
    @Schema(description = "Mayores costos otros costos valor")
    private BigDecimal mayoresCostosOtrosCostosValor;
    
    @Schema(description = "Mayores costos honorarios activo")
    private Boolean mayoresCostosHonorariosActivo;
    
    @Schema(description = "Mayores costos honorarios tipo")
    private String mayoresCostosHonorariosTipo;
    
    @Schema(description = "Mayores costos honorarios valor")
    private BigDecimal mayoresCostosHonorariosValor;
    
    @Schema(description = "Mayores costos configuración presupuesto activo")
    private Boolean mayoresCostosConfiguracionPresupuestoActivo;
    
    @Schema(description = "Mayores costos configuración presupuesto tipo")
    private String mayoresCostosConfiguracionPresupuestoTipo;
    
    @Schema(description = "Mayores costos configuración presupuesto valor")
    private BigDecimal mayoresCostosConfiguracionPresupuestoValor;
    
    @Schema(description = "Mayores costos general importado")
    private Boolean mayoresCostosGeneralImportado;
    
    @Schema(description = "Mayores costos rubro importado")
    private String mayoresCostosRubroImportado;
    
    @Schema(description = "Mayores costos nombre rubro importado")
    private String mayoresCostosNombreRubroImportado;
    
    @Schema(description = "Mayores costos explicación")
    private String mayoresCostosExplicacion;
}
