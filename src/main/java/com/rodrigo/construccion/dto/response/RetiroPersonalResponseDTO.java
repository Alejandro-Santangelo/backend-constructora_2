package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para retiro personal
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetiroPersonalResponseDTO {

    private Long id;
    private Long empresaId;
    private Long obraId;
    private BigDecimal monto;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaRetiro;

    private String motivo;
    private String tipoRetiro;
    private String estado;
    private String observaciones;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaModificacion;

    private Long usuarioCreacionId;
    private Long usuarioModificacionId;
}
