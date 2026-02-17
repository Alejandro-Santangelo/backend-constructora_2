package com.rodrigo.construccion.enums;

/**
 * Tipos de entidades que pueden participar en el sistema financiero unificado.
 *
 * <ul>
 *   <li>OBRA_PRINCIPAL        – Obra con presupuesto_no_cliente vinculado.</li>
 *   <li>OBRA_INDEPENDIENTE    – Obra manual (esObraManual=true) sin presupuesto.</li>
 *   <li>TRABAJO_EXTRA         – TrabajoExtra registrado en una obra.</li>
 *   <li>TRABAJO_ADICIONAL     – TrabajoAdicional asociado a una obra o trabajo extra.</li>
 * </ul>
 */
public enum TipoEntidadFinanciera {
    OBRA_PRINCIPAL,
    OBRA_INDEPENDIENTE,
    TRABAJO_EXTRA,
    TRABAJO_ADICIONAL
}
