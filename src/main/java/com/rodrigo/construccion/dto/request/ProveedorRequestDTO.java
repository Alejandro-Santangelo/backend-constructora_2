package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear o actualizar un proveedor")
public class ProveedorRequestDTO {

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    @Schema(description = "Nombre del proveedor", example = "Constructora ABC S.A.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Size(max = 20, message = "El RUT no puede exceder 20 caracteres")
    @Schema(description = "RUT del proveedor", example = "12345678-9")
    private String rut;

    @Size(max = 50, message = "El teléfono no puede exceder 50 caracteres")
    @Schema(description = "Teléfono del proveedor", example = "+54 9 1234 5678")
    private String telefono;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Schema(description = "Email del proveedor", example = "contacto@proveedor.cl")
    private String email;

    @Size(max = 300, message = "La dirección no puede exceder 300 caracteres")
    @Schema(description = "Dirección del proveedor", example = "Av. Principal 1234, Oficina 501")
    private String direccion;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    @Schema(description = "Ciudad", example = "Santiago")
    private String ciudad;

    @Size(max = 100, message = "La región no puede exceder 100 caracteres")
    @Schema(description = "Región", example = "Metropolitana")
    private String region;

    @Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
    @Schema(description = "Código postal", example = "8320000")
    private String codigoPostal;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    @Schema(description = "País", example = "Chile")
    private String pais;

    @Size(max = 150, message = "El nombre del contacto principal no puede exceder 150 caracteres")
    @Schema(description = "Nombre del contacto principal", example = "Juan Pérez")
    private String contactoPrincipal;

    @Size(max = 100, message = "El cargo del contacto no puede exceder 100 caracteres")
    @Schema(description = "Cargo del contacto", example = "Gerente de Ventas")
    private String cargoContacto;

    @Size(max = 50, message = "El teléfono del contacto no puede exceder 50 caracteres")
    @Schema(description = "Teléfono del contacto", example = "+56 9 8765 4321")
    private String telefonoContacto;

    @Email(message = "El email del contacto debe tener un formato válido")
    @Size(max = 100, message = "El email del contacto no puede exceder 100 caracteres")
    @Schema(description = "Email del contacto", example = "juan.perez@proveedor.cl")
    private String emailContacto;

    @Size(max = 200, message = "El sitio web no puede exceder 200 caracteres")
    @Schema(description = "Sitio web del proveedor", example = "https://www.proveedor.cl")
    private String sitioWeb;

    @Size(max = 50, message = "El tipo de proveedor no puede exceder 50 caracteres")
    @Schema(description = "Tipo de proveedor", example = "Materiales de Construcción")
    private String tipoProveedor;

    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    @Schema(description = "Categoría del proveedor", example = "Ferretería")
    private String categoria;

    @Size(max = 100, message = "Las condiciones de pago no pueden exceder 100 caracteres")
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

    @Size(max = 5000, message = "Las observaciones no pueden exceder 5000 caracteres")
    @Schema(description = "Observaciones adicionales", example = "Proveedor confiable con entregas puntuales")
    private String observaciones;

    @Size(max = 50, message = "El número de cuenta bancaria no puede exceder 50 caracteres")
    @Schema(description = "Número de cuenta bancaria", example = "1234567890")
    private String numeroCuentaBancaria;

    @Size(max = 100, message = "El nombre del banco no puede exceder 100 caracteres")
    @Schema(description = "Banco", example = "Banco de Chile")
    private String banco;

    @Size(max = 50, message = "El tipo de cuenta no puede exceder 50 caracteres")
    @Schema(description = "Tipo de cuenta", example = "Cuenta Corriente")
    private String tipoCuenta;
}
