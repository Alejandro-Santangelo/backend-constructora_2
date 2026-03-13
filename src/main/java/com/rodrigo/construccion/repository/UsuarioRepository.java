package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /* Buscar usuarios activos por empresa */
    List<Usuario> findByIdEmpresaAndActivoTrue(Long empresaId);

    /* Buscar usuarios por empresa con paginación */
    Page<Usuario> findByIdEmpresa(Long empresaId, Pageable pageable);

    /* Buscar usuario por ID y empresa  */
    Optional<Usuario> findByIdAndIdEmpresa(Long id, Long empresaId);

    /* Buscar usuarios por rol en una empresa */
    List<Usuario> findByIdEmpresaAndRol(Long empresaId, String rol);

    /* Buscar administradores de una empresa */
    @Query("SELECT u FROM Usuario u WHERE u.idEmpresa = :empresaId AND u.rol IN ('admin', 'manager') AND u.activo = true")
    List<Usuario> findAdministradoresByEmpresaId(@Param("empresaId") Long empresaId);

    /* Verificar si existe un email en una empresa */
    boolean existsByIdEmpresaAndEmail(Long empresaId, String email);

    /* Verificar si existe un PIN en el sistema (PIN único global) */
    boolean existsByPasswordHash(String pin);

    /* Buscar usuario por PIN (passwordHash) */
    Optional<Usuario> findByPasswordHash(String pin);

    /* Contar usuarios por empresa  */
    long countByIdEmpresa(Long empresaId);

    /* Contar usuarios activos por empresa */
    long countByIdEmpresaAndActivoTrue(Long empresaId);

    /* Buscar por email y empresa específica */
    Optional<Usuario> findByEmailAndIdEmpresa(String email, Long empresaId);

    /* Buscar por nombre con paginación */
    Page<Usuario> findByIdEmpresaAndNombreContainingIgnoreCase(Long empresaId, String nombre, Pageable pageable);
}