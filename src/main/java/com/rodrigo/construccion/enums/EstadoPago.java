package com.rodrigo.construccion.enums;

/**
 * Enum para estados de pago.
 */
public enum EstadoPago {
    PAGADO("Pagado"),
    PENDIENTE("Pendiente"),
    ANULADO("Anulado");
    
    private final String displayName;
    
    EstadoPago(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static EstadoPago fromString(String text) {
        if (text == null) return null;
        for (EstadoPago estado : EstadoPago.values()) {
            if (estado.name().equalsIgnoreCase(text) || estado.displayName.equalsIgnoreCase(text)) {
                return estado;
            }
        }
        return null;
    }
}
