package com.rodrigo.construccion.exception;

/**
 * Excepción lanzada cuando un profesional intenta realizar un gasto
 * pero no tiene saldo suficiente en su caja chica.
 */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String message) {
        super(message);
    }

    public SaldoInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
