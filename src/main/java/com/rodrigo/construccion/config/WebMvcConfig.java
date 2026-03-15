package com.rodrigo.construccion.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración personalizada de Spring MVC
 * 
 * CRÍTICO: Configura el orden de los ResourceHandlers para evitar que
 * el patrón /** intercepte las rutas de los controllers REST.
 * 
 * También registra el TenantInterceptor para establecer automáticamente
 * el empresaId en el TenantContext desde el query param.
 * 
 * SEGURIDAD: Registra EmpresaValidationInterceptor para validar que el usuario
 * tenga permiso para acceder a la empresa solicitada (multi-tenant security).
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final EmpresaValidationInterceptor empresaValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // SEGURIDAD: Registrar EmpresaValidationInterceptor PRIMERO
        // Valida que el usuario tenga permiso para acceder a la empresa solicitada
        registry.addInterceptor(empresaValidationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",       // Excluir autenticación
                        "/swagger-ui/**",     // Excluir Swagger UI
                        "/v3/api-docs/**",    // Excluir OpenAPI docs
                        "/api-docs/**"        // Excluir API docs
                );
        
        // Registrar TenantInterceptor para establecer empresaId en contexto
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configurar recursos estáticos CON ORDEN ESPECÍFICO
        // Order = 1 (alta prioridad, se evalúa DESPUÉS de los controllers)
        
        registry
            .addResourceHandler("/**")
            .addResourceLocations(
                "classpath:/META-INF/resources/",
                "classpath:/resources/",
                "classpath:/static/",
                "classpath:/public/"
            )
            .resourceChain(true);
        
        // Swagger UI tiene su propio handler interno, no lo tocamos
    }
}
