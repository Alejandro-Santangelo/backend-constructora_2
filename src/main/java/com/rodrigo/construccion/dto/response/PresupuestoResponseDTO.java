package com.rodrigo.construccion.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO básico para respuesta de presupuestos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información básica de un presupuesto")
public class PresupuestoResponseDTO {

    @Schema(description = "ID único del presupuesto", example = "1")
    private Long id;

    @Schema(description = "Descripción del presupuesto", example = "Presupuesto inicial")
    private String descripcion;

    @Schema(description = "Monto total del presupuesto", example = "150000.00")
    private double montoTotal;

    @Schema(description = "Estado del presupuesto", example = "Aprobado")
    private String estado;


    @Schema(description = "Fecha de aprobación del presupuesto")
    private LocalDateTime fechaAprobacion;

    @Schema(description = "Fecha de aprobación del presupuesto en formato texto. Muestra '00/00/0000' si no está aprobado, o la fecha en formato 'dd/MM/yyyy' si está aprobado")
    private String fechaAprobacionTexto;

    @Schema(description = "Fecha de emisión del presupuesto")
    private LocalDate fechaEmision;

    @Schema(description = "Fecha de validez del presupuesto (hasta)")
    private String fechaValidezTexto;

    @Schema(description = "Valor fijo de honorario dirección de obra")
    private double honorarioDireccionValorFijo;

    @Schema(description = "Porcentaje de honorario dirección de obra")
    private double honorarioDireccionPorcentaje;

    @Schema(description = "Importe calculado de honorario dirección de obra")
    private double honorarioDireccionImporte;

    @Schema(description = "Total de honorarios de profesionales")
    private double totalHonorariosProfesionales;

    @Schema(description = "Nombre de la obra asociada al presupuesto")
    private String nombreObra;


    @Schema(description = "Total de materiales del presupuesto")
    private double totalMateriales;

    @Schema(description = "Total de honorarios de dirección de obra")
    private double totalHonorariosDireccionObra;

    @Schema(description = "ID de la empresa")
    private Long idEmpresa;

    @Schema(description = "ID de la obra")
    private Long idObra;

    @Schema(description = "Número de versión del presupuesto")
    private Integer version;

    // NUEVO: profesionales asociados al presupuesto (estimados del presupuesto original)
    private List<PresupuestoProfesionalResponseDTO> profesionales;

    // NUEVO: profesionales específicos asignados a la obra (valores reales)
    @Schema(description = "Lista de profesionales específicos asignados a la obra con sus datos reales")
    private List<AsignacionProfesionalResponse> profesionalesAsignados;

    // COSTOS INICIALES POR M²
    @Schema(description = "Cálculo inicial de costos basado en metros cuadrados (opcional)")
    private com.rodrigo.construccion.dto.PresupuestoCostoInicialDTO costosIniciales;

}
