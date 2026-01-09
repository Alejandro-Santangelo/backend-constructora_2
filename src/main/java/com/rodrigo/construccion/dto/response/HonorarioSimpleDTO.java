package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "DTO con los detalles resumidos de un único honorario, para ser usado en listas.")
public class HonorarioSimpleDTO {

    @Schema(description = "ID único del honorario", example = "205")
    private Long idHonorario;

    @Schema(description = "Fecha del pago del honorario", example = "2024-10-25")
    private LocalDate fecha;

    @Schema(description = "Monto del honorario", example = "50000.00")
    private BigDecimal monto;

    @Schema(description = "Nombre de la obra donde se generó el honorario", example = "Edificio Central")
    private String nombreObra;

    @Schema(description = "Observaciones sobre el pago", example = "Adelanto por supervisión de obra.")
    private String observaciones;
}