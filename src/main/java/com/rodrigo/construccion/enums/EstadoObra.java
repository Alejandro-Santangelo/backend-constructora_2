package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración para los estados de una Obra.
 * SINCRONIZADO CON PresupuestoEstado - Ambos enums comparten exactamente los mismos estados.
 * 
 * ESTADOS (10 estados):
 * - BORRADOR: Presupuesto en borrador
 * - A_ENVIAR: Estado inicial cuando se crea un presupuesto tradicional
 * - ENVIADO: Presupuesto enviado al cliente
 * - MODIFICADO: Presupuesto que fue modificado después de enviado/aprobado
 * - APROBADO: Presupuesto aprobado por el cliente
 * - OBRA_A_CONFIRMAR: Obra pendiente de confirmación
 * - EN_EJECUCION: Obra en ejecución
 * - SUSPENDIDA: Obra temporalmente suspendida
 * - TERMINADO: Obra terminada
 * - CANCELADO: Presupuesto o obra cancelada
 */
public enum EstadoObra {
    BORRADOR("BORRADOR"),
    A_ENVIAR("A_ENVIAR"),
    ENVIADO("ENVIADO"),
    MODIFICADO("MODIFICADO"),
    APROBADO("APROBADO"),
    OBRA_A_CONFIRMAR("OBRA_A_CONFIRMAR"),
    EN_EJECUCION("EN_EJECUCION"),
    SUSPENDIDA("SUSPENDIDA"),
    TERMINADO("TERMINADO"),
    CANCELADO("CANCELADO");

    private final String displayName;

    EstadoObra(String displayName) {
        this.displayName = displayName;
    }

    /* Devuelve el nombre legible para mostrar en la UI. */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Busca el valor del Enum a partir de su nombre legible (displayName).
     * Soporta estados legacy para compatibilidad con datos existentes.
     * @param displayName El String como "BORRADOR", "En obra", "Cancelada".
     * @return El Enum correspondiente.
     * @throws IllegalArgumentException si no se encuentra el valor.
     */
    public static EstadoObra fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("displayName no puede ser nulo o vacío");
        }
        
        String normalized = displayName.trim().toUpperCase()
                                      .replaceAll("\\s+", "_")
                                      .replaceAll("[^A-Z0-9_]", "_");
        
        // Primero intentar match exacto
        for (EstadoObra estado : EstadoObra.values()) {
            if (estado.getDisplayName().equalsIgnoreCase(displayName)) {
                return estado;
            }
        }
        
        // Mapeo de estados legacy
        switch (normalized) {
            case "EN_PLANIFICACION": return BORRADOR;
            case "EN_REVISION": return ENVIADO;
            case "EN_OBRA": return EN_EJECUCION;
            case "FINALIZADA": return TERMINADO;
            case "CANCELADA": return CANCELADO;
            default:
                // Intentar buscar por nombre del enum
                try {
                    return EstadoObra.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Valor desconocido para EstadoObra: " + displayName);
                }
        }
    }
    
    /**
     * Convierte un PresupuestoEstado a EstadoObra.
     * Ambos enums están sincronizados y comparten los mismos valores.
     */
    public static EstadoObra fromPresupuestoEstado(com.rodrigo.construccion.enums.PresupuestoEstado presupuestoEstado) {
        if (presupuestoEstado == null) return null;
        return EstadoObra.valueOf(presupuestoEstado.name());
    }
}