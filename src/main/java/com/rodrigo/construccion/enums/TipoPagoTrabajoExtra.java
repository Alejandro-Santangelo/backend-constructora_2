package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración para los tipos de pago de trabajo extra
 */
public enum TipoPagoTrabajoExtra {
    
    PAGO_GENERAL("PAGO_GENERAL", "Pago General del Trabajo Extra"),
    PAGO_PROFESIONAL("PAGO_PROFESIONAL", "Pago a Profesional Específico"),
    PAGO_TAREA("PAGO_TAREA", "Pago por Tarea Específica");

    private final String valor;
    private final String displayName;

    TipoPagoTrabajoExtra(String valor, String displayName) {
        this.valor = valor;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convierte un string al enum correspondiente
     */
    public static TipoPagoTrabajoExtra fromString(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El tipo de pago no puede ser nulo");
        }
        
        for (TipoPagoTrabajoExtra tipo : TipoPagoTrabajoExtra.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        
        throw new IllegalArgumentException(
            "Tipo de pago no válido: " + valor + 
            ". Valores permitidos: PAGO_GENERAL, PAGO_PROFESIONAL, PAGO_TAREA"
        );
    }

    /**
     * Valida si un string es un tipo válido
     */
    public static boolean esValido(String valor) {
        if (valor == null) {
            return false;
        }
        
        for (TipoPagoTrabajoExtra tipo : TipoPagoTrabajoExtra.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return true;
            }
        }
        
        return false;
    }
}
