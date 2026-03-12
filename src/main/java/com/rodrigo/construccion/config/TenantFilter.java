package com.rodrigo.construccion.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * TenantFilter - Filtro HTTP que intercepta requests y guarda el empresaId en TenantContext
 * 
 * Este filtro:
 * 1. Extrae el empresaId del parámetro query o header
 * 2. Lo guarda en TenantContext (ThreadLocal)
 * 3. El HibernateFilterInterceptor lo usará para activar el filtro de Hibernate
 */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    public TenantFilter() {
        System.out.println("✅✅✅ TenantFilter CREADO - Constructor ejecutado ✅✅✅");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    jakarta.servlet.FilterChain filterChain)
            throws jakarta.servlet.ServletException, java.io.IOException {
        
        // CORS PREFLIGHT: Dejar pasar OPTIONS sin procesar
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("🔄 CORS PREFLIGHT: OPTIONS " + request.getRequestURI() + " - pasando sin procesar");
            filterChain.doFilter(request, response);
            return;
        }
        
        // RUTAS PÚBLICAS: No requieren empresaId (autenticación)
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/")) {
            System.out.println("🔓 RUTA PÚBLICA: " + uri + " - pasando sin empresaId");
            filterChain.doFilter(request, response);
            return;
        }
        
        System.out.println("🔍 TenantFilter procesando: " + request.getMethod() + " " + request.getRequestURI());
        
        // Debug: mostrar headers relevantes
        System.out.println("📋 Header 'empresaId': " + request.getHeader("empresaId"));
        System.out.println("📋 Header 'X-Empresa-Id': " + request.getHeader("X-Empresa-Id"));
        System.out.println("📋 Header 'empresa-id': " + request.getHeader("empresa-id"));
        System.out.println("📋 Parámetro empresaId: " + request.getParameter("empresaId"));
        
        // Obtener empresaId del parámetro query o header (intentar varios nombres de header)
        String empresaIdParam = request.getParameter("empresaId");
        String empresaIdHeader = request.getHeader("empresaId");
        if (empresaIdHeader == null) empresaIdHeader = request.getHeader("X-Empresa-Id");
        if (empresaIdHeader == null) empresaIdHeader = request.getHeader("empresa-id");
        
        try {
            Long empresaId = null;
            
            // Prioridad: parámetro query > header
            if (empresaIdParam != null && !empresaIdParam.isEmpty()) {
                empresaId = Long.parseLong(empresaIdParam);
                System.out.println("✅ EmpresaId obtenido del parámetro query: " + empresaId);
            } else if (empresaIdHeader != null && !empresaIdHeader.isEmpty()) {
                empresaId = Long.parseLong(empresaIdHeader);
                System.out.println("✅ EmpresaId obtenido del header: " + empresaId);
            }
            
            if (empresaId != null) {
                // Guardar en contexto ThreadLocal
                TenantContext.setTenantId(empresaId);
                System.out.println("💾 TenantContext.setTenantId: empresaId=" + empresaId + " en " + request.getRequestURI());
            } else {
                System.out.println("⚠️ No se proporcionó empresaId en " + request.getRequestURI());
            }
            
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
            
        } catch (NumberFormatException e) {
            System.err.println("❌ Error parseando empresaId: " + e.getMessage());
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.err.println("❌ Error en TenantFilter: " + e.getMessage());
            e.printStackTrace();
            filterChain.doFilter(request, response);
        } finally {
            // CRÍTICO: Limpiar ThreadLocal para evitar memory leaks
            TenantContext.clear();
            System.out.println("🧹 TenantContext.clear() ejecutado");
        }
    }
}
