package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.PresupuestoGastoGeneral;
import java.util.List;

public interface IPresupuestoGastoGeneralService {
    List<PresupuestoGastoGeneral> findByEmpresaId(Long empresaId);
    List<PresupuestoGastoGeneral> findByItemCalculadoraId(Long itemCalculadoraId);
    List<PresupuestoGastoGeneral> findByItemCalculadoraIdAndEmpresaId(Long itemCalculadoraId, Long empresaId);
    Long countByItemCalculadoraId(Long itemCalculadoraId);
    Double sumSubtotalByItemCalculadoraId(Long itemCalculadoraId);
    int deleteByItemCalculadoraId(Long itemCalculadoraId);
    int deleteByItemCalculadoraIdAndEmpresaId(Long itemCalculadoraId, Long empresaId);
    PresupuestoGastoGeneral save(PresupuestoGastoGeneral gastoGeneral);
    PresupuestoGastoGeneral findById(Long id);
    void deleteById(Long id);
}
