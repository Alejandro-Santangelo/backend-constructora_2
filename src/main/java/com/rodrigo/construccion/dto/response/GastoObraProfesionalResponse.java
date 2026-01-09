package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO Response para gastos de obra profesional
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con información de un gasto registrado")
public class GastoObraProfesionalResponse {

    @Schema(description = "ID del gasto", example = "1")
    private Long id;

    @Schema(description = "ID de la asignación profesional-obra", example = "1")
    private Long profesionalObraId;

    @Schema(description = "Nombre del profesional", example = "Juan Pérez")
    private String nombreProfesional;

    @Schema(description = "Dirección completa de la obra", example = "Av. Corrientes 1234 Piso 5 Depto A")
    private String direccionObra;

    @Schema(description = "Monto del gasto", example = "5000.00")
    private BigDecimal monto;

    @Schema(description = "Descripción del gasto", example = "Materiales urgentes")
    private String descripcion;

    @Schema(description = "Fecha y hora del gasto", example = "2025-10-22T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;

    @Schema(description = "Foto del ticket (base64 o URL)")
    private String fotoTicket;

    @Schema(description = "Saldo restante después del gasto", example = "30000.00")
    private BigDecimal saldoRestante;
}
