package com.rodrigo.construccion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para respuestas de trabajo adicional
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoAdicionalResponseDTO {

    private Long id;

    private String nombre;

    private BigDecimal importe;

    private Integer diasNecesarios;

    private LocalDate fechaInicio;

    private String descripcion;

    private String observaciones;

    private Long obraId;

    private Long trabajoExtraId;

    private Long empresaId;

    private String estado;

    private String fechaCreacion;

    private String fechaActualizacion;

    /**
     * Lista de profesionales asignados
     */
    @Builder.Default
    private List<TrabajoAdicionalProfesionalDTO> profesionales = new ArrayList<>();
}
