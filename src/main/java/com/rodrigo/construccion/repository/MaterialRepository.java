package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad Material
 * NOTA: La tabla materiales NO tiene relacion directa con empresa.
 * Es una tabla de catalogo general.
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Consultas basicas
    List<Material> findByActivoTrue();
    
    Optional<Material> findByIdAndActivoTrue(Long id);
    
    List<Material> findByActivoTrueAndNombreContainingIgnoreCase(String nombre);
    
    @Query("SELECT m FROM Material m WHERE m.activo = true AND " +
           "(LOWER(m.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(m.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')))")
    Page<Material> findByTextoContaining(@Param("texto") String texto, Pageable pageable);

    List<Material> findByActivoTrueAndUnidadMedida(String unidadMedida);
    
    @Query("SELECT m FROM Material m WHERE m.activo = true " +
           "AND m.precioUnitario BETWEEN :precioMin AND :precioMax")
    List<Material> findByActivoTrueAndPrecioBetween(@Param("precioMin") BigDecimal precioMin,
                                                    @Param("precioMax") BigDecimal precioMax);

    long countByActivoTrue();
    
    @Query("SELECT AVG(m.precioUnitario) FROM Material m " +
           "WHERE m.activo = true AND m.precioUnitario IS NOT NULL")
    BigDecimal findPrecioPromedio();

    @Query("SELECT m FROM Material m WHERE m.activo = true ORDER BY m.nombre")
    List<Material> findAllActivosOrdenadosPorNombre();

    /**
     * Buscar materiales por empresa
     */
    List<Material> findByEmpresaIdAndActivoTrue(Long empresaId);

    /**
     * Verificar existencia por nombre y empresa
     */
    boolean existsByNombreIgnoreCaseAndEmpresaIdAndActivoTrue(String nombre, Long empresaId);
    
    // Método legacy para compatibilidad o uso global si aplica
    boolean existsByActivoTrueAndNombreIgnoreCase(String nombre);
    
    boolean existsByActivoTrueAndNombreIgnoreCaseAndIdNot(String nombre, Long materialId);
}