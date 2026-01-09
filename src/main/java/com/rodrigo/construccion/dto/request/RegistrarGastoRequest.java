package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO Request para registrar un gasto de caja chica
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para registrar un gasto del profesional usando caja chica")
public class RegistrarGastoRequest {

    @NotNull(message = "El ID del profesional obra es obligatorio")
    @Schema(description = "ID de la asignación del profesional a la obra", example = "1", required = true)
    private Long profesionalObraId;

    @NotNull(message = "El monto es obligatorio")
    @PositiveOrZero(message = "El monto debe ser mayor o igual a cero")
    @Schema(description = "Monto del gasto", example = "5000.00", required = true)
    private BigDecimal monto;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción del gasto", example = "Materiales urgentes para obra")
    private String descripcion;

    @Schema(description = "Foto del ticket en base64 o URL", 
            example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...")
    private String fotoTicket;

    @NotNull(message = "El ID de empresa es obligatorio")
    @Schema(description = "ID de la empresa", example = "1", required = true)
    private Long empresaId;
}
