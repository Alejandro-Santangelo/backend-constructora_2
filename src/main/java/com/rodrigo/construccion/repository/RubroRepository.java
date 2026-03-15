package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Rubro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Rubro
 */
@Repository
public interface RubroRepository extends JpaRepository<Rubro, Long> {

    /**
     * Buscar rubro por nombre exacto (case-insensitive)
     */
    Optional<Rubro> findByNombreIgnoreCase(String nombre);

    /**
     * Buscar rubros activos
     */
    List<Rubro> findByActivoTrue();

    /**
     * Buscar rubros por categoría
     */
    List<Rubro> findByCategoria(String categoria);

    /**
     * Buscar rubros activos por categoría
     */
    List<Rubro> findByCategoriaAndActivoTrue(String categoria);

    /**
     * Buscar rubros que contengan un texto en el nombre (case-insensitive)
     */
    @Query("SELECT r FROM Rubro r WHERE LOWER(r.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) AND r.activo = true")
    List<Rubro> buscarPorNombre(String texto);

    /**
     * Verificar si existe un rubro con un nombre específico
     */
    boolean existsByNombreIgnoreCase(String nombre);
}
