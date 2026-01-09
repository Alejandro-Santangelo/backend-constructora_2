package com.rodrigo.construccion.exception;

/**
 * Excepción lanzada cuando se detectan horarios inválidos
 * (ej: hora_salida < hora_entrada, fecha futura, etc.)
 */
public class HorarioInvalidoException extends RuntimeException {

    public HorarioInvalidoException(String message) {
        super(message);
    }

    public HorarioInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
