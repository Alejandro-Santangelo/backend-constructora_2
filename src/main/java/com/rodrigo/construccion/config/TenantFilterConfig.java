package com.rodrigo.construccion.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuración del TenantFilter
 * 
 * Registra explícitamente el TenantFilter como FilterRegistrationBean
 * para asegurar que se ejecute DESPUÉS de CORS pero ANTES de Spring Security.
 * 
 * ORDEN DE FILTROS:
 * 1. CorsFilter (HIGHEST_PRECEDENCE = -2147483648)
 * 2. TenantFilter (HIGHEST_PRECEDENCE + 1 = -2147483647)
 * 3. Otros filtros...
 */
@Configuration
public class TenantFilterConfig {

    @Bean
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(TenantFilter tenantFilter) {
        System.out.println("🔧 Registrando TenantFilter con FilterRegistrationBean");
        
        FilterRegistrationBean<TenantFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(tenantFilter);
        registration.addUrlPatterns("/api/*"); // Solo para rutas de API
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1); // Justo DESPUÉS de CORS
        registration.setName("TenantFilter");
        
        System.out.println("✅ TenantFilter registrado con orden: " + (Ordered.HIGHEST_PRECEDENCE + 1) + " (después de CORS)");
        
        return registration;
    }
}
