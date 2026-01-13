package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Proveedor
 *
 * Contiene todos los datos del proveedor más información adicional como el nombre de la empresa.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información completa del proveedor para respuestas de la API")
public class ProveedorResponseDTO {

    @Schema(description = "ID único del proveedor", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del proveedor", example = "Constructora ABC S.A.")
    private String nombre;

    @Schema(description = "RUT del proveedor", example = "12345678-9")
    private String rut;

    @Schema(description = "Teléfono del proveedor", example = "+56 9 1234 5678")
    private String telefono;

    @Schema(description = "Email del proveedor", example = "contacto@proveedor.cl")
    private String email;

    @Schema(description = "Dirección del proveedor", example = "Av. Principal 1234, Oficina 501")
    private String direccion;

    @Schema(description = "Ciudad", example = "Santiago")
    private String ciudad;

    @Schema(description = "Región", example = "Metropolitana")
    private String region;

    @Schema(description = "Código postal", example = "8320000")
    private String codigoPostal;

    @Schema(description = "País", example = "Chile")
    private String pais;

    @Schema(description = "Nombre del contacto principal", example = "Juan Pérez")
    private String contactoPrincipal;

    @Schema(description = "Cargo del contacto", example = "Gerente de Ventas")
    private String cargoContacto;

    @Schema(description = "Teléfono del contacto", example = "+56 9 8765 4321")
    private String telefonoContacto;

    @Schema(description = "Email del contacto", example = "juan.perez@proveedor.cl")
    private String emailContacto;

    @Schema(description = "Sitio web del proveedor", example = "https://www.proveedor.cl")
    private String sitioWeb;

    @Schema(description = "Tipo de proveedor", example = "Materiales de Construcción")
    private String tipoProveedor;

    @Schema(description = "Categoría del proveedor", example = "Ferretería")
    private String categoria;

    @Schema(description = "Condiciones de pago", example = "30 días")
    private String condicionesPago;

    @Schema(description = "Límite de crédito", example = "5000000.00")
    private Double limiteCredito;

    @Schema(description = "Descuento máximo en porcentaje", example = "15.0")
    private Double descuentoMaximo;

    @Schema(description = "Plazo de entrega promedio en días", example = "7")
    private Integer plazoEntregaPromedio;

    @Schema(description = "Calificación del proveedor (0-5)", example = "4.5")
    private Double calificacion;

    @Schema(description = "Estado del proveedor", example = "ACTIVO")
    private String estado;

    @Schema(description = "Observaciones adicionales", example = "Proveedor confiable con entregas puntuales")
    private String observaciones;

    @Schema(description = "Fecha de registro del proveedor", example = "2025-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaRegistro;

    @Schema(description = "Fecha de la última compra realizada", example = "2025-12-20T14:20:00")
    private LocalDateTime fechaUltimaCompra;

    @Schema(description = "Número de cuenta bancaria", example = "1234567890")
    private String numeroCuentaBancaria;

    @Schema(description = "Banco", example = "Banco de Chile")
    private String banco;

    @Schema(description = "Tipo de cuenta", example = "Cuenta Corriente")
    private String tipoCuenta;

    @Schema(description = "Indica si el proveedor está activo", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha de creación del registro", example = "2025-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación", example = "2025-12-29T16:45:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaModificacion;

    @Schema(description = "ID de la empresa a la que pertenece el proveedor", example = "1")
    private Long empresaId;

    @Schema(description = "Nombre de la empresa a la que pertenece el proveedor", example = "Constructora XYZ S.A.")
    private String nombreEmpresa;
}
