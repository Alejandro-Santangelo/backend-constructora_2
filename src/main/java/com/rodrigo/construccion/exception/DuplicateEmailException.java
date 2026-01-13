package com.rodrigo.construccion.exception;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Excepción lanzada cuando se intenta crear o actualizar un usuario
 * con un email que ya existe en la empresa.
 */
public class DuplicateEmailException extends DataIntegrityViolationException {

    public DuplicateEmailException(String email, String empresaNombre) {
        super(buildMessage(email, empresaNombre));
    }

    public DuplicateEmailException(String email) {
        super(buildSimpleMessage(email));
    }

    private static String buildMessage(String email, String empresaNombre) {
        return String.format("El email '%s' ya está registrado en la empresa '%s'.",
                email, empresaNombre);
    }

    private static String buildSimpleMessage(String email) {
        return String.format("El email '%s' ya está registrado.", email);
    }
}

