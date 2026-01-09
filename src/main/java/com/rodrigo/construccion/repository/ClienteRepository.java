package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /* Buscar todos los clientes asociados a una empresa específica (muchos a muchos) */
    @Query("SELECT c FROM Cliente c JOIN c.empresas e WHERE e.id = :empresaId")
    List<Cliente> findByEmpresaId(@Param("empresaId") Long empresaId);

    /* Buscar clientes de una empresa con paginación */
    @Query("SELECT c FROM Cliente c JOIN c.empresas e WHERE e.id = :empresaId")
    Page<Cliente> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

    /* Buscar cliente por ID y empresa */
    @Query("SELECT c FROM Cliente c JOIN c.empresas e WHERE c.id = :id AND e.id = :empresaId")
    Optional<Cliente> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /* Buscar clientes por nombre (búsqueda parcial) dentro de una empresa */
    @Query("SELECT c FROM Cliente c JOIN c.empresas e WHERE e.id = :empresaId AND LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Cliente> findByEmpresaIdAndNombreContainingIgnoreCase(@Param("empresaId") Long empresaId,
                                                               @Param("nombre") String nombre,
                                                               Pageable pageable);

    /* Buscar cliente por CUIT/CUIL dentro de una empresa */
    @Query("SELECT c FROM Cliente c JOIN c.empresas e WHERE e.id = :empresaId AND c.cuitCuil = :cuitCuil")
    Optional<Cliente> findByEmpresaIdAndCuitCuil(@Param("empresaId") Long empresaId,
                                                 @Param("cuitCuil") String cuitCuil);

    /* Verificar si existe un cliente con CUIT/CUIL en una empresa */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c JOIN c.empresas e WHERE e.id = :empresaId AND c.cuitCuil = :cuitCuil")
    boolean existsByEmpresaIdAndCuitCuil(@Param("empresaId") Long empresaId, @Param("cuitCuil") String cuitCuil);

    /* ------------ CONSULTAS COMPLEJAS CON DATOS RELACIONADOS ------------ */

    /**
     * Obtener clientes con resumen de obras por empresa
     */
    @Query("SELECT c.id as clienteId, c.nombre as clienteNombre, c.cuitCuil, " +
            "COUNT(DISTINCT o) as totalObras, " +
            "COUNT(DISTINCT CASE WHEN o.estado = 'En obra' THEN o.id END) as obrasActivas, " +
            "COUNT(DISTINCT CASE WHEN o.estado = 'Finalizada' THEN o.id END) as obrasFinalizadas, " +
            "COALESCE(SUM(o.presupuestoEstimado), 0) as totalPresupuestado " +
            "FROM Cliente c " +
            "JOIN c.empresas e " +
            "LEFT JOIN c.obras o " +
            "WHERE e.id = :empresaId " +
            "GROUP BY c.id, c.nombre, c.cuitCuil " +
            "ORDER BY c.nombre")
    List<Object[]> findClientesConResumenObras(@Param("empresaId") Long empresaId);

    /* Buscar clientes con obras activas en una empresa */
    @Query("SELECT DISTINCT c FROM Cliente c " +
            "JOIN c.empresas e " +
            "JOIN c.obras o " +
            "WHERE e.id = :empresaId AND o.estado = 'En obra' " +
            "ORDER BY c.nombre")
    List<Cliente> findClientesConObrasActivas(@Param("empresaId") Long empresaId);

    /* Contar clientes por empresa */
    @Query("SELECT COUNT(c) FROM Cliente c JOIN c.empresas e WHERE e.id = :empresaId")
    long countByEmpresaId(@Param("empresaId") Long empresaId);

    /* ------------ MÉTODOS PARA BÚSQUEDA UNIVERSAL POR IDENTIFICADOR ------------  */

    /* Métodos de búsqueda global (sin filtrar por empresa) para soportar identificador universal cuando empresaId == null */
    @Query("SELECT c FROM Cliente c WHERE c.cuitCuil = :cuitCuil")
    Optional<Cliente> findByCuitCuil(@Param("cuitCuil") String cuitCuil);

    @Query("SELECT c FROM Cliente c JOIN c.empresas e WHERE e.id = :empresaId AND LOWER(c.cuitCuil) LIKE LOWER(CONCAT('%', :cuitCuil, '%'))")
    List<Cliente> findByEmpresaIdAndCuitCuilContainingIgnoreCase(@Param("empresaId") Long empresaId,
                                                                 @Param("cuitCuil") String cuitCuil);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.cuitCuil) LIKE LOWER(CONCAT('%', :cuitCuil, '%'))")
    List<Cliente> findByCuitCuilContainingIgnoreCase(@Param("cuitCuil") String cuitCuil);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Cliente> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /* Buscar cliente por nombre exacto (para cliente genérico)  */
    @Query("SELECT c FROM Cliente c WHERE c.nombre = :nombre")
    Optional<Cliente> findByNombre(@Param("nombre") String nombre);

    /* Buscar clientes por nombreSolicitante y telefono (para buscar desde presupuesto) */
    @Query("SELECT c FROM Cliente c WHERE c.nombreSolicitante = :nombreSolicitante AND c.telefono = :telefono")
    List<Cliente> findByNombreSolicitanteAndTelefono(@Param("nombreSolicitante") String nombreSolicitante,
                                                     @Param("telefono") String telefono);

    /* Buscar clientes por nombreSolicitante y email */
    @Query("SELECT c FROM Cliente c WHERE c.nombreSolicitante = :nombreSolicitante AND c.email = :email")
    List<Cliente> findByNombreSolicitanteAndEmail(@Param("nombreSolicitante") String nombreSolicitante,
                                                  @Param("email") String email);

    /* Buscar clientes por telefono */
    @Query("SELECT c FROM Cliente c WHERE c.telefono = :telefono")
    List<Cliente> findByTelefono(@Param("telefono") String telefono);

    /* Buscar cliente por email */
    @Query("SELECT c FROM Cliente c WHERE c.email = :email")
    Optional<Cliente> findByEmail(@Param("email") String email);
}