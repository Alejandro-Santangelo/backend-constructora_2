package com.rodrigo.construccion.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuración CORS usando filtro explícito con máxima prioridad
 * 
 * CRÍTICO: Este filtro debe ejecutarse ANTES que cualquier otro filtro
 * (incluido TenantFilter) para que las solicitudes OPTIONS (preflight)
 * reciban los headers CORS correctos ANTES de cualquier procesamiento.
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Orígenes permitidos
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:3002",
            "http://localhost:3003",
            "http://localhost:3004",
            "http://localhost:3005",
            "http://localhost:5173",
            "https://frontend-constructora2-production.up.railway.app",
            "https://zonal-curiosity-production-3041.up.railway.app"
        ));
        
        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Headers expuestos al cliente
        config.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"
        ));
        
        // Permitir credenciales (cookies, headers de autenticación)
        config.setAllowCredentials(true);
        
        // Cache preflight por 1 hora
        config.setMaxAge(3600L);
        
        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        // Crear filtro CORS
        CorsFilter corsFilter = new CorsFilter(source);
        
        // Registrar como FilterRegistrationBean con MÁXIMA PRIORIDAD
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(corsFilter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Se ejecuta ANTES que todos los demás filtros
        
        System.out.println("🌐 CORS Filter registrado con orden HIGHEST_PRECEDENCE");
        
        return bean;
    }
}
