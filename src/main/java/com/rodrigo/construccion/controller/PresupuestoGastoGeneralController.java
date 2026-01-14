package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.model.entity.PresupuestoGastoGeneral;
import com.rodrigo.construccion.service.IPresupuestoGastoGeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/presupuesto-gasto-general")
public class PresupuestoGastoGeneralController {

    @Autowired
    private IPresupuestoGastoGeneralService service;

    @GetMapping
    public ResponseEntity<List<PresupuestoGastoGeneral>> getAllByEmpresa(@RequestParam Long empresaId) {
        return ResponseEntity.ok(service.findByEmpresaId(empresaId));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<PresupuestoGastoGeneral>> getByItemCalculadoraId(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.findByItemCalculadoraId(itemId));
    }

    @GetMapping("/item/{itemId}/empresa/{empresaId}")
    public ResponseEntity<List<PresupuestoGastoGeneral>> getByItemCalculadoraIdAndEmpresaId(@PathVariable Long itemId, @PathVariable Long empresaId) {
        return ResponseEntity.ok(service.findByItemCalculadoraIdAndEmpresaId(itemId, empresaId));
    }

    @PostMapping
    public ResponseEntity<PresupuestoGastoGeneral> create(@RequestBody PresupuestoGastoGeneral gastoGeneral) {
        return ResponseEntity.ok(service.save(gastoGeneral));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoGastoGeneral> update(@PathVariable Long id, @RequestBody PresupuestoGastoGeneral gastoGeneral) {
        PresupuestoGastoGeneral existing = service.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        // Actualizar campos del existente con los del DTO
        existing.setDescripcion(gastoGeneral.getDescripcion());
        existing.setCantidad(gastoGeneral.getCantidad());
        existing.setPrecioUnitario(gastoGeneral.getPrecioUnitario());
        existing.setSubtotal(gastoGeneral.getSubtotal());
        existing.setObservaciones(gastoGeneral.getObservaciones());
        existing.setSinCantidad(gastoGeneral.getSinCantidad());
        existing.setSinPrecio(gastoGeneral.getSinPrecio());
        existing.setOrden(gastoGeneral.getOrden());
        return ResponseEntity.ok(service.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Void> deleteByItem(@PathVariable Long itemId) {
        service.deleteByItemCalculadoraId(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/item/{itemId}/empresa/{empresaId}")
    public ResponseEntity<Void> deleteByItemAndEmpresa(@PathVariable Long itemId, @PathVariable Long empresaId) {
        service.deleteByItemCalculadoraIdAndEmpresaId(itemId, empresaId);
        return ResponseEntity.noContent().build();
    }
}
