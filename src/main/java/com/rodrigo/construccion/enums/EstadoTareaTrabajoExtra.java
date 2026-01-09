package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración para los estados de tareas en trabajos extra
 */
public enum EstadoTareaTrabajoExtra {
    
    TERMINADA("TERMINADA"),
    A_TERMINAR("A_TERMINAR"),
    POSTERGADA("POSTERGADA"),
    SUSPENDIDA("SUSPENDIDA");

    private final String valor;

    EstadoTareaTrabajoExtra(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    /**
     * Convierte un string al enum correspondiente
     */
    public static EstadoTareaTrabajoExtra fromString(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El estado de tarea no puede ser nulo");
        }
        
        for (EstadoTareaTrabajoExtra estado : EstadoTareaTrabajoExtra.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        
        throw new IllegalArgumentException(
            "Estado de tarea no válido: " + valor + 
            ". Valores permitidos: TERMINADA, A_TERMINAR, POSTERGADA, SUSPENDIDA"
        );
    }

    /**
     * Valida si un string es un estado válido
     */
    public static boolean esValido(String valor) {
        if (valor == null) {
            return false;
        }
        
        for (EstadoTareaTrabajoExtra estado : EstadoTareaTrabajoExtra.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return true;
            }
        }
        
        return false;
    }
}
