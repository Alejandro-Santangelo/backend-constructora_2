package com.rodrigo.construccion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para especificar un tipo de profesional solicitado con cantidad
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Especificación de tipo de profesional solicitado")
public class SolicitudTipoProfesionalRequest {

        @Schema(description = "Tipo de profesional solicitado (opcional)", example = "Oficial Albañil", allowableValues = {
                        "Arquitecto", "Ingeniero Civil", "Oficial Albañil", "Ayudante Albañil",
                        "Carpintero", "Electricista", "Pintor", "Plomero", "Ceramista", "Cerrajero" })
        public String tipoProfesional;

        @Schema(description = "Cantidad de profesionales de este tipo (opcional, por defecto 1)", example = "2", minimum = "1")
        public Integer cantidad;

        @Schema(description = "Rol específico en obra (opcional)", example = "Capataz", allowableValues = {
                        "Director de Obra", "Jefe de Obra", "Capataz", "Oficial", "Medio Oficial", "Peón",
                        "Especialista", "Consultor" })
        public String rolEnObra;

        @Schema(description = "Valor por hora específico para este tipo (opcional)", example = "22000.00")
        public BigDecimal valorHoraSugerido;

        @Schema(description = "Observaciones específicas para este tipo de profesional (opcional)", example = "Con experiencia en construcción de viviendas")
        public String observaciones;

        @Schema(description = "Prioridad de asignación (opcional, por defecto MEDIA)", example = "ALTA", allowableValues = {
                        "BAJA", "MEDIA", "ALTA", "URGENTE" })
        public String prioridad;
}