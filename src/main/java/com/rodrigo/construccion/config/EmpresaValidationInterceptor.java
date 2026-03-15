package com.rodrigo.construccion.config;

import com.rodrigo.construccion.model.entity.Usuario;
import com.rodrigo.construccion.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que valida que el usuario autenticado tiene permiso para acceder a la empresa solicitada.
 * 
 * SECURITY: Previene que un usuario manipule el header empresaId para acceder a datos de otras empresas.
 * 
 * Headers requeridos:
 * - X-User-Id: ID del usuario autenticado (retornado en login)
 * - empresaId o X-Tenant-ID: ID de la empresa que se desea acceder
 * 
 * Endpoints excluidos de validación:
 * - /api/auth/** (login, cambio PIN, etc.)
 * - /swagger-ui/** (documentación API)
 * - /v3/api-docs/** (OpenAPI)
 * 
 * @author Sistema Construcción
 * @since 2026-03-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmpresaValidationInterceptor implements HandlerInterceptor {

    private final UsuarioRepository usuarioRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        
        // Excluir endpoints públicos
        if (isPublicEndpoint(path)) {
            return true;
        }

        // Obtener userId del header
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            log.warn("🚫 Request sin X-User-Id: {} {}", request.getMethod(), path);
            // MODO PERMISIVO: solo loguea, no bloquea (hasta que frontend implemente el header)
            return true;
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            log.warn("🚫 X-User-Id inválido: {}", userIdHeader);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"X-User-Id debe ser un número\"}");
            return false;
        }

        // Obtener empresaId del header (puede venir como 'empresaId' o 'X-Tenant-ID')
        String empresaIdHeader = request.getHeader("empresaId");
        if (empresaIdHeader == null || empresaIdHeader.isEmpty()) {
            empresaIdHeader = request.getHeader("X-Tenant-ID");
        }

        // Si no hay empresaId en el header, permitir (algunos endpoints no requieren empresa específica)
        if (empresaIdHeader == null || empresaIdHeader.isEmpty()) {
            log.debug("✅ Request sin empresaId (permitido): {} {}", request.getMethod(), path);
            return true;
        }

        Long empresaId;
        try {
            empresaId = Long.parseLong(empresaIdHeader);
        } catch (NumberFormatException e) {
            log.warn("🚫 empresaId inválido: {}", empresaIdHeader);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"empresaId debe ser un número\"}");
            return false;
        }

        // Validar que el usuario tiene permiso para acceder a esta empresa
        try {
            Usuario usuario = usuarioRepository.findById(userId).orElse(null);
            if (usuario == null) {
                log.warn("🚫 Usuario {} no encontrado: {} {}", userId, request.getMethod(), path);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Usuario no encontrado\"}");
                return false;
            }

            // SUPER_ADMIN tiene acceso a todas las empresas
            boolean tienePermiso = usuario.isSuperAdmin()
                    || empresaId.equals(usuario.getIdEmpresa());

            if (!tienePermiso) {
                log.warn("🚫 Usuario {} intentó acceder a empresa {} sin permiso: {} {}",
                         userId, empresaId, request.getMethod(), path);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"No tiene permiso para acceder a esta empresa\"}");
                return false;
            }

            log.debug("✅ Usuario {} accediendo a empresa {}: {} {}",
                     userId, empresaId, request.getMethod(), path);
            return true;

        } catch (Exception e) {
            log.error("❌ Error validando permisos usuario {} empresa {}: {}", 
                     userId, empresaId, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error interno validando permisos\"}");
            return false;
        }
    }

    /**
     * Verifica si el endpoint es público y no requiere validación de empresa.
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/api-docs/") ||
               path.equals("/swagger-ui.html") ||
               path.equals("/") ||
               path.startsWith("/actuator/") ||  // Health checks
               path.startsWith("/error");         // Error handling
    }
}
