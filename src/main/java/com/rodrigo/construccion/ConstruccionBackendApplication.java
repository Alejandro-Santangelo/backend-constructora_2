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
@EnableScheduling  // Habilita tareas programadas (@Scheduled)
public class ConstruccionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConstruccionBackendApplication.class, args);
        System.out.println("🚀 Backend Multi-Tenant iniciado correctamente!");
        System.out.println("📋 Swagger UI: http://localhost:8080/api/swagger-ui.html");
        System.out.println("🔗 API Docs: http://localhost:8080/api/api-docs");
        System.out.println("⏰ Tareas programadas: Actualización automática de estados de obras (07:00 AM diaria - Argentina)");
    }
}