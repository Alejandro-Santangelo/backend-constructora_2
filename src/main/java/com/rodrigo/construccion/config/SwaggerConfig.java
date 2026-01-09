package com.rodrigo.construccion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI
 * 
 * Configura la documentación automática de las APIs REST del sistema.
 * Incluye información sobre autenticación, multi-tenancy y ejemplos de uso.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServers())
                .components(createComponents())
                .addSecurityItem(createSecurityRequirement());
    }

    private Info createApiInfo() {
        return new Info()
                .title("Sistema de Gestión Multi-Tenant - API REST")
                .description(createApiDescription())
                .version("1.0.0")
                .contact(createContact())
                .license(createLicense());
    }

    private String createApiDescription() {
        return """
                # Sistema de Gestión Multi-Tenant
                
                ## Descripción
                API REST para un sistema de gestión multi-tenant que soporta diferentes tipos de empresas:
                - **Constructora**: Gestión de obras, profesionales, materiales de construcción
                - **Mueblería**: Gestión de productos, diseñadores, materiales de muebles
                - **Seguros**: Gestión de pólizas, agentes, reclamos
                - **Y otros tipos de empresas**
                
                ## Características Multi-Tenant
                - Cada empresa es un **tenant independiente**
                - Los datos están completamente separados por empresa
                - Todas las operaciones requieren el header `X-Tenant-ID`
                
                ## Autenticación
                - Sistema de autenticación basado en JWT
                - Autorización por roles: admin, manager, user, viewer
                - Cada usuario pertenece a una empresa específica
                
                ## Consultas SQL Originales
                El sistema implementa todas las consultas SQL originales como endpoints REST:
                - `/clientes/{id}/datos-completos` - Equivalente a la consulta completa de cliente
                - `/obras/resumen-gastos` - Resumen financiero por obra
                - Y muchas más...
                
                ## Ejemplos de Uso
                
                ### 1. Obtener todos los datos de un cliente
                ```
                GET /api/clientes/1/datos-completos
                Headers:
                  X-Tenant-ID: 1
                  Authorization: Bearer {jwt-token}
                ```
                
                ### 2. Crear un nuevo cliente
                ```
                POST /api/clientes
                Headers:
                  X-Tenant-ID: 1
                  Authorization: Bearer {jwt-token}
                  Content-Type: application/json
                
                Body:
                {
                  "nombre": "Cliente Ejemplo S.A.",
                  "cuitCuil": "30-12345678-9",
                  "direccion": "Av. Ejemplo 123",
                  "telefono": "+5411234567",
                  "email": "contacto@ejemplo.com"
                }
                ```
                """;
    }

    private Contact createContact() {
        return new Contact()
                .name("Sistema Rodrigo")
                .email("soporte@sistema-rodrigo.com")
                .url("https://github.com/rodrigo/construccion-backend");
    }

    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    private List<Server> createServers() {
        return List.of(
                new Server()
                        .url("/")
                        .description("Servidor Actual (con context-path /api)")
        );
    }

    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", createSecurityScheme())
                .addHeaders("X-Tenant-ID", 
                        new io.swagger.v3.oas.models.headers.Header()
                                .description("ID de la empresa (tenant)")
                                .required(true)
                                .schema(new io.swagger.v3.oas.models.media.Schema<Long>().type("integer")));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token obtenido del endpoint de login");
    }

    private SecurityRequirement createSecurityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }
    
    @Bean
    public OperationCustomizer pageableParameterCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // Limpiar respuestas para mostrar solo la respuesta exitosa
            if (operation.getResponses() != null) {
                // Mantener solo las respuestas 200 y 201 (exitosas)
                operation.getResponses().keySet().removeIf(responseCode -> 
                    !responseCode.equals("200") && !responseCode.equals("201"));
            }
            
            if (operation.getParameters() != null) {
                operation.getParameters().forEach(parameter -> {
                    if ("sort".equals(parameter.getName())) {
                        // Para el endpoint de clientes, configurar valores válidos de sort
                        if (handlerMethod.getMethod().getDeclaringClass().getSimpleName().equals("ClienteController")) {
                            StringSchema schema = new StringSchema();
                            schema.setDefault("id");
                            schema.setEnum(List.of("id", "nombre", "cuitCuil", "direccion", "telefono", "email", "fechaCreacion"));
                            schema.setDescription("Campo por el cual ordenar. Valores válidos: id, nombre, cuitCuil, direccion, telefono, email, fechaCreacion");
                            parameter.setSchema(schema);
                        }
                    }
                });
            }
            return operation;
        };
    }
}