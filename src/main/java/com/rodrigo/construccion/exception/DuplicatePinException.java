package com.rodrigo.construccion.exception;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Excepción lanzada cuando se intenta crear un usuario o cambiar un PIN
 * a un valor que ya está en uso por otro usuario en el sistema.
 */
public class DuplicatePinException extends DataIntegrityViolationException {

    public DuplicatePinException(String pin) {
        super(buildMessage(pin));
    }

    private static String buildMessage(String pin) {
        return String.format("El PIN '%s' ya está en uso por otro usuario. Por favor, elija un PIN diferente.", pin);
    }
}
