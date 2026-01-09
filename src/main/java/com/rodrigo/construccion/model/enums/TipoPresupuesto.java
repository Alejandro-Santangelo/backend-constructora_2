package com.rodrigo.construccion.model.enums;

/**
 * Enumeración para tipos de presupuesto no cliente.
 * Define las categorías de presupuestos soportadas por el sistema.
 */
public enum TipoPresupuesto {
    /**
     * Presupuesto tradicional con flujo normal de estados.
     * Estado inicial: "A enviar"
     * Requiere aprobación del cliente antes de crear obra.
     */
    TRADICIONAL,
    
    /**
     * Trabajos semanales o recurrentes.
     * Estado inicial: "APROBADO" (automático)
     * No requiere envío ni aprobación del cliente.
     * Se puede crear obra directamente.
     */
    TRABAJOS_SEMANALES;
    
    /**
     * Obtiene el estado por defecto según el tipo de presupuesto.
     * 
     * @return Estado inicial correspondiente al tipo
     */
    public String getEstadoPorDefecto() {
        switch (this) {
            case TRABAJOS_SEMANALES:
                return "APROBADO";
            case TRADICIONAL:
            default:
                return "A enviar";
        }
    }
    
    /**
     * Verifica si el tipo de presupuesto requiere aprobación del cliente.
     * 
     * @return true si requiere aprobación, false en caso contrario
     */
    public boolean requiereAprobacionCliente() {
        return this == TRADICIONAL;
    }
    
    /**
     * Verifica si el tipo de presupuesto se aprueba automáticamente.
     * 
     * @return true si se aprueba automáticamente, false en caso contrario
     */
    public boolean esAprobacionAutomatica() {
        return this == TRABAJOS_SEMANALES;
    }
}
