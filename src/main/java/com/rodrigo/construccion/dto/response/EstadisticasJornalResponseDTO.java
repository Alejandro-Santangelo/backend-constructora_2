package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO que contiene estadísticas consolidadas sobre los jornales de una empresa.")
public class EstadisticasJornalResponseDTO {

    @Schema(description = "Cantidad total de jornales registrados para la empresa.", example = "150")
    private long totalJornales;

    @Schema(description = "Cantidad de jornales registrados en el mes actual.", example = "25")
    private long jornalesMesActual;

    @Schema(description = "Monto total facturado en el mes actual (horas * valor hora).", example = "550000.00")
    private BigDecimal montoTotalMesActual;

    @Schema(description = "Total de horas trabajadas en el mes actual.", example = "200.00")
    private BigDecimal horasTotalMesActual;

    @Schema(description = "Fecha en la que se generaron las estadísticas.", example = "2024-10-22")
    private LocalDate fechaConsulta;
}