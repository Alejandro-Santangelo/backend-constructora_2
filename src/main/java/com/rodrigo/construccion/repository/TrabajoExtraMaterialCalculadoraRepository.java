package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtraMaterialCalculadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para TrabajoExtraMaterialCalculadora.
 */
@Repository
public interface TrabajoExtraMaterialCalculadoraRepository extends JpaRepository<TrabajoExtraMaterialCalculadora, Long> {
    
    List<TrabajoExtraMaterialCalculadora> findByItemCalculadoraId(Long itemCalculadoraId);
    
    void deleteByItemCalculadoraId(Long itemCalculadoraId);
}
