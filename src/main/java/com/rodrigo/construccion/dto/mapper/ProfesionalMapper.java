package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.dto.request.ProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.DisponibilidadProfesionalResponse;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.model.entity.ProfesionalObra;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.*;

/**
 * Mapper para convertir entre DTOs y entidades de Profesional
 */
@Mapper(componentModel = "spring")
public interface ProfesionalMapper {

    /**
     * Convierte un ProfesionalRequestDTO a entidad Profesional
     * Los campos obrasAsignadas y honorarios se ignoran porque son relaciones
     * que se manejan por separado
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "obrasAsignadas", ignore = true)
    @Mapping(target = "honorarios", ignore = true)
    @Mapping(target = "importeGanancia", ignore = true)
    @Mapping(target = "tipoProfesionalEnum", ignore = true)
    @Mapping(target = "horas", ignore = true)
    @Mapping(target = "dias", ignore = true)
    @Mapping(target = "semanas", ignore = true)
    @Mapping(target = "meses", ignore = true)
    @Mapping(target = "honorarioHora", ignore = true)
    @Mapping(target = "honorarioDia", ignore = true)
    @Mapping(target = "honorarioSemana", ignore = true)
    @Mapping(target = "honorarioMes", ignore = true)
    Profesional toEntity(ProfesionalRequestDTO dto);

    /**
     * Actualiza una entidad Profesional existente con datos del DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "obrasAsignadas", ignore = true)
    @Mapping(target = "honorarios", ignore = true)
    @Mapping(target = "importeGanancia", ignore = true)
    @Mapping(target = "tipoProfesionalEnum", ignore = true)
    @Mapping(target = "horas", ignore = true)
    @Mapping(target = "dias", ignore = true)
    @Mapping(target = "semanas", ignore = true)
    @Mapping(target = "meses", ignore = true)
    @Mapping(target = "honorarioHora", ignore = true)
    @Mapping(target = "honorarioDia", ignore = true)
    @Mapping(target = "honorarioSemana", ignore = true)
    @Mapping(target = "honorarioMes", ignore = true)
    void updateEntity(ProfesionalRequestDTO requestDTO, @MappingTarget Profesional profesional);

    /**
     * Convierte una entidad Profesional a ProfesionalResponseDTO
     * Evita el bucle infinito al no incluir las relaciones bidireccionales
     */
    @Mapping(target = "cantidadObrasAsignadas", expression = "java(profesional.getObrasAsignadas() != null ? profesional.getObrasAsignadas().size() : 0)")
    @Mapping(target = "cantidadHonorarios", expression = "java(profesional.getHonorarios() != null ? profesional.getHonorarios().size() : 0)")
    @Mapping(target = "porcentajeGanancia", source = "porcentajeGanancia")
    @Mapping(target = "importeGanancia", source = "importeGanancia")
    @Mapping(target = "cuit", source = "cuit")
    @Mapping(target = "rolPersonalizado", source = "rolPersonalizado")
    @Mapping(target = "horas", source = "horas")
    @Mapping(target = "dias", source = "dias")
    @Mapping(target = "semanas", source = "semanas")
    @Mapping(target = "meses", source = "meses")
    @Mapping(target = "honorarioHora", source = "honorarioHora")
    @Mapping(target = "honorarioDia", source = "honorarioDia")
    @Mapping(target = "honorarioSemana", source = "honorarioSemana")
    @Mapping(target = "honorarioMes", source = "honorarioMes")
    @Mapping(target = "honorario", expression = "java(calcularHonorarioPreferencial(profesional))")
    ProfesionalResponseDTO toResponseDTO(Profesional profesional);

    /**
     * Calcula el honorario preferencial según prioridad:
     * 1. honorarioDia
     * 2. valorHoraDefault
     * 3. honorarioHora
     */
    default java.math.BigDecimal calcularHonorarioPreferencial(Profesional profesional) {
        if (profesional.getHonorarioDia() != null && profesional.getHonorarioDia().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return profesional.getHonorarioDia();
        }
        if (profesional.getValorHoraDefault() != null && profesional.getValorHoraDefault().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return profesional.getValorHoraDefault();
        }
        if (profesional.getHonorarioHora() != null && profesional.getHonorarioHora().compareTo(java.math.BigDecimal.ZERO) > 0) {
            return profesional.getHonorarioHora();
        }
        return java.math.BigDecimal.ZERO;
    }

    /**
     * Convierte una lista de entidades Profesional a una lista de
     * ProfesionalResponseDTO
     */
    List<ProfesionalResponseDTO> toResponseDTOList(List<Profesional> profesionales);

    /**
     * =================================================================
     * MÉTODOS PARA MAPEO A DisponibilidadProfesionalResponse
     * =================================================================
     */
    default DisponibilidadProfesionalResponse toDisponibilidadDto(Profesional profesional) {
        if (profesional == null) {
            return null;
        }

        DisponibilidadProfesionalResponse dto = new DisponibilidadProfesionalResponse();
        dto.setId(profesional.getId());
        dto.setNombre(profesional.getNombre());
        dto.setTipoProfesional(profesional.getTipoProfesional());
        dto.setEspecialidad(profesional.getEspecialidad());
        dto.setActivo(profesional.getActivo());

        List<ProfesionalObra> asignaciones = profesional.getObrasAsignadas();
        if (asignaciones == null || asignaciones.isEmpty()) {
            dto.setDisponible(true);
            dto.setEstado("DISPONIBLE");
            return dto;
        }

        Optional<ProfesionalObra> asignacionActivaOpt = asignaciones.stream()
                .filter(a -> a.getActivo() != null && a.getActivo())
                .findFirst();

        if (asignacionActivaOpt.isPresent()) {
            ProfesionalObra asignacionActiva = asignacionActivaOpt.get();
            dto.setDisponible(false);
            dto.setEstado("OCUPADO");
            dto.setObraActual(asignacionActiva.getId()); // Usar ID de la asignación
            dto.setNombreObraActual(asignacionActiva.getDireccionObraCompleta()); // Usar dirección completa
            dto.setFechaFinAsignacionActual(asignacionActiva.getFechaHasta());
        } else {
            dto.setDisponible(true);
            dto.setEstado("DISPONIBLE");
        }

        return dto;
    }

    List<DisponibilidadProfesionalResponse> toDisponibilidadDtoList(List<Profesional> profesionales);

}