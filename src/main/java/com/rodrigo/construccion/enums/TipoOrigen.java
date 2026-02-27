package com.rodrigo.construccion.enums;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Enumeración que define el tipo/origen de una obra.
 * Determina cómo fue generada y cómo debe mostrarse en el frontend.
 *
 * Contrato con frontend (obraTypes.js):
 *   OBRA_PRINCIPAL      = obra generada al aprobar presupuesto TRADICIONAL
 *   OBRA_TRABAJO_DIARIO = obra generada automáticamente desde presupuesto TRABAJO_DIARIO
 *   TRABAJO_EXTRA       = adicional sobre obra existente (presupuesto TRABAJO_EXTRA)
 *   TRABAJO_ADICIONAL   = tarea leve sobre obra existente (presupuesto TAREA_LEVE)
 *   OBRA_INDEPENDIENTE  = obra creada manualmente, sin presupuesto
 */
public enum TipoOrigen {

    // =====================================================================
    // VALORES ACTUALES (acordados con frontend)
    // =====================================================================

    /** Obra generada al aprobar un presupuesto TRADICIONAL (Obra Principal) */
    @JsonAlias("PRESUPUESTO_PRINCIPAL")
    OBRA_PRINCIPAL,

    /** Obra generada automáticamente al crear un presupuesto TRABAJO_DIARIO */
    @JsonAlias("PRESUPUESTO_TRABAJO_DIARIO")
    OBRA_TRABAJO_DIARIO,

    /**
     * Adicional sobre obra existente.
     * Se asigna a la obra vinculada cuando se aprueba un presupuesto TRABAJO_EXTRA.
     */
    @JsonAlias("PRESUPUESTO_ADICIONAL_OBRA")
    TRABAJO_EXTRA,

    /**
     * Tarea leve sobre obra existente.
     * Se asigna a la obra vinculada cuando se aprueba un presupuesto TAREA_LEVE.
     */
    @JsonAlias("PRESUPUESTO_TAREA_LEVE")
    TRABAJO_ADICIONAL,

    /** Obra creada manualmente, sin presupuesto previo */
    OBRA_INDEPENDIENTE,

    // =====================================================================
    // VALORES LEGACY (backward-compatible con datos existentes en BD)
    // =====================================================================

    /** @deprecated Usar OBRA_PRINCIPAL */
    @Deprecated
    TRADICIONAL,

    /** @deprecated Usar OBRA_TRABAJO_DIARIO */
    @Deprecated
    TRABAJO_DIARIO,

    /** @deprecated Usar OBRA_INDEPENDIENTE */
    @Deprecated
    MANUAL,

    /** Obras migradas de sistema anterior */
    LEGACY;

    // =====================================================================
    // Métodos de conversión
    // =====================================================================

    /**
     * Mapea un TipoPresupuesto al TipoOrigen correspondiente para la obra resultante.
     * Usa los valores nuevos para todas las obras creadas a partir de ahora.
     *
     * @param tipoPresupuesto tipo del presupuesto que origina la obra
     * @return TipoOrigen a persistir en la obra
     */
    public static TipoOrigen fromTipoPresupuesto(TipoPresupuesto tipoPresupuesto) {
        if (tipoPresupuesto == null) {
            return OBRA_INDEPENDIENTE;
        }
        switch (tipoPresupuesto) {
            case TRADICIONAL:           return OBRA_PRINCIPAL;
            case TRABAJO_DIARIO:        return OBRA_TRABAJO_DIARIO;
            case TRABAJO_EXTRA:         return TRABAJO_EXTRA;
            case TAREA_LEVE:            return TRABAJO_ADICIONAL;
            case TRABAJOS_SEMANALES:    return OBRA_TRABAJO_DIARIO; // legacy → más cercano semánticamente
            default:                    return OBRA_INDEPENDIENTE;
        }
    }
}