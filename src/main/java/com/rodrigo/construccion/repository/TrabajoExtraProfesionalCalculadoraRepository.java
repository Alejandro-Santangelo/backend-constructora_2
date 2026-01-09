package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.TrabajoExtraProfesionalCalculadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para TrabajoExtraProfesionalCalculadora.
 */
@Repository
public interface TrabajoExtraProfesionalCalculadoraRepository extends JpaRepository<TrabajoExtraProfesionalCalculadora, Long> {
    
    List<TrabajoExtraProfesionalCalculadora> findByItemCalculadoraId(Long itemCalculadoraId);
    
    void deleteByItemCalculadoraId(Long itemCalculadoraId);
}
