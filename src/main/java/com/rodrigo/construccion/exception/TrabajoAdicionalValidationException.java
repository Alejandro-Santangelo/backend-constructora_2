package com.rodrigo.construccion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción para validaciones de negocio relacionadas con trabajos adicionales
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TrabajoAdicionalValidationException extends RuntimeException {

    public TrabajoAdicionalValidationException(String message) {
        super(message);
    }
}
