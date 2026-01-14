package com.rodrigo.construccion.service.impl;

import com.rodrigo.construccion.model.entity.PresupuestoGastoGeneral;
import com.rodrigo.construccion.repository.PresupuestoGastoGeneralRepository;
import com.rodrigo.construccion.service.IPresupuestoGastoGeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PresupuestoGastoGeneralServiceImpl implements IPresupuestoGastoGeneralService {

    @Autowired
    private PresupuestoGastoGeneralRepository repository;

    @Override
    public List<PresupuestoGastoGeneral> findByEmpresaId(Long empresaId) {
        return repository.findByEmpresaId(empresaId);
    }

    @Override
    public List<PresupuestoGastoGeneral> findByItemCalculadoraId(Long itemCalculadoraId) {
        return repository.findByItemCalculadoraIdOrderByOrden(itemCalculadoraId);
    }

    @Override
    public List<PresupuestoGastoGeneral> findByItemCalculadoraIdAndEmpresaId(Long itemCalculadoraId, Long empresaId) {
        return repository.findByItemCalculadoraIdAndEmpresaIdOrderByOrden(itemCalculadoraId, empresaId);
    }

    @Override
    public Long countByItemCalculadoraId(Long itemCalculadoraId) {
        return repository.countByItemCalculadoraId(itemCalculadoraId);
    }

    @Override
    public Double sumSubtotalByItemCalculadoraId(Long itemCalculadoraId) {
        return repository.sumSubtotalByItemCalculadoraId(itemCalculadoraId);
    }

    @Override
    @Transactional
    public int deleteByItemCalculadoraId(Long itemCalculadoraId) {
        return repository.deleteByItemCalculadoraId(itemCalculadoraId);
    }

    @Override
    @Transactional
    public int deleteByItemCalculadoraIdAndEmpresaId(Long itemCalculadoraId, Long empresaId) {
        return repository.deleteByItemCalculadoraIdAndEmpresaId(itemCalculadoraId, empresaId);
    }

    @Override
    public PresupuestoGastoGeneral save(PresupuestoGastoGeneral gastoGeneral) {
        return repository.save(gastoGeneral);
    }

    @Override
    public PresupuestoGastoGeneral findById(Long id) {
        Optional<PresupuestoGastoGeneral> opt = repository.findById(id);
        return opt.orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
