package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.PresupuestoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PresupuestoAuditoriaRepository extends JpaRepository<PresupuestoAuditoria, Long> {
    // Métodos personalizados si son necesarios
}
