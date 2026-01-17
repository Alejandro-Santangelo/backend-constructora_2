package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para asignar caja chica a múltiples profesionales")
public class AsignarCajaChicaMultipleRequest {

    /**
     * NOTA IMPORTANTE: Este campo NO debe ser enviado por el cliente en el body del request.
     * Es seteado automáticamente por el controlador con el valor del tenant actual (empresaId del token/header)
     * para garantizar seguridad multi-tenant y evitar que un usuario manipule este valor.
     */
    @NotNull(message = "El ID de empresa es obligatorio")
    @Schema(description = "ID de la empresa (seteado por el servidor, NO enviar en el request)",
            example = "3",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long empresaId;

    @NotNull(message = "El ID del presupuesto es obligatorio")
    @Schema(description = "ID del presupuesto no cliente", example = "123")
    private Long presupuestoNoClienteId;

    @NotNull(message = "La lista de profesionales es obligatoria")
    @Schema(description = "IDs de los profesionales a asignar caja chica")
    private List<Long> profesionalesIds;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    @Schema(description = "Monto a asignar a cada profesional", example = "5000.00")
    private BigDecimal monto;

    @Schema(description = "Fecha de asignación (opcional, default hoy)")
    private LocalDate fecha;

    @Schema(description = "Observaciones de la asignación")
    private String observaciones;
}
