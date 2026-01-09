package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum para los estados de cobros a nivel empresa.
 * 
 * Estados posibles:
 * - DISPONIBLE: Todo el monto está disponible para asignar
 * - ASIGNADO_PARCIAL: Parte del monto está asignado a obras
 * - ASIGNADO_TOTAL: Todo el monto está asignado a obras
 * - ANULADO: Cobro anulado
 */
public enum EstadoCobroEmpresa {
    
    DISPONIBLE("DISPONIBLE"),
    ASIGNADO_PARCIAL("ASIGNADO_PARCIAL"),
    ASIGNADO_TOTAL("ASIGNADO_TOTAL"),
    ANULADO("ANULADO");

    private final String displayName;

    EstadoCobroEmpresa(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public static EstadoCobroEmpresa fromDisplayName(String displayName) {
        if (displayName == null) return null;
        for (EstadoCobroEmpresa estado : EstadoCobroEmpresa.values()) {
            if (estado.getDisplayName().equalsIgnoreCase(displayName)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de cobro empresa no válido: " + displayName);
    }
    
    public static EstadoCobroEmpresa fromString(String text) {
        if (text == null) return null;
        for (EstadoCobroEmpresa estado : EstadoCobroEmpresa.values()) {
            if (estado.name().equalsIgnoreCase(text) || estado.displayName.equalsIgnoreCase(text)) {
                return estado;
            }
        }
        return null;
    }
}
