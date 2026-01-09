package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.response.ObraResumenFinancieroDTO;
import com.rodrigo.construccion.service.IObraFinancieroService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/obras-financiero")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*")
public class ObraFinancieroController {

    private final IObraFinancieroService obraFinancieroService;

    /**
     * GET /api/v1/obras-financiero/{obraId}/resumen
     * Obtener resumen financiero completo de una obra
     */
    @GetMapping("/{obraId}/resumen")
    public ResponseEntity<ObraResumenFinancieroDTO> obtenerResumenFinanciero(
            @PathVariable Long obraId,
            @RequestParam Long empresaId) {
        ObraResumenFinancieroDTO resumen = obraFinancieroService.obtenerResumenFinanciero(obraId);
        return ResponseEntity.ok(resumen);
    }
}
