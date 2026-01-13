package com.rodrigo.construccion.exception;

/**
 * Excepción lanzada cuando se intenta modificar o eliminar un costo que ya está aprobado.
 */
public class ApprovedCostoException extends RuntimeException {

    public ApprovedCostoException(String mensaje) {
        super(mensaje);
    }

    public ApprovedCostoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

