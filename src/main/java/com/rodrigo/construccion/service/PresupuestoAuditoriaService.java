package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.PresupuestoAuditoria;
import com.rodrigo.construccion.repository.PresupuestoAuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PresupuestoAuditoriaService {
    private final PresupuestoAuditoriaRepository auditoriaRepository;

    @Autowired
    public PresupuestoAuditoriaService(PresupuestoAuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public PresupuestoAuditoria guardarAuditoria(PresupuestoAuditoria auditoria) {
        return auditoriaRepository.save(auditoria);
    }
}
