package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobarPresupuestoResponse {
    private Long obraId;
    private Integer presupuestosActualizados;
    private String mensaje;
    private boolean obraCreada;
    private boolean clienteReutilizado;
    private Long clienteId;
}
