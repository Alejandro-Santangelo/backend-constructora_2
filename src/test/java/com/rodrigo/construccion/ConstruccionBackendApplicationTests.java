package com.rodrigo.construccion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Prueba de integración completa de la aplicación
 * Verifica que el contexto de Spring se carga correctamente
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ConstruccionBackendApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Test
    void contextLoads() {
        // Esta prueba verifica que el contexto de Spring se carga sin errores
        // Es fundamental para detectar problemas de configuración
    }

    @Test
    void databaseConnectionWorks() {
        // Verifica que la conexión a la base de datos funciona
        assert postgres.isRunning();
        assert postgres.getJdbcUrl() != null;
    }
}