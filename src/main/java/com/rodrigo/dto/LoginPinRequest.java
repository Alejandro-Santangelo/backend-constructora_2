package com.rodrigo.dto;

import lombok.Data;

@Data
public class LoginPinRequest {
    private String pin;
    private Long empresaId; // Opcional: si se proporciona, valida que el PIN corresponda a esa empresa
}
