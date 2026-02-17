package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representación de un cobro registrado contra una entidad financiera.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CobroEntidadResponseDTO {

    private Long id;
    private Long entidadFinancieraId;
    private Long empresaId;
    private BigDecimal monto;
    private LocalDate fechaCobro;
    private String metodoPago;
    private String referencia;
    private String notas;
    private String creadoPor;
    private LocalDateTime fechaCreacion;
}
