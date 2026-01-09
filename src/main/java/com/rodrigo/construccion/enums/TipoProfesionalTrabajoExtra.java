package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración para los tipos de profesional en trabajos extra
 */
public enum TipoProfesionalTrabajoExtra {
    
    ASIGNADO_OBRA("ASIGNADO_OBRA"),       // Profesional ya asignado a la obra
    LISTADO_GENERAL("LISTADO_GENERAL"),   // Profesional del listado general del sistema
    MANUAL("MANUAL");                      // Profesional ingresado manualmente

    private final String valor;

    TipoProfesionalTrabajoExtra(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    /**
     * Convierte un string al enum correspondiente
     */
    public static TipoProfesionalTrabajoExtra fromString(String valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El tipo de profesional no puede ser nulo");
        }
        
        for (TipoProfesionalTrabajoExtra tipo : TipoProfesionalTrabajoExtra.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        
        throw new IllegalArgumentException(
            "Tipo de profesional no válido: " + valor + 
            ". Valores permitidos: ASIGNADO_OBRA, LISTADO_GENERAL, MANUAL"
        );
    }

    /**
     * Valida si un string es un tipo válido
     */
    public static boolean esValido(String valor) {
        if (valor == null) {
            return false;
        }
        
        for (TipoProfesionalTrabajoExtra tipo : TipoProfesionalTrabajoExtra.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return true;
            }
        }
        
        return false;
    }
}
