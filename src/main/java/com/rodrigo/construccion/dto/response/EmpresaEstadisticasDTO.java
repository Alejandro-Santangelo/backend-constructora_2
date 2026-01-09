package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO con las estadísticas agregadas de una empresa.")
public class EmpresaEstadisticasDTO {

    @Schema(description = "ID de la empresa", example = "1")
    private Long empresaId;
    @Schema(description = "Nombre de la empresa", example = "Constructora XYZ")
    private String nombre;
    @Schema(description = "Número total de clientes asociados", example = "15")
    private Long totalClientes;
    @Schema(description = "Número total de obras asociadas", example = "25")
    private Long totalObras;
    @Schema(description = "Número total de usuarios asociados", example = "10")
    private Long totalUsuarios;
}