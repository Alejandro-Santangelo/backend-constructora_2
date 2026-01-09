package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.response.ObraResumenFinancieroDTO;

public interface IObraFinancieroService {

    /**
     * Obtener resumen financiero completo de una obra
     * Incluye: presupuesto, cobros, pagos, gastos y balances
     */
    ObraResumenFinancieroDTO obtenerResumenFinanciero(Long obraId);
}
