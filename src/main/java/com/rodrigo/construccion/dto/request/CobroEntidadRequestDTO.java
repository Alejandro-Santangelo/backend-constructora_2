package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request para registrar un cobro contra una entidad financiera.
 */
@Data
public class CobroEntidadRequestDTO {

    @NotNull(message = "entidadFinancieraId es obligatorio")
    private Long entidadFinancieraId;

    @NotNull(message = "empresaId es obligatorio")
    private Long empresaId;

    @NotNull(message = "monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "fechaCobro es obligatoria")
    private LocalDate fechaCobro;

    private String metodoPago;
    private String referencia;
    private String notas;
    private String creadoPor;
}
