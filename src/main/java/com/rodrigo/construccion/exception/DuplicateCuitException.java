package com.rodrigo.construccion.exception;

public class DuplicateCuitException extends RuntimeException {

    public DuplicateCuitException(String entityType, String cuit) {
        super(String.format("Ya existe un %s con el CUIT: %s", entityType, cuit));
    }

    public DuplicateCuitException(String entityType, String cuit, Long empresaId) {
        super(String.format("Ya existe un %s con el CUIT: %s en la empresa con ID: %d", entityType, cuit, empresaId));
    }

}
