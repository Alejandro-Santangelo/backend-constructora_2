package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeración para los estados de pago de trabajos extra
 * Aplica tanto a profesionales como a tareas
 */
public enum EstadoPagoTrabajoExtra {
    
    PENDIENTE("PENDIENTE", "Pago Pendiente"),
    PAGADO_PARCIAL("PAGADO_PARCIAL", "Pagado Parcialmente"),
    PAGADO_TOTAL("PAGADO_TOTAL", "Pagado Totalmente");

    private final String valor;
    private final String displayName;

    EstadoPagoTrabajoExtra(String valor, String displayName) {
        this.valor = valor;
        this.displayName = displayName;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convierte un string al enum correspondiente
     */
    public static EstadoPagoTrabajoExtra fromString(String valor) {
        if (valor == null) {
            return PENDIENTE; // Default
        }
        
        for (EstadoPagoTrabajoExtra estado : EstadoPagoTrabajoExtra.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        
        return PENDIENTE; // Default si no coincide
    }

    /**
     * Valida si un string es un estado válido
     */
    public static boolean esValido(String valor) {
        if (valor == null) {
            return false;
        }
        
        for (EstadoPagoTrabajoExtra estado : EstadoPagoTrabajoExtra.values()) {
            if (estado.valor.equalsIgnoreCase(valor)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Determina el estado de pago según el monto total y el monto pagado
     */
    public static EstadoPagoTrabajoExtra determinarEstado(java.math.BigDecimal montoTotal, java.math.BigDecimal montoPagado) {
        if (montoPagado == null || montoPagado.compareTo(java.math.BigDecimal.ZERO) == 0) {
            return PENDIENTE;
        }
        
        if (montoPagado.compareTo(montoTotal) >= 0) {
            return PAGADO_TOTAL;
        }
        
        return PAGADO_PARCIAL;
    }
}
