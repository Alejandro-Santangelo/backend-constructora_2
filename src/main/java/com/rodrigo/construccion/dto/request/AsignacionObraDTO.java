package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para representar una asignación de cobro empresa a una obra o trabajo adicional.
 * Se debe proveer exactamente uno de: obraId o trabajoAdicionalId.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionObraDTO {

    // Obligatorio si se asigna a una obra
    private Long obraId;

    // Obligatorio si se asigna a un trabajo adicional
    private Long trabajoAdicionalId;

    @NotNull(message = "El monto asignado es obligatorio")
    @Positive(message = "El monto asignado debe ser mayor a cero")
    private BigDecimal montoAsignado;

    private String descripcion;

    // Distribución por ítems (opcional, solo para obras)
    private DistribucionItemsDTO distribucionItems;
}
