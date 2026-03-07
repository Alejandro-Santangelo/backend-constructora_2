package com.rodrigo.construccion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Aplicación principal del Sistema de Gestión Multi-Tenant
 * 
 * Soporta múltiples tipos de empresas:
 * - Constructora: Obras, Profesionales, Materiales de construcción
 * - Mueblería: Productos, Diseñadores, Materiales de muebles
 * - Seguros: Pólizas, Agentes, Reclamos
 * - Y otros tipos de empresas
 * 
 * @author Sistema Rodrigo
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
// @EnableScheduling  // DESACTIVADO temporalmente para Railway
public class ConstruccionBackendApplication {

    public static void main(String[] args) {
        System.out.println("🚀 ========================================");
        System.out.println("🚀 INICIANDO BACKEND MULTI-TENANT...");
        System.out.println("🚀 Java Version: " + System.getProperty("java.version"));
        System.out.println("🚀 Max Memory: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB");
        System.out.println("🚀 Port: " + System.getenv("PORT"));
        System.out.println("🚀 Profile: " + System.getenv("SPRING_PROFILES_ACTIVE"));
        System.out.println("🚀 ========================================");
        
        SpringApplication.run(ConstruccionBackendApplication.class, args);
        
        System.out.println("✅ ========================================");
        System.out.println("✅ BACKEND INICIADO EXITOSAMENTE!");
        System.out.println("✅ ========================================");
    }
}