package com.rodrigo.construccion.enums;

/**
 * Enum para tipos de pago consolidado.
 * Define las categorías de pagos que se pueden registrar.
 */
public enum TipoPagoConsolidado {
    MATERIALES("Materiales"),
    GASTOS_GENERALES("Gastos Generales"),
    OTROS_COSTOS("Otros Costos"),
    OTROS("Otros");
    
    private final String displayName;
    
    TipoPagoConsolidado(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static TipoPagoConsolidado fromString(String text) {
        if (text == null) return null;
        for (TipoPagoConsolidado tipo : TipoPagoConsolidado.values()) {
            if (tipo.name().equalsIgnoreCase(text) || tipo.displayName.equalsIgnoreCase(text)) {
                return tipo;
            }
        }
        return null;
    }
}
