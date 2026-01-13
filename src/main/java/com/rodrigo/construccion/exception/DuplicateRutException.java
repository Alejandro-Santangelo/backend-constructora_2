package com.rodrigo.construccion.exception;

/**
 * Excepción lanzada cuando se intenta crear o actualizar un proveedor con un RUT que ya existe
 */
public class DuplicateRutException extends RuntimeException {

    public DuplicateRutException(String entityType, String rut) {
        super(String.format("Ya existe un %s con el RUT: %s", entityType, rut));
    }

    public DuplicateRutException(String entityType, String rut, Long empresaId) {
        super(String.format("Ya existe un %s con el RUT: %s en la empresa con ID: %d", entityType, rut, empresaId));
    }
}

