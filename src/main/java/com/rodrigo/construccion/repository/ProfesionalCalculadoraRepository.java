package com.rodrigo.construccion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rodrigo.construccion.model.entity.ProfesionalCalculadora;

import java.util.List;

/**
 * Repositorio para entidad ProfesionalCalculadora.
 */
@Repository
public interface ProfesionalCalculadoraRepository extends JpaRepository<ProfesionalCalculadora, Long> {

    /**
     * Buscar profesionales por item de calculadora.
     */
    List<ProfesionalCalculadora> findByItemCalculadoraId(Long itemCalculadoraId);

    /**
     * Eliminar todos los profesionales de un item de calculadora específico.
     */
    @Modifying
    @Query("DELETE FROM ProfesionalCalculadora p WHERE p.itemCalculadora.id = :itemCalculadoraId")
    void deleteByItemCalculadoraId(@Param("itemCalculadoraId") Long itemCalculadoraId);
}