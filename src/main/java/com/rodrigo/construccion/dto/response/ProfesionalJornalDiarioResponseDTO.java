package com.rodrigo.construccion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para jornal diario de profesional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfesionalJornalDiarioResponseDTO {

    /**
     * ID del jornal
     */
    private Long id;

    /**
     * ID del profesional
     */
    private Long profesionalId;

    /**
     * Nombre del profesional
     */
    private String profesionalNombre;

    /**
     * Tipo de profesional (Oficial Albañil, Electricista, etc.)
     */
    private String tipoProfesional;

    /**
     * ID de la obra
     */
    private Long obraId;

    /**
     * Nombre de la obra
     */
    private String obraNombre;

    /**
     * ID del rubro del presupuesto
     */
    private Long rubroId;

    /**
     * Nombre del rubro (obtenido desde honorarios_por_rubro)
     */
    private String rubroNombre;

    /**
     * Fecha del trabajo
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    /**
     * Horas trabajadas como decimal
     * (1.0 = día completo, 0.5 = medio día, 0.25 = cuarto día)
     */
    private BigDecimal horasTrabajadasDecimal;

    /**
     * Tarifa diaria aplicada
     */
    private BigDecimal tarifaDiaria;

    /**
     * Monto cobrado (tarifaDiaria * horasTrabajadasDecimal)
     */
    private BigDecimal montoCobrado;

    /**
     * Observaciones
     */
    private String observaciones;

    /**
     * ID de la empresa
     */
    private Long empresaId;

    /**
     * Fecha de creación del registro
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    /**
     * Fecha de última actualización
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaActualizacion;

    /**
     * Mensaje informativo sobre la acción realizada
     * Ejemplo: "Jornal creado correctamente" o "Jornal actualizado - ya existía uno para esta fecha/obra/rubro"
     */
    private String mensajeAccion;
}
