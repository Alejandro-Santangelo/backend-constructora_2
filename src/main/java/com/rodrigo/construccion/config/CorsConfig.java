package com.rodrigo.construccion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    
    @Value("${app.cors.allowed-origins:}")
    private String allowedOriginsFromProperties;
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Lista de origenes permitidos (desarrollo + producción)
                String[] allowedOrigins = new String[] {
                    "http://localhost:3000",
                    "http://localhost:3001",
                    "http://localhost:3002",
                    "http://localhost:3003",
                    "http://localhost:3004",
                    "http://localhost:3005",
                    "https://frontend-constructora2-production.up.railway.app"
                };
                
                // Si hay origenes adicionales en properties, agregarlos
                // (esto permite configurar más orígenes sin recompilar)
                if (allowedOriginsFromProperties != null && !allowedOriginsFromProperties.isEmpty()) {
                    String[] additionalOrigins = allowedOriginsFromProperties.split(",");
                    String[] allOrigins = new String[allowedOrigins.length + additionalOrigins.length];
                    System.arraycopy(allowedOrigins, 0, allOrigins, 0, allowedOrigins.length);
                    System.arraycopy(additionalOrigins, 0, allOrigins, allowedOrigins.length, additionalOrigins.length);
                    allowedOrigins = allOrigins;
                }
                
                System.out.println("🔧 CORS: Configurando orígenes permitidos:");
                for (String origin : allowedOrigins) {
                    System.out.println("   ✅ " + origin.trim());
                }
                
                registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
