package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtraJornalCalculadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para TrabajoExtraJornalCalculadora.
 */
@Repository
public interface TrabajoExtraJornalCalculadoraRepository extends JpaRepository<TrabajoExtraJornalCalculadora, Long> {
    
    List<TrabajoExtraJornalCalculadora> findByItemCalculadoraId(Long itemCalculadoraId);
    
    void deleteByItemCalculadoraId(Long itemCalculadoraId);
}
