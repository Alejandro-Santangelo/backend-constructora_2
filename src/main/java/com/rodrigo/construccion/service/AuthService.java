package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.CambiarPinRequest;
import com.rodrigo.construccion.dto.request.LoginPinRequest;
import com.rodrigo.construccion.dto.response.LoginResponse;
import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.model.entity.Usuario;
import com.rodrigo.construccion.repository.EmpresaRepository;
import com.rodrigo.construccion.repository.UsuarioRepository;
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
        if (usuario.getEmpresaId() != null) {
            empresaRepository.findById(usuario.getEmpresaId())
                    .ifPresent(empresa -> response.setEmpresaNombre(empresa.getNombreEmpresa()));
        }

        // Obtener lista de empresas permitidas según rol
        List<LoginResponse.EmpresaPermitida> empresasPermitidas = new ArrayList<>();

        if ("SUPER_ADMIN".equals(usuario.getRol())) {
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
            log.info("🔒 Contratista detectado - acceso solo a empresa ID {}", usuario.getEmpresaId());
            final Long empresaId = usuario.getEmpresaId();
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
     */
    @Transactional
    public void cambiarPin(Long userId, CambiarPinRequest request) {
        log.info("🔐 Intentando cambiar PIN para usuario ID {}", userId);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar PIN actual
        if (!usuario.getPasswordHash().equals(request.getPinActual())) {
            log.warn("❌ PIN actual incorrecto para usuario ID {}", userId);
            throw new RuntimeException("PIN actual incorrecto");
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
        if ("SUPER_ADMIN".equals(usuario.getRol())) {
            return true;
        }

        // Contratista solo tiene acceso a su empresa
        return usuario.getEmpresaId() != null && usuario.getEmpresaId().equals(empresaId);
    }
}
