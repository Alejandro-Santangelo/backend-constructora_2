package com.rodrigo.construccion.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para establecer el empresaId en el TenantContext automáticamente
 * desde el parámetro de query "empresaId" en cada request.
 * 
 * Esto permite que el filtrado multi-tenant de Hibernate funcione correctamente
 * sin necesidad de establecer manualmente el TenantContext en cada controller.
 */
@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String empresaIdParam = request.getParameter("empresaId");
        
        if (empresaIdParam != null) {
            try {
                Long empresaId = Long.parseLong(empresaIdParam);
                TenantContext.setTenantId(empresaId);
                log.debug("🏢 TenantContext establecido: empresaId={} para URI={}", empresaId, request.getRequestURI());
            } catch (NumberFormatException e) {
                log.warn("⚠️ empresaId inválido: {} para URI={}", empresaIdParam, request.getRequestURI());
            }
        } else {
            log.debug("ℹ️ No se proporcionó empresaId para URI={}", request.getRequestURI());
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Limpiar el TenantContext después de completar el request para evitar memory leaks
        TenantContext.clear();
        log.debug("🧹 TenantContext limpiado para URI={}", request.getRequestURI());
    }
}
