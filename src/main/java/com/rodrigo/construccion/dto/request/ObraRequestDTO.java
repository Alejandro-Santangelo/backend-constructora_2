package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/* DTO para crear/actualizar obras */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear una nueva obra")
public class ObraRequestDTO {

    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Schema(description = "Nombre de la obra (opcional - se genera automáticamente si está vacío)", example = "Casa Familiar García")
    private String nombre;

    // DIRECCIÓN EN 6 CAMPOS SEPARADOS (igual que PresupuestoNoCliente)
    @Schema(description = "Barrio de la obra (opcional)", example = "Palermo")
    private String direccionObraBarrio;

    @NotBlank(message = "La calle es obligatoria")
    @Schema(description = "Calle de la obra", example = "Av. Libertador", requiredMode = Schema.RequiredMode.REQUIRED)
    private String direccionObraCalle;

    @NotNull(message = "La altura es obligatoria")
    @Schema(description = "Número/Altura de la obra", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String direccionObraAltura;

    @Schema(description = "Torre o edificio (opcional)", example = "Torre A")
    private String direccionObraTorre;

    @Schema(description = "Piso (opcional)", example = "4")
    private String direccionObraPiso;

    @Schema(description = "Departamento (opcional)", example = "A")
    private String direccionObraDepartamento;

    @Schema(description = "Estado actual de la obra", example = "En planificación", allowableValues = {
            "En planificación", "En revisión", "En obra", "Suspendida", "Finalizada", "Cancelada"
    })
    private String estado;

    @Schema(description = "Fecha de inicio planificada", example = "2024-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de finalización estimada", example = "2024-06-30")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    @PositiveOrZero(message = "El presupuesto estimado debe ser mayor o igual a cero")
    @Schema(description = "Presupuesto estimado inicial", example = "150000.00")
    private BigDecimal presupuestoEstimado;

    @Schema(description = "Desglose jornales del presupuesto", example = "1000000.00")
    private BigDecimal presupuestoJornales;

    @Schema(description = "Desglose materiales del presupuesto", example = "800000.00")
    private BigDecimal presupuestoMateriales;

    @Schema(description = "Desglose gastos generales del presupuesto (nueva categoría)", example = "150000.00")
    private BigDecimal importeGastosGeneralesObra;

    @Schema(description = "Desglose honorarios del presupuesto (monto o porcentaje)", example = "15")
    private BigDecimal presupuestoHonorarios;

    @Schema(description = "Tipo de honorarios: fijo o porcentaje", example = "porcentaje")
    private String tipoHonorarioPresupuesto;

    @Schema(description = "Desglose mayores costos del presupuesto (monto o porcentaje)", example = "100000.00")
    private BigDecimal presupuestoMayoresCostos;

    @Schema(description = "Tipo de mayores costos: fijo o porcentaje", example = "fijo")
    private String tipoMayoresCostosPresupuesto;

    // ========== HONORARIOS INDIVIDUALES POR CATEGORÍA (SISTEMA NUEVO) ==========
    @Schema(description = "Honorario para jornales - puede ser fijo ($) o porcentual (%)", example = "50000.00")
    private BigDecimal honorarioJornalesObra;
    @Schema(description = "Tipo de honorario jornales: fijo o porcentaje", example = "fijo")
    private String tipoHonorarioJornalesObra;
    
    @Schema(description = "Honorario para materiales - puede ser fijo ($) o porcentual (%)", example = "10")
    private BigDecimal honorarioMaterialesObra;
    @Schema(description = "Tipo de honorario materiales: fijo o porcentaje", example = "porcentaje")
    private String tipoHonorarioMaterialesObra;
    
    @Schema(description = "Honorario para gastos generales - puede ser fijo ($) o porcentual (%)", example = "25000.00")
    private BigDecimal honorarioGastosGeneralesObra;
    @Schema(description = "Tipo de honorario gastos generales: fijo o porcentaje", example = "fijo")
    private String tipoHonorarioGastosGeneralesObra;
    
    @Schema(description = "Honorario para mayores costos - puede ser fijo ($) o porcentual (%)", example = "15")
    private BigDecimal honorarioMayoresCostosObra;
    @Schema(description = "Tipo de honorario mayores costos: fijo o porcentaje", example = "porcentaje")
    private String tipoHonorarioMayoresCostosObra;

    // ========== DESCUENTOS SOBRE IMPORTES BASE POR CATEGORÍA ==========
    @Schema(description = "Descuento sobre importe base de jornales - puede ser fijo ($) o porcentual (%)", example = "5")
    private BigDecimal descuentoJornalesObra;
    @Schema(description = "Tipo de descuento jornales: fijo o porcentaje", example = "porcentaje")
    private String tipoDescuentoJornalesObra;
    
    @Schema(description = "Descuento sobre importe base de materiales - puede ser fijo ($) o porcentual (%)", example = "10000.00")
    private BigDecimal descuentoMaterialesObra;
    @Schema(description = "Tipo de descuento materiales: fijo o porcentaje", example = "fijo")
    private String tipoDescuentoMaterialesObra;
    
    @Schema(description = "Descuento sobre importe base de gastos generales - puede ser fijo ($) o porcentual (%)", example = "3")
    private BigDecimal descuentoGastosGeneralesObra;
    @Schema(description = "Tipo de descuento gastos generales: fijo o porcentaje", example = "porcentaje")
    private String tipoDescuentoGastosGeneralesObra;
    
    @Schema(description = "Descuento sobre importe base de mayores costos - puede ser fijo ($) o porcentual (%)", example = "5000.00")
    private BigDecimal descuentoMayoresCostosObra;
    @Schema(description = "Tipo de descuento mayores costos: fijo o porcentaje", example = "fijo")
    private String tipoDescuentoMayoresCostosObra;

    // ========== DESCUENTOS SOBRE HONORARIOS POR CATEGORÍA (NUEVOS) ==========
    @Schema(description = "Descuento sobre honorario de jornales - puede ser fijo ($) o porcentual (%)", example = "2000.00")
    private BigDecimal descuentoHonorarioJornalesObra;
    @Schema(description = "Tipo de descuento honorario jornales: fijo o porcentaje", example = "fijo")
    private String tipoDescuentoHonorarioJornalesObra;
    
    @Schema(description = "Descuento sobre honorario de materiales - puede ser fijo ($) o porcentual (%)", example = "5")
    private BigDecimal descuentoHonorarioMaterialesObra;
    @Schema(description = "Tipo de descuento honorario materiales: fijo o porcentaje", example = "porcentaje")
    private String tipoDescuentoHonorarioMaterialesObra;
    
    @Schema(description = "Descuento sobre honorario de gastos generales - puede ser fijo ($) o porcentual (%)", example = "1000.00")
    private BigDecimal descuentoHonorarioGastosGeneralesObra;
    @Schema(description = "Tipo de descuento honorario gastos generales: fijo o porcentaje", example = "fijo")
    private String tipoDescuentoHonorarioGastosGeneralesObra;
    
    @Schema(description = "Descuento sobre honorario de mayores costos - puede ser fijo ($) o porcentual (%)", example = "10")
    private BigDecimal descuentoHonorarioMayoresCostosObra;
    @Schema(description = "Tipo de descuento honorario mayores costos: fijo o porcentaje", example = "porcentaje")
    private String tipoDescuentoHonorarioMayoresCostosObra;

    @Schema(description = "Descripción detallada de la obra", example = "Refacción integral de vivienda")
    private String descripcion;

    @Schema(description = "Observaciones adicionales", example = "Cliente prefiere materiales premium")
    private String observaciones;

    @Schema(description = "ID del cliente propietario de la obra (opcional si se crea uno nuevo)", example = "1")
    private Long idCliente;

    // Datos del solicitante (sincronizados desde presupuesto)
    @Schema(description = "Nombre del solicitante/cliente", example = "Juan García")
    private String nombreSolicitante;

    @Schema(description = "Teléfono de contacto", example = "+54 11 1234-5678")
    private String telefono;

    @Schema(description = "Email de contacto", example = "juan.garcia@email.com")
    private String mail;

    @Schema(description = "Dirección particular del cliente", example = "Av. Corrientes 1234, CABA")
    private String direccionParticular;

    @Schema(description = "ID de la empresa", example = "1")
    private Long empresaId;

    @Schema(description = "Profesionales asignados desde el formulario")
    private java.util.List<ProfesionalFormDTO> profesionalesAsignadosForm;
}