package com.rodrigo.service;

import com.rodrigo.dto.CambiarPinRequest;
import com.rodrigo.dto.LoginPinRequest;
import com.rodrigo.dto.LoginResponse;
import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.model.Usuario;
import com.rodrigo.construccion.repository.EmpresaRepository;
import com.rodrigo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;

    /**
     * Login con PIN de 4 dígitos
     * @param request PIN ingresado
     * @return Información del usuario y sus permisos
     */
    @Transactional(readOnly = true)
    public LoginResponse loginConPin(LoginPinRequest request) {
        log.info("🔐 Intentando login con PIN");

        // Buscar usuario por PIN (guardado en password_hash temporalmente)
        Optional<Usuario> usuarioOpt = usuarioRepository.findAll().stream()
                .filter(u -> u.getPasswordHash().equals(request.getPin()))
                .filter(Usuario::getActivo)
                .findFirst();

        if (usuarioOpt.isEmpty()) {
            log.warn("❌ PIN incorrecto o usuario inactivo");
            throw new RuntimeException("PIN incorrecto");
        }

        Usuario usuario = usuarioOpt.get();
        log.info("✅ Usuario autenticado: {} (Rol: {})", usuario.getNombre(), usuario.getRol());

        // Construir respuesta
        LoginResponse response = LoginResponse.fromUsuario(usuario);

        // Obtener nombre de la empresa del usuario
        if (usuario.getIdEmpresa() != null) {
            empresaRepository.findById(usuario.getIdEmpresa())
                    .ifPresent(empresa -> response.setEmpresaNombre(empresa.getNombreEmpresa()));
        }

        // Obtener lista de empresas permitidas según rol
        List<LoginResponse.EmpresaPermitida> empresasPermitidas = new ArrayList<>();

        if (usuario.getRol() == Usuario.RolUsuario.SUPER_ADMIN) {
            // Super admin puede ver TODAS las empresas
            log.info("🔓 Super Admin detectado - acceso a todas las empresas");
            empresasPermitidas = empresaRepository.findAll().stream()
                    .map(e -> new LoginResponse.EmpresaPermitida(
                            e.getId(),
                            e.getNombreEmpresa(),
                            e.getCuit()
                    ))
                    .collect(Collectors.toList());
        } else {
            // Contratista solo puede ver SU empresa
            log.info("🔒 Contratista detectado - acceso solo a empresa ID {}", usuario.getIdEmpresa());
            final Long empresaId = usuario.getIdEmpresa();
            if (empresaId != null) {
                Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
                if (empresaOpt.isPresent()) {
                    Empresa empresa = empresaOpt.get();
                    empresasPermitidas.add(
                            new LoginResponse.EmpresaPermitida(
                                    empresa.getId(),
                                    empresa.getNombreEmpresa(),
                                    empresa.getCuit()
                            )
                    );
                }
            }
        }

        response.setEmpresasPermitidas(empresasPermitidas);
        log.info("📋 Empresas permitidas: {}", empresasPermitidas.size());

        return response;
    }

    /**
     * Cambiar PIN de un usuario
     * @param userId ID del usuario
     * @param request PIN actual y nuevo
     * @param isSuperAdmin Si es superadmin, omite validación del PIN actual
     */
    @Transactional
    public void cambiarPin(Long userId, CambiarPinRequest request, boolean isSuperAdmin) {
        log.info("🔐 Intentando cambiar PIN para usuario ID {} (Super Admin: {})", userId, isSuperAdmin);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar PIN actual SOLO si NO es super admin
        if (!isSuperAdmin) {
            if (!usuario.getPasswordHash().equals(request.getPinActual())) {
                log.warn("❌ PIN actual incorrecto para usuario ID {}", userId);
                throw new RuntimeException("PIN actual incorrecto");
            }
        } else {
            log.info("🔓 Super Admin detectado - omitiendo validación de PIN actual");
        }

        // Validar nuevo PIN (4 dígitos)
        if (!request.getPinNuevo().matches("\\d{4}")) {
            log.warn("❌ PIN nuevo inválido (debe ser 4 dígitos)");
            throw new RuntimeException("El PIN debe tener 4 dígitos numéricos");
        }

        // Actualizar PIN
        usuario.setPasswordHash(request.getPinNuevo());
        usuarioRepository.save(usuario);

        log.info("✅ PIN actualizado exitosamente para usuario ID {}", userId);
    }

    /**
     * Verificar si un usuario tiene permiso para acceder a una empresa
     * @param userId ID del usuario
     * @param empresaId ID de la empresa
     * @return true si tiene permiso
     */
    @Transactional(readOnly = true)
    public boolean tienePermisoEmpresa(Long userId, Long empresaId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Super admin tiene acceso a todas las empresas
        if (usuario.getRol() == Usuario.RolUsuario.SUPER_ADMIN) {
            return true;
        }

        // Contratista solo tiene acceso a su empresa
        return usuario.getIdEmpresa().equals(empresaId);
    }
}
