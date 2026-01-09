package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registrar un nuevo retiro personal
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetiroPersonalRequestDTO {

    @NotNull(message = "El ID de empresa es requerido")
    private Long empresaId;

    private Long obraId;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "La fecha de retiro es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaRetiro;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    @Pattern(regexp = "GANANCIA|PRESTAMO|GASTO_PERSONAL", 
             message = "Tipo de retiro inválido. Valores permitidos: GANANCIA, PRESTAMO, GASTO_PERSONAL")
    private String tipoRetiro = "GANANCIA";

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}
