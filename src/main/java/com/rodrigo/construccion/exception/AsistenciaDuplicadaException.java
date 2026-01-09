package com.rodrigo.construccion.exception;

/**
 * Excepción lanzada cuando se intenta registrar una asistencia
 * para un profesional que ya tiene un registro para la misma fecha.
 */
public class AsistenciaDuplicadaException extends RuntimeException {

    public AsistenciaDuplicadaException(String message) {
        super(message);
    }

    public AsistenciaDuplicadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
