package com.rodrigo.construccion;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Configuración base para todas las pruebas
 * Proporciona beans mock y configuraciones específicas para testing
 */
@TestConfiguration
@Profile("test")
@ActiveProfiles("test")
public class BaseTestConfiguration {
    
    /**
     * Clock fijo para pruebas consistentes con fechas
     */
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(Instant.parse("2025-10-03T10:00:00Z"), ZoneOffset.UTC);
    }
    
    /**
     * Configuración para datos de prueba
     */
    public static class TestConstants {
        public static final Long TEST_EMPRESA_ID = 1L;
        public static final String TEST_USUARIO_EMAIL = "test@test.com";
        public static final String TEST_JWT_TOKEN = "Bearer test-token";
    }
}