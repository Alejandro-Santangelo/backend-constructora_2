package com.rodrigo.construccion.enums;

/**
 * Enum para métodos de pago.
 */
public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia"),
    CHEQUE("Cheque"),
    DEBITO("Débito"),
    CREDITO("Crédito");
    
    private final String displayName;
    
    MetodoPago(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static MetodoPago fromString(String text) {
        if (text == null) return null;
        for (MetodoPago metodo : MetodoPago.values()) {
            if (metodo.name().equalsIgnoreCase(text) || metodo.displayName.equalsIgnoreCase(text)) {
                return metodo;
            }
        }
        return null;
    }
}
