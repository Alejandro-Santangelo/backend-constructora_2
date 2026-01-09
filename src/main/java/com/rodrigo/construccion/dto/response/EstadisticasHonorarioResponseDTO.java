package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "DTO con estadísticas clave sobre los honorarios de una empresa.")
public class EstadisticasHonorarioResponseDTO {

    @Schema(description = "Cantidad total de honorarios registrados para la empresa.", example = "150")
    private long totalHonorarios;

    @Schema(description = "Cantidad de honorarios registrados en el mes actual.", example = "12")
    private long honorariosMesActual;

    @Schema(description = "Suma total de los montos de honorarios del mes actual.", example = "85000.75")
    private BigDecimal montoTotalMesActual;

    @Schema(description = "Fecha en que se realizó la consulta de estadísticas.", example = "2024-10-26")
    private LocalDate fechaConsulta;
}