package com.rodrigo.construccion.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para la distribución de un cobro por ítems
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistribucionItemsDTO {

    private BigDecimal montoProfesionales;
    private BigDecimal porcentajeProfesionales;

    private BigDecimal montoMateriales;
    private BigDecimal porcentajeMateriales;

    private BigDecimal montoGastosGenerales;
    private BigDecimal porcentajeGastosGenerales;

    private BigDecimal montoTrabajosExtra;
    private BigDecimal porcentajeTrabajosExtra;
}
