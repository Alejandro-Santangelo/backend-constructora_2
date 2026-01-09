package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO con desglose financiero detallado
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesgloseFinancieroDTO {

    private ResumenCobrosDTO cobros;
    private ResumenAsignacionesDTO asignaciones;
    private ResumenRetirosDTO retiros;
}
