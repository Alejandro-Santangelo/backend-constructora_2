package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.ItemCalculadoraPresupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar items de la calculadora de presupuestos.
 */
@Repository
public interface ItemCalculadoraPresupuestoRepository extends JpaRepository<ItemCalculadoraPresupuesto, Long> {
    
    /**
     * Encuentra todos los items de calculadora asociados a un presupuesto.
     * 
     * @param presupuestoNoClienteId ID del presupuesto
     * @return Lista de items de calculadora
     */
    List<ItemCalculadoraPresupuesto> findByPresupuestoNoClienteId(Long presupuestoNoClienteId);
    
    /**
     * Elimina todos los items de calculadora asociados a un presupuesto.
     * Usado al actualizar el presupuesto para reemplazar items existentes.
     * 
     * @param presupuestoNoClienteId ID del presupuesto
     */
    @Modifying
    @Query(value = "DELETE FROM items_calculadora_presupuesto WHERE presupuesto_no_cliente_id = :presupuestoNoClienteId", nativeQuery = true)
    void deleteByPresupuestoNoClienteId(@Param("presupuestoNoClienteId") Long presupuestoNoClienteId);
    
    /**
     * Cuenta los items de calculadora asociados a un presupuesto.
     * 
     * @param presupuestoNoClienteId ID del presupuesto
     * @return Cantidad de items
     */
    long countByPresupuestoNoClienteId(Long presupuestoNoClienteId);
}
