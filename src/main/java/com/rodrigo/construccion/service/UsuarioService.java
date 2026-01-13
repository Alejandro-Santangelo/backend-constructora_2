package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.response.UsuarioEstadisticasDTO;
import com.rodrigo.construccion.exception.DuplicateEmailException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Usuario;
import com.rodrigo.construccion.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final IEmpresaService empresaService;

    /* Crear un nuevo usuario */
    public Usuario crearUsuario(Usuario usuario, Long empresaId) {
        // Validar que la empresa existe
        var empresa = empresaService.findEmpresaById(empresaId);

        // Verificar email único en la empresa
        if (usuarioRepository.existsByEmpresa_IdAndEmail(empresaId, usuario.getEmail())) {
            throw new DuplicateEmailException(usuario.getEmail(), empresa.getNombreEmpresa());
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

        return usuarioRepository.save(usuario);
    }

    /* Obtener usuarios por empresa con paginación  */
    @Transactional(readOnly = true)
    public Page<Usuario> obtenerPorEmpresaConPaginacion(Long empresaId, Pageable pageable) {
        return usuarioRepository.findByEmpresa_Id(empresaId, pageable);
    }

    /* Obtener usuario por ID y empresa */
    @Transactional(readOnly = true)
    public Usuario obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return usuarioRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    /* Actualizar usuario */
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado, Long empresaId) {

        var usuarioExistente = obtenerPorIdYEmpresa(id, empresaId);

        // Verificar email único si se está cambiando
        if (usuarioActualizado.getEmail() != null &&
                !usuarioActualizado.getEmail().equals(usuarioExistente.getEmail()) &&
                usuarioRepository.existsByEmpresa_IdAndEmail(empresaId, usuarioActualizado.getEmail())) {
            throw new DuplicateEmailException(usuarioActualizado.getEmail());
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

        return usuarioRepository.save(usuarioExistente);
    }

    /* Eliminar usuario (soft delete)  */
    public void eliminarUsuario(Long id, Long empresaId) {
        var usuario = obtenerPorIdYEmpresa(id, empresaId);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /* Obtener usuarios por rol y empresa  */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerPorRolYEmpresa(String rol, Long empresaId) {
        return usuarioRepository.findByEmpresa_IdAndRol(empresaId, rol);
    }

    /* Buscar usuarios por nombre */
    @Transactional(readOnly = true)
    public Page<Usuario> buscarPorNombre(Long empresaId, String nombre, Pageable pageable) {
        return usuarioRepository.findByEmpresa_IdAndNombreContainingIgnoreCase(empresaId, nombre, pageable);
    }

    /* GESTIÓN DE CONTRASEÑAS Y AUTENTICACIÓN */

    /* Cambiar contraseña */
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva, Long empresaId) {
        var usuario = obtenerPorIdYEmpresa(id, empresaId);

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
    }

    /* Cambiar estado de usuario */
    public void cambiarEstado(Long id, Boolean activo, Long empresaId) {
        var usuario = obtenerPorIdYEmpresa(id, empresaId);
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    /* Obtener usuario por email y empresa */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorEmailYEmpresa(String email, Long empresaId) {
        return usuarioRepository.findByEmailAndEmpresa_Id(email, empresaId);
    }

    /* ESTADÍSTICAS Y MÉTRICAS */

    /* Obtener estadísticas de usuarios */
    @Transactional(readOnly = true)
    public UsuarioEstadisticasDTO obtenerEstadisticas(Long empresaId) {
        long totalUsuarios = usuarioRepository.countByEmpresa_Id(empresaId);
        long usuariosActivos = usuarioRepository.countByEmpresa_IdAndActivoTrue(empresaId);
        int administradores = usuarioRepository.findAdministradoresByEmpresaId(empresaId).size();

        double porcentajeActivos = totalUsuarios > 0 ? (usuariosActivos * 100.0 / totalUsuarios) : 0.0;

        return UsuarioEstadisticasDTO.builder()
                .totalUsuarios(totalUsuarios)
                .usuariosActivos(usuariosActivos)
                .usuariosInactivos(totalUsuarios - usuariosActivos)
                .administradores(administradores)
                .porcentajeActivos(porcentajeActivos)
                .build();
    }

    /* MÉTODOS QUE DEBEN SER ELIMINADOS PORQUE NO ESTÁN SIENDO USADOS POR EL FRONTEND */

    /* Autenticar usuario */
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

    /* Obtener administradores de una empresa */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerAdministradores(Long empresaId) {
        return usuarioRepository.findAdministradoresByEmpresaId(empresaId);
    }

    /* Obtener usuarios activos por empresa */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerActivosPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresa_IdAndActivoTrue(empresaId);
    }

    /* Contar usuarios por empresa */
    @Transactional(readOnly = true)
    public long contarUsuariosPorEmpresa(Long empresaId) {
        return usuarioRepository.countByEmpresa_Id(empresaId);
    }

    /* Contar usuarios activos por empresa */
    @Transactional(readOnly = true)
    public long contarUsuariosActivosPorEmpresa(Long empresaId) {
        return usuarioRepository.countByEmpresa_IdAndActivoTrue(empresaId);
    }

    /* VALIDACIONES */

    /* Verificar si existe un usuario en la empresa */
    @Transactional(readOnly = true)
    public boolean existeUsuarioEnEmpresa(Long usuarioId, Long empresaId) {
        return usuarioRepository.findByIdAndEmpresa_Id(usuarioId, empresaId).isPresent();
    }

    /* Verificar si existe un email en la empresa     */
    @Transactional(readOnly = true)
    public boolean existeEmailEnEmpresa(String email, Long empresaId) {
        return usuarioRepository.existsByEmpresa_IdAndEmail(empresaId, email);
    }
}