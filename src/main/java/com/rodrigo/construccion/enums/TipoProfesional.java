package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoProfesional {
    ARQUITECTO("Arquitecto"),
    INGENIERO("Ingeniero"),
    MAESTRO_MAYOR("Maestro Mayor"),
    ALBANIL("Albañil"),
    ELECTRICISTA("Electricista"),
    PLOMERO("Plomero"),
    PINTOR("Pintor"),
    CARPINTERO("Carpintero"),
    DISENIADOR("Diseñador"),
    EBANISTA("Ebanista"),
    TAPICERO("Tapicero"),
    AGENTE("Agente"),
    PERITO("Perito"),
    LIQUIDADOR("Liquidador");

    private final String displayName;

    TipoProfesional(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Busca el valor del Enum a partir de su nombre legible (displayName).
     * Es case-insensitive.
     * Si no se encuentra una coincidencia, devuelve null en lugar de lanzar una excepción.
     * 
     * @param displayName El String como "Arquitecto".
     * @return El Enum correspondiente (ej: TipoProfesional.ARQUITECTO) o null si no existe.
     */
    public static TipoProfesional fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        for (TipoProfesional tipo : TipoProfesional.values()) {
            if (tipo.getDisplayName().equalsIgnoreCase(displayName.trim())) {
                return tipo;
            }
        }
        return null; // No se encontró, es un tipo nuevo o un error de tipeo.
    }
}
