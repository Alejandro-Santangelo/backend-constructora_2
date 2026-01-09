package com.rodrigo.construccion.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class DuplicateAssignmentException extends DataIntegrityViolationException {

    public DuplicateAssignmentException(String profesionalNombre, String obraNombre) {
        super(buildMessage(profesionalNombre, obraNombre));
    }

    private static String buildMessage(String profesionalNombre, String obraNombre) {
        return String.format("No puedes realizar esta actualización porque resultaría en un duplicado. El profesional '%s' ya está asignado a la obra '%s'.",
                profesionalNombre, obraNombre);
    }
}