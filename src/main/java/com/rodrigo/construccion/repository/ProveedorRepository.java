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

/**
 * Repositorio para la entidad Proveedor
 * 
 * Proporciona operaciones de acceso a datos para proveedores con soporte Multi-Tenant.
 * Todas las consultas incluyen filtros por empresa para mantener la separación de datos.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    /**
     * CONSULTAS BÁSICAS
     */

    /**
     * Buscar proveedor por ID y empresa
     */
    Optional<Proveedor> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Obtener proveedores por empresa con paginación ordenados por nombre
     */
    Page<Proveedor> findByEmpresaIdOrderByNombre(Long empresaId, Pageable pageable);

    /**
     * Obtener todos los proveedores de una empresa
     */
    List<Proveedor> findByEmpresaIdOrderByNombre(Long empresaId);

    /**
     * VALIDACIONES
     */

    /**
     * Verificar si existe un proveedor con el RUT dado en la empresa
     */
    boolean existsByRutAndEmpresaId(String rut, Long empresaId);

    /**
     * Verificar si existe otro proveedor con el mismo RUT (para validación en actualización)
     */
    boolean existsByRutAndEmpresaIdAndIdNot(String rut, Long empresaId, Long id);

    /**
     * BÚSQUEDAS
     */

    /**
     * Buscar proveedores por nombre (búsqueda parcial)
     */
    List<Proveedor> findByNombreContainingIgnoreCaseAndEmpresaId(String nombre, Long empresaId);

    /**
     * Buscar proveedores por ciudad
     */
    List<Proveedor> findByCiudadIgnoreCaseAndEmpresaId(String ciudad, Long empresaId);

    /**
     * Buscar proveedores por región
     */
    List<Proveedor> findByRegionIgnoreCaseAndEmpresaId(String region, Long empresaId);

    /**
     * Buscar proveedores por tipo
     */
    List<Proveedor> findByTipoProveedorAndEmpresaId(String tipoProveedor, Long empresaId);

    /**
     * Buscar proveedores por categoría
     */
    List<Proveedor> findByCategoriaAndEmpresaId(String categoria, Long empresaId);

    /**
     * FILTROS POR ESTADO
     */

    /**
     * Obtener proveedores activos
     */
    List<Proveedor> findByActivoTrueAndEmpresaIdOrderByNombre(Long empresaId);

    /**
     * Obtener proveedores inactivos
     */
    List<Proveedor> findByActivoFalseAndEmpresaIdOrderByNombre(Long empresaId);

    /**
     * Obtener proveedores por estado
     */
    List<Proveedor> findByEstadoAndEmpresaId(String estado, Long empresaId);

    /**
     * CONTADORES
     */

    /**
     * Contar proveedores por empresa
     */
    long countByEmpresaId(Long empresaId);

    /**
     * Contar proveedores activos por empresa
     */
    long countByActivoTrueAndEmpresaId(Long empresaId);

    /**
     * Contar proveedores por tipo
     */
    long countByTipoProveedorAndEmpresaId(String tipoProveedor, Long empresaId);

    /**
     * CONSULTAS ESTADÍSTICAS
     */

    /**
     * Distribución de proveedores por ciudad
     */
    @Query("SELECT p.ciudad as ciudad, COUNT(p) as cantidad " +
           "FROM Proveedor p " +
           "WHERE p.empresaId = :empresaId AND p.ciudad IS NOT NULL " +
           "GROUP BY p.ciudad " +
           "ORDER BY cantidad DESC")
    List<Map<String, Object>> countProveedoresPorCiudad(@Param("empresaId") Long empresaId);

    /**
     * Distribución de proveedores por región
     */
    @Query("SELECT p.region as region, COUNT(p) as cantidad " +
           "FROM Proveedor p " +
           "WHERE p.empresaId = :empresaId AND p.region IS NOT NULL " +
           "GROUP BY p.region " +
           "ORDER BY cantidad DESC")
    List<Map<String, Object>> countProveedoresPorRegion(@Param("empresaId") Long empresaId);

    /**
     * Distribución de proveedores por tipo
     */
    @Query("SELECT p.tipoProveedor as tipo, COUNT(p) as cantidad " +
           "FROM Proveedor p " +
           "WHERE p.empresaId = :empresaId AND p.tipoProveedor IS NOT NULL " +
           "GROUP BY p.tipoProveedor " +
           "ORDER BY cantidad DESC")
    List<Map<String, Object>> countProveedoresPorTipo(@Param("empresaId") Long empresaId);

    /**
     * Proveedores con mejor calificación
     */
    @Query("SELECT p FROM Proveedor p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.calificacion IS NOT NULL " +
           "ORDER BY p.calificacion DESC")
    List<Proveedor> findProveedoresMejorCalificados(@Param("empresaId") Long empresaId, Pageable pageable);

    /**
     * Proveedores por rango de límite de crédito
     */
    @Query("SELECT p FROM Proveedor p " +
           "WHERE p.empresaId = :empresaId " +
           "AND p.limiteCredito BETWEEN :min AND :max " +
           "ORDER BY p.limiteCredito DESC")
    List<Proveedor> findByLimiteCreditoBetween(@Param("empresaId") Long empresaId, 
                                             @Param("min") Double min, 
                                             @Param("max") Double max);

    /**
     * Buscar proveedores por múltiples criterios
     */
    @Query("SELECT p FROM Proveedor p " +
           "WHERE p.empresaId = :empresaId " +
           "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:ciudad IS NULL OR LOWER(p.ciudad) = LOWER(:ciudad)) " +
           "AND (:region IS NULL OR LOWER(p.region) = LOWER(:region)) " +
           "AND (:tipoProveedor IS NULL OR p.tipoProveedor = :tipoProveedor) " +
           "AND (:activo IS NULL OR p.activo = :activo) " +
           "ORDER BY p.nombre")
    List<Proveedor> buscarProveedoresConFiltros(@Param("empresaId") Long empresaId,
                                               @Param("nombre") String nombre,
                                               @Param("ciudad") String ciudad,
                                               @Param("region") String region,
                                               @Param("tipoProveedor") String tipoProveedor,
                                               @Param("activo") Boolean activo);
}