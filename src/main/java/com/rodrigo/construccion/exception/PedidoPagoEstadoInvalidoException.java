package com.rodrigo.construccion.exception;

public class PedidoPagoEstadoInvalidoException extends RuntimeException {

    public PedidoPagoEstadoInvalidoException(String message) {
        super(message);
    }

    public PedidoPagoEstadoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
