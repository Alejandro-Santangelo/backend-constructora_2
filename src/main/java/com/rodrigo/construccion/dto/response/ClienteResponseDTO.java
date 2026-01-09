package com.rodrigo.construccion.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para Cliente
 * 
 * Contiene solo los datos que se exponen en las respuestas de la API.
 * Evita exponer información sensible o innecesaria como relaciones complejas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de cliente para respuestas de la API")
public class ClienteResponseDTO {

        @Schema(description = "ID único del cliente", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        private Long id_cliente;

        @Schema(description = "Nombre completo del cliente (razón social)", example = "Juan Pérez")
        private String nombre;

        @Schema(description = "Nombre del solicitante/contacto", example = "María García")
        private String nombreSolicitante;

        @Schema(description = "Dirección de correo electrónico", example = "juan.perez@email.com")
        private String email;

        @Schema(description = "Número de teléfono", example = "+54 11 1234-5678")
        private String telefono;

        @Schema(description = "CUIT/CUIL del cliente", example = "20-12345678-9")
        private String cuitCuil;

        @Schema(description = "Dirección física del cliente", example = "Av. Corrientes 1234, CABA")
        private String direccion;

        @Schema(description = "Fecha y hora de creación del registro", example = "2025-10-03T09:30:00", accessMode = AccessMode.READ_ONLY)
        private LocalDateTime fechaCreacion;

        @Schema(description = "Empresas asociadas al cliente")
        private List<EmpresaResponseDTO> empresas;
        
}