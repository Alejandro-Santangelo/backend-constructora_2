package com.rodrigo.dto;

import lombok.Data;

@Data
public class CambiarPinRequest {
    private String pinActual;
    private String pinNuevo;
}
