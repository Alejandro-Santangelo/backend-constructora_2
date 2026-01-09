package com.rodrigo.construccion.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para transferir datos de gastos generales individuales.
 * Corresponde exactamente a la estructura que envía el frontend.
 * 
 * Ejemplo de uso desde el frontend:
 * {
 *   "descripcion": "Alquiler de andamios",
 *   "cantidad": 2,
 *   "precioUnitario": 150000,
 *   "subtotal": 300000,
 *   "sinCantidad": false,
 *   "sinPrecio": false
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GastoGeneralDTO {

    @Schema(description = "ID del gasto general (solo en respuestas, ignorado en creación)", 
            example = "1")
    private Long id;

    @Schema(description = "Indica si es un item global del presupuesto híbrido", example = "false")
    private Boolean esGlobal = false;

    @Schema(description = "Descripción específica del gasto", 
            example = "Alquiler de andamios",
            required = true)
    @NotBlank(message = "La descripción del gasto general es obligatoria")
    private String descripcion;

    @Schema(description = "Observaciones adicionales sobre el gasto", 
            example = "Incluye montaje y desmontaje")
    private String observaciones;

    @Schema(description = "Cantidad de unidades del gasto", 
            example = "2",
            defaultValue = "1")
    @DecimalMin(value = "0.0", inclusive = false, message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidad = BigDecimal.ONE;

    @Schema(description = "Precio por unidad del gasto", 
            example = "150000",
            defaultValue = "0")
    @DecimalMin(value = "0.0", message = "El precio unitario debe ser mayor o igual a 0")
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @Schema(description = "Subtotal calculado (cantidad × precioUnitario)", 
            example = "300000",
            defaultValue = "0")
    @DecimalMin(value = "0.0", message = "El subtotal debe ser mayor o igual a 0")
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Schema(description = "Si es true, este gasto no maneja cantidad (precio fijo)", 
            example = "false",
            defaultValue = "false")
    @NotNull(message = "El campo sinCantidad es obligatorio")
    private Boolean sinCantidad = false;

    @Schema(description = "Si es true, este gasto no maneja precio unitario", 
            example = "false",
            defaultValue = "false")
    @NotNull(message = "El campo sinPrecio es obligatorio")
    private Boolean sinPrecio = false;

    @Schema(description = "Orden de visualización del gasto dentro del item", 
            example = "1",
            defaultValue = "1")
    @Min(value = 1, message = "El orden debe ser mayor a 0")
    private Integer orden = 1;

    /**
     * Constructor de conveniencia para casos simples.
     * 
     * @param descripcion Descripción del gasto
     * @param cantidad Cantidad de unidades
     * @param precioUnitario Precio por unidad
     */
    public GastoGeneralDTO(String descripcion, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.descripcion = descripcion;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.subtotal = this.cantidad.multiply(this.precioUnitario);
        this.sinCantidad = false;
        this.sinPrecio = false;
        this.orden = 1;
    }

    /**
     * Constructor para gastos con precio fijo (sin cantidad).
     * 
     * @param descripcion Descripción del gasto
     * @param precioFijo Precio fijo del gasto
     */
    public GastoGeneralDTO(String descripcion, BigDecimal precioFijo) {
        this.descripcion = descripcion;
        this.cantidad = BigDecimal.ONE; // Se ignora en cálculos
        this.precioUnitario = precioFijo != null ? precioFijo : BigDecimal.ZERO;
        this.subtotal = this.precioUnitario;
        this.sinCantidad = true; // No maneja cantidad
        this.sinPrecio = false;
        this.orden = 1;
    }

    /**
     * Calcula y establece el subtotal según las reglas de negocio.
     * Este método replica la lógica de la entidad para validación frontend.
     */
    public void calcularSubtotal() {
        if (Boolean.TRUE.equals(sinPrecio)) {
            // Si no maneja precio, mantener subtotal actual (valor manual)
            return;
        }
        
        if (Boolean.TRUE.equals(sinCantidad)) {
            // Si no maneja cantidad, subtotal = precio unitario
            this.subtotal = this.precioUnitario != null ? this.precioUnitario : BigDecimal.ZERO;
        } else {
            // Caso normal: subtotal = cantidad × precio unitario
            BigDecimal cantidadCalc = this.cantidad != null ? this.cantidad : BigDecimal.ZERO;
            BigDecimal precioCalc = this.precioUnitario != null ? this.precioUnitario : BigDecimal.ZERO;
            this.subtotal = cantidadCalc.multiply(precioCalc);
        }
    }

    /**
     * Valida la consistencia de los datos del DTO.
     * @throws IllegalArgumentException si los datos son inconsistentes
     */
    public void validar() {
        // Descripción obligatoria
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del gasto general es obligatoria");
        }

        // Validar coherencia de flags
        if (Boolean.TRUE.equals(sinCantidad) && Boolean.TRUE.equals(sinPrecio)) {
            throw new IllegalArgumentException("Un gasto no puede tener sinCantidad=true Y sinPrecio=true simultáneamente");
        }

        // Si no maneja cantidad, debe tener precio unitario
        if (Boolean.TRUE.equals(sinCantidad) && 
            (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Si sinCantidad=true, debe especificar precioUnitario > 0");
        }

        // Si maneja cantidad, validar que sea positiva
        if (Boolean.FALSE.equals(sinCantidad) && 
            (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Si sinCantidad=false, la cantidad debe ser > 0");
        }

        // Si maneja precio unitario, validar que sea positivo
        if (Boolean.FALSE.equals(sinPrecio) && 
            (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("Si sinPrecio=false, el precioUnitario debe ser > 0");
        }
    }

    /**
     * Configura valores por defecto para campos nulos.
     */
    public void configurarDefaults() {
        if (cantidad == null) cantidad = BigDecimal.ONE;
        if (precioUnitario == null) precioUnitario = BigDecimal.ZERO;
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (sinCantidad == null) sinCantidad = false;
        if (sinPrecio == null) sinPrecio = false;
        if (orden == null) orden = 1;
        
        // Calcular subtotal con los valores configurados
        calcularSubtotal();
    }

    @Override
    public String toString() {
        return String.format("GastoGeneralDTO{descripcion='%s', cantidad=%s, precioUnitario=%s, subtotal=%s, orden=%d}", 
            descripcion, cantidad, precioUnitario, subtotal, orden);
    }
}