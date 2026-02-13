package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rodrigo.construccion.enums.EstadoObra;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/* DTO para respuesta de obras */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información completa de una obra")
public class ObraResponseDTO {

    @Schema(description = "ID único de la obra", example = "1")
    private Long id;

    @Schema(description = "Indica si la obra es un trabajo extra", example = "false")
    @JsonProperty("esTrabajoExtra") // Alias para el frontend
    private Boolean esObraTrabajoExtra;

    @Schema(description = "ID de la obra principal/padre (si es trabajo extra)", example = "100")
    @JsonProperty("obraPadreId") // Alias para el frontend, valor real: obraOrigenId
    private Long obraOrigenId;

    @Schema(description = "Nombre de la obra", example = "Casa Familiar García")
    private String nombre;

    // DIRECCIÓN EN 6 CAMPOS SEPARADOS (igual que PresupuestoNoCliente)
    @Schema(description = "Barrio de la obra", example = "Palermo")
    private String direccionObraBarrio;

    @Schema(description = "Calle de la obra", example = "Av. Libertador")
    private String direccionObraCalle;

    @Schema(description = "Número/Altura de la obra", example = "1234")
    private String direccionObraAltura;

    @Schema(description = "Torre o edificio", example = "Torre A")
    private String direccionObraTorre;

    @Schema(description = "Piso (opcional)", example = "4")
    private String direccionObraPiso;

    @Schema(description = "Departamento (opcional)", example = "A")
    private String direccionObraDepartamento;

    @Schema(description = "Estado actual de la obra", example = "EN_PLANIFICACION")
    private EstadoObra estado;

    @Schema(description = "Fecha de inicio planificada", example = "2024-01-15")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;

    @Schema(description = "Fecha de finalización estimada", example = "2024-06-30")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;

    @Schema(description = "Presupuesto estimado inicial", example = "150000.00")
    private BigDecimal presupuestoEstimado;

    @Schema(description = "ID del presupuesto sin cliente vinculado", example = "123")
    private Long presupuestoNoClienteId;

    @Schema(description = "ID de la empresa propietaria", example = "3")
    private Long empresaId;

    @Schema(description = "Fecha de creación del registro", example = "2024-01-01T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @Schema(description = "ID del cliente propietario", example = "1")
    private Long idCliente;

    @Schema(description = "Información del cliente")
    private ClienteResponseDTO cliente;

    @Schema(description = "Presupuesto no cliente que originó la obra (si aplica)")
    private PresupuestoNoClienteSimpleDTO presupuestoNoCliente;

    @Schema(description = "Presupuestos asociados a la obra")
    private List<PresupuestoResponseDTO> presupuestos;

    // Datos del solicitante (sincronizados desde presupuesto)
    @Schema(description = "Nombre del solicitante/cliente", example = "Juan García")
    private String nombreSolicitante;

    @Schema(description = "Teléfono de contacto", example = "+54 11 1234-5678")
    private String telefono;

    @Schema(description = "Email de contacto", example = "juan.garcia@email.com")
    private String mail;

    @Schema(description = "Dirección particular del cliente", example = "Av. Corrientes 1234, CABA")
    private String direccionParticular;

    @Schema(description = "Descripción detallada de la obra", example = "Refacción integral de vivienda")
    private String descripcion;

    @Schema(description = "Observaciones adicionales", example = "Cliente prefiere materiales premium")
    private String observaciones;

    /**
     * Helper para obtener la dirección completa formateada
     * @return Dirección formateada como "Barrio - Calle 1234 Torre A Piso 4 Depto A"
     */
    public String getDireccionCompleta() {
        StringBuilder direccion = new StringBuilder();
        if (direccionObraBarrio != null && !direccionObraBarrio.trim().isEmpty()) {
            direccion.append(direccionObraBarrio).append(" - ");
        }
        direccion.append(direccionObraCalle != null ? direccionObraCalle : "")
                 .append(" ")
                 .append(direccionObraAltura != null ? direccionObraAltura.toString() : "0");
        if (direccionObraTorre != null && !direccionObraTorre.trim().isEmpty()) {
            direccion.append(" ").append(direccionObraTorre);
        }
        if (direccionObraPiso != null && !direccionObraPiso.trim().isEmpty()) {
            direccion.append(" Piso ").append(direccionObraPiso);
        }
        if (direccionObraDepartamento != null && !direccionObraDepartamento.trim().isEmpty()) {
            direccion.append(" Depto ").append(direccionObraDepartamento);
        }
        return direccion.toString().trim();
    }

}