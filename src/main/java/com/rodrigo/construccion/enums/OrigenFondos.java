package com.rodrigo.construccion.enums;

/**
 * Enum para indicar el origen de los fondos de un gasto/otro costo.
 * Utilizado cuando el presupuesto es 0 para rastrear de dónde salen los fondos.
 */
public enum OrigenFondos {
    RETIRO_DIRECTO("Retiro Directo"),
    PRESUPUESTO_MATERIALES("Presupuesto de Materiales");
    
    private final String displayName;
    
    OrigenFondos(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Convierte un string al enum correspondiente.
     * @param text Nombre del enum o displayName
     * @return El enum correspondiente o null si no existe
     */
    public static OrigenFondos fromString(String text) {
        if (text == null) return null;
        for (OrigenFondos origen : OrigenFondos.values()) {
            if (origen.name().equalsIgnoreCase(text) || origen.displayName.equalsIgnoreCase(text)) {
                return origen;
            }
        }
        return null;
    }
}
