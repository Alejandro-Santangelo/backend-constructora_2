package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO Request para asignar caja chica a un profesional en una obra
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para asignar dinero de caja chica a un profesional")
public class AsignarCajaChicaRequest {

    @NotNull(message = "El monto es obligatorio")
    @PositiveOrZero(message = "El monto debe ser mayor o igual a cero")
    @Schema(description = "Monto a asignar de caja chica", example = "50000.00", required = true)
    private BigDecimal monto;

    @NotNull(message = "El ID de empresa es obligatorio")
    @Schema(description = "ID de la empresa", example = "1", required = true)
    private Long empresaId;
}
