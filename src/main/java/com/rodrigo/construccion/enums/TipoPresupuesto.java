package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Enumeración para tipos de presupuesto no cliente.
 * Define las categorías de presupuestos soportadas por el sistema.
 *
 * Cada constante acepta dos nombres en el JSON entrante:
 *  - Nombre técnico original (backward-compatible con BD y datos existentes)
 *  - Nombre semántico nuevo (acordado con el frontend)
 */
public enum TipoPresupuesto {

    /**
     * Presupuesto Principal / Obra Principal
     * Alias frontend: PRESUPUESTO_PRINCIPAL
     * Estado inicial: BORRADOR → cliente aprueba → se genera la obra
     * Campos requeridos: nombreObra, direccionObraCalle, direccionObraAltura
     */
    @JsonAlias("PRESUPUESTO_PRINCIPAL")
    TRADICIONAL,

    /**
     * Presupuesto Trabajo Diario
     * Alias frontend: PRESUPUESTO_TRABAJO_DIARIO
     * Estado inicial: APROBADO (auto-aprobado) → obra se genera inmediatamente
     * Campos requeridos: nombreObra, direccionObraCalle, direccionObraAltura
     */
    @JsonAlias("PRESUPUESTO_TRABAJO_DIARIO")
    TRABAJO_DIARIO,

    /**
     * Presupuesto Adicional Obra
     * Alias frontend: PRESUPUESTO_ADICIONAL_OBRA
     * Estado inicial: BORRADOR → cliente aprueba → NO crea obra nueva
     * Campo OBLIGATORIO: obraId (vinculado a obra existente)
     * Cliente heredado de obra padre
     */
    @JsonAlias("PRESUPUESTO_ADICIONAL_OBRA")
    TRABAJO_EXTRA,

    /**
     * Presupuesto Tarea Leve
     * Alias frontend: PRESUPUESTO_TAREA_LEVE
     * Estado inicial: BORRADOR → el usuario edita → clic en "Aprobar" → cambia a TERMINADO
     * Campo OBLIGATORIO: idObra (obra padre: puede ser Obra Principal o Sub-Obra de TRABAJO_EXTRA)
     * Al APROBAR (cambiar estado a TERMINADO) crea su propia obra vinculada en estado TERMINADO
     * obraOrigenId = idObra del padre
     * Cliente heredado de obra padre
     */
    @JsonAlias("PRESUPUESTO_TAREA_LEVE")
    TAREA_LEVE,

    /**
     * LEGACY — mantener por compatibilidad con datos existentes
     */
    @Deprecated
    TRABAJOS_SEMANALES;
    
    /**
     * Obtiene el estado por defecto según el tipo de presupuesto.
     * 
     * @return Estado inicial correspondiente al tipo
     */
    public PresupuestoEstado getEstadoPorDefecto() {
        switch (this) {
            case TRABAJO_DIARIO:
            case TRABAJOS_SEMANALES:
                return PresupuestoEstado.APROBADO;
            case TAREA_LEVE:
            case TRABAJO_EXTRA:
            case TRADICIONAL:
            default:
                // TAREA_LEVE arranca en BORRADOR: el usuario puede editarlo antes de marcarlo TERMINADO
                return PresupuestoEstado.BORRADOR;
        }
    }
    
    /**
     * Verifica si el tipo de presupuesto requiere aprobación del cliente.
     * 
     * @return true si requiere aprobación, false en caso contrario
     */
    public boolean requiereAprobacionCliente() {
        return this == TRADICIONAL || this == TRABAJO_EXTRA;
    }
    
    /**
     * Verifica si el tipo de presupuesto se aprueba automáticamente.
     * 
     * @return true si se aprueba automáticamente, false en caso contrario
     */
    public boolean seApruebaAutomaticamente() {
        // TAREA_LEVE ya NO se auto-aprueba: tiene flujo BORRADOR → TERMINADO
        return this == TRABAJO_DIARIO || this == TRABAJOS_SEMANALES;
    }
    
    /**
     * Verifica si requiere obra asociada (obraId no nulo)
     * 
     * @return true si requiere obra existente, false si debe crear nueva obra
     */
    public boolean requiereObraExistente() {
        return this == TRABAJO_EXTRA || this == TAREA_LEVE;
    }
    
    /**
     * Verifica si debe crear obra nueva inmediatamente
     * 
     * @return true si debe crear obra inmediatamente al crear presupuesto
     */
    public boolean creaObraInmediatamente() {
        // TAREA_LEVE NO crea obra al POST - la crea al aprobar (cambiar estado a TERMINADO)
        return this == TRABAJO_DIARIO;
    }
    
    /**
     * Obtiene el valor para esPresupuestoTrabajoExtra
     * 
     * @return true si es presupuesto de trabajo extra
     */
    public boolean getEsPresupuestoTrabajoExtra() {
        return this == TRABAJO_EXTRA;
    }

    /**
     * Parsea un string a TipoPresupuesto aceptando tanto nombres técnicos
     * como aliases semánticos acordados con el frontend.
     *
     * Nombres técnicos (BD):      TRADICIONAL | TRABAJO_DIARIO | TRABAJO_EXTRA | TAREA_LEVE
     * Aliases semánticos (frontend): PRESUPUESTO_PRINCIPAL | PRESUPUESTO_TRABAJO_DIARIO |
     *                                PRESUPUESTO_ADICIONAL_OBRA | PRESUPUESTO_TAREA_LEVE
     *
     * @throws IllegalArgumentException si el valor no corresponde a ningún tipo conocido
     */
    public static TipoPresupuesto fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        switch (value.trim().toUpperCase()) {
            // Aliases semánticos del frontend
            case "PRESUPUESTO_PRINCIPAL":      return TRADICIONAL;
            case "PRESUPUESTO_TRABAJO_DIARIO": return TRABAJO_DIARIO;
            case "PRESUPUESTO_ADICIONAL_OBRA": return TRABAJO_EXTRA;
            case "PRESUPUESTO_TAREA_LEVE":     return TAREA_LEVE;
            // Nombres técnicos originales (backward-compatible)
            default:
                try {
                    return TipoPresupuesto.valueOf(value.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                        "Tipo de presupuesto inválido: '" + value + "'. " +
                        "Valores técnicos: TRADICIONAL, TRABAJO_DIARIO, TRABAJO_EXTRA, TAREA_LEVE. " +
                        "Aliases semánticos: PRESUPUESTO_PRINCIPAL, PRESUPUESTO_TRABAJO_DIARIO, " +
                        "PRESUPUESTO_ADICIONAL_OBRA, PRESUPUESTO_TAREA_LEVE"
                    );
                }
        }
    }
}
