package com.rodrigo.construccion.dto.request;

import lombok.Data;

@Data
public class CambiarPinRequest {
    private String pinActual;
    private String pinNuevo;
}
