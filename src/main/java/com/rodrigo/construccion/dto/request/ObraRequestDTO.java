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