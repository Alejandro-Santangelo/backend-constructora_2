package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.JornalCalculadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JornalCalculadoraRepository extends JpaRepository<JornalCalculadora, Long> {
    
    /**
     * Busca todos los jornales asociados a un item de calculadora específico
     */
    List<JornalCalculadora> findByItemCalculadoraId(Long itemCalculadoraId);
    
    /**
     * Elimina todos los jornales asociados a un item de calculadora específico
     */
    void deleteByItemCalculadoraId(Long itemCalculadoraId);
}

