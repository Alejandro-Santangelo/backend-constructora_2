package com.rodrigo.construccion.enums;

/**
 * Estados válidos para presupuestos-no-cliente y obras.
 * SINCRONIZADO CON EstadoObra - Ambos enums comparten los mismos estados.
 * 
 * ESTADOS ACTUALES (10 estados):
 * - BORRADOR: Presupuesto en borrador
 * - A_ENVIAR: Estado inicial cuando se crea un presupuesto tradicional
 * - ENVIADO: Presupuesto enviado al cliente
 * - MODIFICADO: Presupuesto que fue modificado después de enviado/aprobado
 * - APROBADO: Presupuesto aprobado por el cliente (o auto-aprobado si es tipo TRABAJOS_SEMANALES)
 * - OBRA_A_CONFIRMAR: Obra pendiente de confirmación (SOLO para presupuestos TRABAJOS_SEMANALES)
 * - EN_EJECUCION: Obra en ejecución (usado en Sistema Financiero)
 * - SUSPENDIDA: Obra temporalmente suspendida
 * - TERMINADO: Obra terminada
 * - CANCELADO: Presupuesto o obra cancelada
 * 
 * ESTADOS OBSOLETOS ELIMINADOS:
 * - PENDIENTE, EN_REVISION, RECHAZADO, ACTIVO, VENCIDO, FINALIZADO, VIGENTE
 * - completado, en proceso, pendiente (estados antiguos del sistema)
 */
public enum PresupuestoEstado {
    BORRADOR("Borrador"),           // Presupuesto en borrador
    A_ENVIAR("A enviar"),           // Estado inicial tradicional
    ENVIADO("Enviado"),             // Enviado al cliente
    MODIFICADO("Modificado"),       // Modificado post-envío
    APROBADO("APROBADO"),           // Aprobado (también auto-aprobado para TRABAJOS_SEMANALES)
    OBRA_A_CONFIRMAR("Obra a confirmar"), // Obra pendiente de confirmación (SOLO TRABAJOS_SEMANALES)
    EN_EJECUCION("EN EJECUCION"),   // Obra en ejecución (usado en Sistema Financiero)
    SUSPENDIDA("SUSPENDIDA"),       // Obra temporalmente suspendida
    TERMINADO("TERMINADO"),         // Obra terminada
    CANCELADO("CANCELADO");         // Presupuesto o obra cancelada
    
    private final String displayValue;
    
    PresupuestoEstado(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
    
    /**
     * Convierte un string a PresupuestoEstado.
     * Soporta tanto el nombre del enum como el displayValue.
     * 
     * Ejemplos:
     * - "A enviar" → A_ENVIAR
     * - "A_ENVIAR" → A_ENVIAR
     * - "APROBADO" → APROBADO
     * - "EN EJECUCION" → EN_EJECUCION
     * - "EN_EJECUCION" → EN_EJECUCION
     * 
     * @param s String a convertir
     * @return PresupuestoEstado o null si no es válido
     */
    public static PresupuestoEstado fromString(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        
        String normalized = s.trim();
        
        // Primero intentar match exacto con displayValue (case-sensitive para mantener formato)
        for (PresupuestoEstado estado : values()) {
            if (estado.displayValue.equals(normalized)) {
                return estado;
            }
        }
        
        // Luego intentar normalizar y buscar por nombre de enum
        String key = normalized.toUpperCase()
                               .replaceAll("\\s+", "_")
                               .replaceAll("[^A-Z0-9_]", "_");
        
        switch (key) {
            case "BORRADOR": return BORRADOR;
            case "A_ENVIAR": return A_ENVIAR;
            case "ENVIADO": return ENVIADO;
            case "MODIFICADO": return MODIFICADO;
            case "APROBADO": return APROBADO;
            case "OBRA_A_CONFIRMAR": return OBRA_A_CONFIRMAR;
            case "EN_EJECUCION": return EN_EJECUCION;
            case "SUSPENDIDA": return SUSPENDIDA;
            case "TERMINADO": return TERMINADO;
            case "CANCELADO": return CANCELADO;
            
            // Estados obsoletos - migrar automáticamente
            case "PENDIENTE": return A_ENVIAR;
            case "EN_PROCESO": return EN_EJECUCION;
            case "COMPLETADO": return TERMINADO;
            case "CANCELADA": return CANCELADO;  // Compatibilidad femenino/masculino
            case "FINALIZADA": return TERMINADO;
            case "EN_PLANIFICACION": return BORRADOR;
            case "EN_REVISION": return ENVIADO;
            case "EN_OBRA": return EN_EJECUCION;
            
            default: return null;
        }
    }
    
    /**
     * Verifica si el estado es válido para crear una obra.
     * Solo presupuestos APROBADOS, MODIFICADOS o EN_EJECUCION pueden crear obras.
     */
    public boolean puedeCrearObra() {
        return this == APROBADO || this == MODIFICADO || this == EN_EJECUCION;
    }
    
    /**
     * Verifica si el estado representa una obra activa.
     * Usado por el Sistema Financiero para filtrar obras disponibles.
     */
    public boolean esObraActiva() {
        return this == APROBADO || this == MODIFICADO || this == EN_EJECUCION;
    }
}
