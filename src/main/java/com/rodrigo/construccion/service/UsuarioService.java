package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.Usuario;
import com.rodrigo.construccion.repository.UsuarioRepository;
import com.rodrigo.construccion.repository.EmpresaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para gestión de Usuarios
 * 
 * Maneja toda la lógica de negocio relacionada con usuarios del sistema.
 * Incluye validaciones multi-tenant y gestión de autenticación.
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    
    public UsuarioService(UsuarioRepository usuarioRepository, EmpresaRepository empresaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
    }

    /**
     * OPERACIONES CRUD
     */

    /**
     * Crear un nuevo usuario
     */
    public Usuario crearUsuario(Usuario usuario, Long empresaId) {
    System.out.println("Creando usuario: " + usuario.getEmail() + " para empresa: " + empresaId);

        // Validar que la empresa existe
        var empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        // Validaciones de negocio
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio");
        }

        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("El email del usuario es obligatorio");
        }

        // Verificar email único en la empresa
        if (usuarioRepository.existsByEmpresa_IdAndEmail(empresaId, usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email en la empresa");
        }

        // Verificar contraseña
        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        // Asignar empresa
        usuario.setEmpresa(empresa);

        // Establecer valores por defecto
        if (usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
            usuario.setRol("user");
        }

        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }

        // Sin encriptación de contraseña (Spring Security deshabilitado)
        // usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));

        var usuarioGuardado = usuarioRepository.save(usuario);
    System.out.println("Usuario creado exitosamente con ID: " + usuarioGuardado.getId());

        return usuarioGuardado;
    }

    /**
     * Obtener usuarios por empresa con paginación
     */
    @Transactional(readOnly = true)
    public Page<Usuario> obtenerPorEmpresaConPaginacion(Long empresaId, Pageable pageable) {
        return usuarioRepository.findByEmpresa_Id(empresaId, pageable);
    }

    /**
     * Obtener usuario por ID y empresa
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return usuarioRepository.findByIdAndEmpresa_Id(id, empresaId);
    }

    /**
     * Actualizar usuario
     */
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado, Long empresaId) {
    System.out.println("Actualizando usuario ID: " + id + " de empresa: " + empresaId);

        var usuarioExistente = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar email único si se está cambiando
        if (usuarioActualizado.getEmail() != null && 
            !usuarioActualizado.getEmail().equals(usuarioExistente.getEmail()) &&
            usuarioRepository.existsByEmpresa_IdAndEmail(empresaId, usuarioActualizado.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email en la empresa");
        }

        // Actualizar campos (sin contraseña)
        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }
        if (usuarioActualizado.getEmail() != null) {
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }
        if (usuarioActualizado.getRol() != null) {
            usuarioExistente.setRol(usuarioActualizado.getRol());
        }

        var usuarioGuardado = usuarioRepository.save(usuarioExistente);
    System.out.println("Usuario actualizado exitosamente: " + usuarioGuardado.getId());

        return usuarioGuardado;
    }

    /**
     * Eliminar usuario (soft delete)
     */
    public void eliminarUsuario(Long id, Long empresaId) {
    System.out.println("Eliminando usuario ID: " + id + " de empresa: " + empresaId);

        var usuario = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Soft delete - marcar como inactivo
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

    System.out.println("Usuario eliminado exitosamente: " + id);
    }

    /**
     * CONSULTAS ESPECIALIZADAS POR ROL
     */

    /**
     * Obtener usuarios por rol y empresa
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerPorRolYEmpresa(String rol, Long empresaId) {
        return usuarioRepository.findByEmpresa_IdAndRol(empresaId, rol);
    }

    /**
     * Obtener administradores de una empresa
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerAdministradores(Long empresaId) {
        return usuarioRepository.findAdministradoresByEmpresaId(empresaId);
    }

    /**
     * Obtener usuarios activos por empresa
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerActivosPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresa_IdAndActivoTrue(empresaId);
    }

    /**
     * Buscar usuarios por nombre
     */
    @Transactional(readOnly = true)
    public Page<Usuario> buscarPorNombre(Long empresaId, String nombre, Pageable pageable) {
        return usuarioRepository.findByEmpresa_IdAndNombreContainingIgnoreCase(empresaId, nombre, pageable);
    }

    /**
     * GESTIÓN DE CONTRASEÑAS Y AUTENTICACIÓN
     */

    /**
     * Cambiar contraseña
     */
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva, Long empresaId) {
    System.out.println("Cambiando contraseña para usuario ID: " + id + " de empresa: " + empresaId);

        var usuario = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar contraseña actual (sin encriptación - Spring Security deshabilitado)
        if (!passwordActual.equals(usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        // Validar nueva contraseña
        if (passwordNueva == null || passwordNueva.trim().length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        // Actualizar contraseña (sin encriptación)
        usuario.setPasswordHash(passwordNueva);
        usuarioRepository.save(usuario);

    System.out.println("Contraseña actualizada exitosamente para usuario: " + id);
    }

    /**
     * Cambiar estado de usuario
     */
    public void cambiarEstado(Long id, Boolean activo, Long empresaId) {
    System.out.println("Cambiando estado de usuario ID: " + id + " a " + activo + " - Empresa: " + empresaId);

        var usuario = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setActivo(activo);
        usuarioRepository.save(usuario);

    System.out.println("Estado del usuario actualizado exitosamente: " + id);
    }

    /**
     * Obtener usuario por email y empresa
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorEmailYEmpresa(String email, Long empresaId) {
        return usuarioRepository.findByEmailAndEmpresa_Id(email, empresaId);
    }

    /**
     * ESTADÍSTICAS Y MÉTRICAS
     */

    /**
     * Obtener estadísticas de usuarios
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas(Long empresaId) {
        var totalUsuarios = usuarioRepository.countByEmpresa_Id(empresaId);
        var usuariosActivos = usuarioRepository.countByEmpresa_IdAndActivoTrue(empresaId);
        var administradores = usuarioRepository.findAdministradoresByEmpresaId(empresaId).size();

        return Map.of(
                "totalUsuarios", totalUsuarios,
                "usuariosActivos", usuariosActivos,
                "usuariosInactivos", totalUsuarios - usuariosActivos,
                "administradores", administradores,
                "porcentajeActivos", totalUsuarios > 0 ? (usuariosActivos * 100.0 / totalUsuarios) : 0
        );
    }

    /**
     * Contar usuarios por empresa
     */
    @Transactional(readOnly = true)
    public long contarUsuariosPorEmpresa(Long empresaId) {
        return usuarioRepository.countByEmpresa_Id(empresaId);
    }

    /**
     * Contar usuarios activos por empresa
     */
    @Transactional(readOnly = true)
    public long contarUsuariosActivosPorEmpresa(Long empresaId) {
        return usuarioRepository.countByEmpresa_IdAndActivoTrue(empresaId);
    }

    /**
     * VALIDACIONES
     */

    /**
     * Verificar si existe un usuario en la empresa
     */
    @Transactional(readOnly = true)
    public boolean existeUsuarioEnEmpresa(Long usuarioId, Long empresaId) {
        return obtenerPorIdYEmpresa(usuarioId, empresaId).isPresent();
    }

    /**
     * Verificar si existe un email en la empresa
     */
    @Transactional(readOnly = true)
    public boolean existeEmailEnEmpresa(String email, Long empresaId) {
        return usuarioRepository.existsByEmpresa_IdAndEmail(empresaId, email);
    }

    /**
     * Autenticar usuario
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> autenticar(String email, String password, Long empresaId) {
        var usuario = obtenerPorEmailYEmpresa(email, empresaId);
        
        if (usuario.isPresent() && 
            usuario.get().getActivo() && 
            password.equals(usuario.get().getPasswordHash())) {
            return usuario;
        }
        
        return Optional.empty();
    }
}