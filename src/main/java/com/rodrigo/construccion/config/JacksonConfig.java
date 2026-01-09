package com.rodrigo.construccion.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Configuración global de Jackson para el manejo consistente de fechas.
 * 
 * FORMATOS SOPORTADOS:
 * - LocalDate: "yyyy-MM-dd" (ej: 2025-12-10)
 * - LocalDateTime: "yyyy-MM-dd'T'HH:mm:ss" (ej: 2025-12-10T15:30:00)
 * 
 * COMPORTAMIENTO:
 * - ✅ Acepta fechas ISO-8601 del frontend
 * - ✅ Devuelve fechas en formato String (NO timestamps numéricos)
 * - ✅ Es flexible con zonas horarias
 * - ✅ Previene errores de deserialización
 */
@Configuration
public class JacksonConfig {

    // Formatos estándar para fechas
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // Crear módulo personalizado de JavaTime con formatos específicos
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Configurar serializadores (cómo se convierten a JSON)
        javaTimeModule.addSerializer(LocalDate.class, 
            new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalDateTime.class, 
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
        
        // Configurar deserializadores (cómo se leen desde JSON)
        javaTimeModule.addDeserializer(LocalDate.class, 
            new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, 
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
        
        // Construir ObjectMapper
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // Registrar módulo de Java 8 Time
        objectMapper.registerModule(javaTimeModule);
        
        // ========== CONFIGURACIONES GLOBALES ==========
        
        // Serialización: NO usar timestamps numéricos (usar strings)
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Deserialización: Ser flexible con fechas ISO-8601
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        
        // Deserialización: NO fallar si hay propiedades desconocidas en el JSON
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Deserialización: Aceptar arrays de un solo elemento como valores únicos
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        
        return objectMapper;
    }
}