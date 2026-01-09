package com.rodrigo.construccion.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para representar una asignación de cobro empresa a una obra específica
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionObraDTO {

    @NotNull(message = "La obra es obligatoria")
    private Long obraId;

    @NotNull(message = "El monto asignado es obligatorio")
    @Positive(message = "El monto asignado debe ser mayor a cero")
    private BigDecimal montoAsignado;

    private String descripcion;

    // Distribución por ítems (opcional)
    private DistribucionItemsDTO distribucionItems;
}
