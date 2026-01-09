package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración para los estados de etapas diarias
 */
public enum EstadoEtapaDiaria {
    
    TERMINADA("TERMINADA"),
    EN_PROCESO("EN_PROCESO"),
    SUSPENDIDA("SUSPENDIDA"),
    MODIFICADA("MODIFICADA"),
    CANCELADA("CANCELADA");

    private final String valor;

    EstadoEtapaDiaria(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    /**
     * Convierte un string al enum correspondiente
     */
    public static EstadoEtapaDiaria fromString(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El estado de etapa diaria no puede ser nulo");
        }
        
        for (EstadoEtapaDiaria estado : EstadoEtapaDiaria.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        
        throw new IllegalArgumentException(
            "Estado de etapa diaria no válido: " + valor + 
            ". Valores permitidos: TERMINADA, EN_PROCESO, SUSPENDIDA, MODIFICADA, CANCELADA"
        );
    }

    /**
     * Valida si un string es un estado válido
     */
    public static boolean esValido(String valor) {
        if (valor == null) {
            return false;
        }
        
        for (EstadoEtapaDiaria estado : EstadoEtapaDiaria.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return true;
            }
        }
        
        return false;
    }
}
