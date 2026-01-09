package com.rodrigo.construccion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rodrigo.construccion.model.entity.PresupuestoCostoInicial;

/**
 * Repositorio para la entidad PresupuestoCostoInicial.
 */
@Repository
public interface PresupuestoCostoInicialRepository extends JpaRepository<PresupuestoCostoInicial, Long> {
    
    /**
     * Busca el costo inicial asociado a un presupuesto específico.
     * 
     * @param presupuestoNoClienteId ID del presupuesto
     * @return Optional con el costo inicial si existe
     */
    Optional<PresupuestoCostoInicial> findByPresupuestoNoClienteId(Long presupuestoNoClienteId);
    
    /**
     * Elimina el costo inicial asociado a un presupuesto específico.
     * 
     * @param presupuestoNoClienteId ID del presupuesto
     */
    void deleteByPresupuestoNoClienteId(Long presupuestoNoClienteId);
    
    /**
     * Verifica si existe un costo inicial para un presupuesto específico.
     * 
     * @param presupuestoNoClienteId ID del presupuesto
     * @return true si existe, false en caso contrario
     */
    boolean existsByPresupuestoNoClienteId(Long presupuestoNoClienteId);
}
