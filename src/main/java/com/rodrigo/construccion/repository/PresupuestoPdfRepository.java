package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.PresupuestoPdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresupuestoPdfRepository extends JpaRepository<PresupuestoPdf, Long> {

    List<PresupuestoPdf> findByPresupuestoIdOrderByFechaGeneracionDesc(Long presupuestoId);
    
    void deleteByPresupuestoId(Long presupuestoId);
    
    int countByPresupuestoId(Long presupuestoId);
}
