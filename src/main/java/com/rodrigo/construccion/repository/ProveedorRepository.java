package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /* Buscar proveedor por ID y empresa */
    Optional<Proveedor> findByIdAndEmpresa_Id(Long id, Long empresaId);

    /* Obtener proveedores por empresa con paginación ordenados por nombre */
    Page<Proveedor> findByEmpresa_IdOrderByNombre(Long empresaId, Pageable pageable);

    /* Verificar si existe un proveedor con el RUT dado en la empresa  */
    boolean existsByRutAndEmpresa_Id(String rut, Long empresaId);

    /* Verificar si existe otro proveedor con el mismo RUT (para validación en actualización) */
    boolean existsByRutAndEmpresa_IdAndIdNot(String rut, Long empresaId, Long id);

    /* Buscar proveedores por nombre (búsqueda parcial) */
    List<Proveedor> findByNombreContainingIgnoreCaseAndEmpresa_Id(String nombre, Long empresaId);

    /* Buscar proveedores por ciudad */
    List<Proveedor> findByCiudadIgnoreCaseAndEmpresa_Id(String ciudad, Long empresaId);

    /* Buscar proveedores por región  */
    List<Proveedor> findByRegionIgnoreCaseAndEmpresa_Id(String region, Long empresaId);

    /* Obtener proveedores activos */
    List<Proveedor> findByActivoTrueAndEmpresa_IdOrderByNombre(Long empresaId);

    /* Contar proveedores por empresa */
    long countByEmpresa_Id(Long empresaId);

    /* Contar proveedores activos por empresa  */
    long countByActivoTrueAndEmpresa_Id(Long empresaId);

    /* Distribución de proveedores por ciudad */
    @Query("SELECT p.ciudad as ciudad, COUNT(p) as cantidad " +
            "FROM Proveedor p " +
            "WHERE p.empresa.id = :empresaId AND p.ciudad IS NOT NULL " +
            "GROUP BY p.ciudad " +
            "ORDER BY cantidad DESC")
    List<Map<String, Object>> countProveedoresPorCiudad(@Param("empresaId") Long empresaId);

    /* Distribución de proveedores por región */
    @Query("SELECT p.region as region, COUNT(p) as cantidad " +
            "FROM Proveedor p " +
            "WHERE p.empresa.id = :empresaId AND p.region IS NOT NULL " +
            "GROUP BY p.region " +
            "ORDER BY cantidad DESC")
    List<Map<String, Object>> countProveedoresPorRegion(@Param("empresaId") Long empresaId);

}