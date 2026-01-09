package com.rodrigo.construccion.exception;

/**
 * Excepción lanzada cuando se intenta hacer check-out
 * pero no existe un check-in previo para el profesional.
 */
public class CheckInNoEncontradoException extends RuntimeException {

    public CheckInNoEncontradoException(String message) {
        super(message);
    }

    public CheckInNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
