package com.rodrigo.construccion.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;

/**
 * Configuración de Base de Datos
 * 
 * Configura la conexión a PostgreSQL y características JPA.
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * Configuración del DataSource principal
     */
    @Bean
    @Primary
    public DataSource dataSource() {
    System.out.println("💾 Configurando conexión a base de datos PostgreSQL");
    System.out.println("🔗 URL: " + datasourceUrl);
    System.out.println("👤 Usuario: " + datasourceUsername);
        
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(datasourceUrl)
                .username(datasourceUsername)
                .password(datasourcePassword)
                .build();
    }

    /**
     * Configuración adicional para el contexto de aplicación
     */
    @Bean
    public String databaseInfo() {
        return String.format("Conectado a: %s con usuario: %s", datasourceUrl, datasourceUsername);
    }
}