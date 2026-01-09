package com.rodrigo.construccion.dto.request;

import com.rodrigo.construccion.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para crear un nuevo cobro a nivel empresa
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CobroEmpresaRequestDTO {

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    @NotNull(message = "El monto total es obligatorio")
    @Positive(message = "El monto total debe ser mayor a cero")
    private BigDecimal montoTotal;

    private String descripcion;

    @NotNull(message = "La fecha de cobro es obligatoria")
    private LocalDate fechaCobro;

    private String metodoPago; // Se convertirá a enum MetodoPago

    private String numeroComprobante;

    private String tipoComprobante;

    private String observaciones;
}
