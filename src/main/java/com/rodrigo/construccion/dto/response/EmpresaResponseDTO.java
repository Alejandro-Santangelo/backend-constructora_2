package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para las respuestas de consultas de empresas
 * Contiene solo los campos necesarios para el sistema multi-tenant
 */
@Data
@Schema(description = "Respuesta con datos básicos de una empresa")
public class EmpresaResponseDTO {

    @Schema(description = "ID único de la empresa", example = "1")
    private Long id;

    @Schema(description = "Nombre de la empresa", example = "Constructora ABC S.A.")
    private String nombreEmpresa;

    @Schema(description = "Razón social de la empresa", example = "Constructora ABC Sociedad Anónima")
    private String razonSocial;

    @Schema(description = "CUIT de la empresa", example = "20-12345678-9")
    private String cuit;

    @Schema(description = "Estado activo de la empresa", example = "true")
    private Boolean activa;

    @Schema(description = "Fecha de creación de la empresa", example = "2025-10-03T09:30:00")
    private LocalDateTime fechaCreacion;
}