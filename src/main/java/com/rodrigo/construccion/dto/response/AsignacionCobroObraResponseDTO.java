package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionCobroObraResponseDTO {

    private Long id;
    private Long cobroObraId;
    private Long obraId;
    private String obraNombre;
    private Long presupuestoNoClienteId;
    private Long empresaId;

    private BigDecimal montoAsignado;

    // Distribución por ítems
    private BigDecimal montoProfesionales;
    private BigDecimal montoMateriales;
    private BigDecimal montoGastosGenerales;

    private BigDecimal porcentajeProfesionales;
    private BigDecimal porcentajeMateriales;
    private BigDecimal porcentajeGastosGenerales;

    private String estado;
    private String observaciones;

    // Auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long usuarioCreacionId;
    private Long usuarioModificacionId;
}
