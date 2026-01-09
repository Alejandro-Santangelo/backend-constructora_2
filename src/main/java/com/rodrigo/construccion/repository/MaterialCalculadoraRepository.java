package com.rodrigo.construccion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rodrigo.construccion.model.entity.MaterialCalculadora;

import java.util.List;

/**
 * Repositorio para entidad MaterialCalculadora.
 */
@Repository
public interface MaterialCalculadoraRepository extends JpaRepository<MaterialCalculadora, Long> {

    /**
     * Buscar materiales por item de calculadora.
     */
    List<MaterialCalculadora> findByItemCalculadoraId(Long itemCalculadoraId);

    /**
     * Eliminar todos los materiales de un item de calculadora específico.
     */
    @Modifying
    @Query("DELETE FROM MaterialCalculadora m WHERE m.itemCalculadora.id = :itemCalculadoraId")
    void deleteByItemCalculadoraId(@Param("itemCalculadoraId") Long itemCalculadoraId);
}