package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear múltiples asignaciones desde una obra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de múltiples profesionales para una obra")
public class SolicitudProfesionalesRequest {

        @Schema(description = "ID de la obra que solicita profesionales (opcional si se especifica en URL)", example = "1")
        @NotBlank(message = "El ID de la obra es obligatorio")
        public Long obraId;

        @Schema(description = "ID de la empresa (opcional si se especifica en URL)", example = "1")
        public Long empresaId;

        @Schema(description = "Fecha de inicio de las asignaciones (opcional, por defecto hoy)", example = "2025-10-04")
        public LocalDate fechaDesde;

        @Schema(description = "Fecha estimada de fin (opcional)", example = "2025-12-31")
        public LocalDate fechaHasta;

        @Schema(description = "Observaciones generales de la solicitud (opcional)", example = "Urgente para obra en construcción")
        public String observaciones;

        @Schema(description = "Lista de tipos de profesionales solicitados con cantidades (opcional, puede estar vacía)")
        public List<SolicitudTipoProfesionalRequest> tiposProfesionales;
}