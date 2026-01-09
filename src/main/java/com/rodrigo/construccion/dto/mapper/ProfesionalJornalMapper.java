package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.dto.request.JornalRequestDTO;
import com.rodrigo.construccion.dto.response.ResumenPeriodoJornalDTO;
import com.rodrigo.construccion.dto.response.EstadisticasJornalResponseDTO;
import com.rodrigo.construccion.dto.response.JornalResumenDTO;
import com.rodrigo.construccion.dto.response.ProfesionalJornalResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenJornalesProfesionalDTO;
import com.rodrigo.construccion.model.entity.Jornal;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProfesionalJornalMapper {

    /**
     * Convierte un DTO de solicitud a una entidad Jornal.
     * Se ignoran los campos que no deben ser seteados desde el DTO, como el ID,
     * la fecha de creación y la relación de asignación.
     * La relación 'asignacion' debe ser manejada por el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "asignacion", ignore = true)
    Jornal toEntity(JornalRequestDTO requestDTO);

    /**
     * Actualiza una entidad Jornal existente a partir de un DTO.
     * Se ignoran los campos que no deben ser modificables, como el ID, la fecha de
     * creación y la asignación.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "asignacion", ignore = true)
    void updateEntityFromDto(JornalRequestDTO dto, @MappingTarget Jornal jornal);

    // Este método ya cumple la segunda parte de tu petición: convertir la entidad
    // guardada a un DTO de respuesta.
    @Mapping(source = "id", target = "idJornal")
    @Mapping(source = "fecha", target = "fecha")
    @Mapping(source = "horasTrabajadas", target = "horasTrabajadas")
    @Mapping(source = "valorHora", target = "valorHora")
    @Mapping(source = "observaciones", target = "observaciones")
    // Datos de la Obra (a través de la asignación - AHORA SOLO DIRECCIÓN)
    @Mapping(source = "asignacion.direccionObraCompleta", target = "nombreObra")
    // Datos de la Asignación
    @Mapping(source = "asignacion.rolEnObra", target = "rolEnObra")
    @Mapping(source = "asignacion.valorHoraAsignado", target = "valorHoraAsignado")
    // Datos del Profesional (a través de la asignación)
    @Mapping(source = "asignacion.profesional.nombre", target = "nombreProfesional")
    @Mapping(source = "asignacion.profesional.cuit", target = "cuitProfesional")
    @Mapping(source = "asignacion.profesional.email", target = "emailProfesional")
    @Mapping(source = "asignacion.profesional.telefono", target = "telefonoProfesional")
    @Mapping(source = "asignacion.profesional.especialidad", target = "especialidad")
    @Mapping(source = "asignacion.profesional.tipoProfesional", target = "tipoProfesional")
    @Mapping(source = "asignacion.profesional.honorarioHora", target = "honorarioHora")
    @Mapping(source = "asignacion.profesional.honorarioDia", target = "honorarioDia")
    @Mapping(source = "asignacion.profesional.honorarioSemana", target = "honorarioSemana")
    @Mapping(source = "asignacion.profesional.honorarioMes", target = "honorarioMes")
    @Mapping(source = "asignacion.profesional.valorHoraDefault", target = "valorHoraDefault")
    @Mapping(source = "asignacion.profesional.porcentajeGanancia", target = "porcentajeGanancia")
    @Mapping(source = "asignacion.profesional.importeGanancia", target = "importeGanancia")
    ProfesionalJornalResponseDTO toProfesionalJornalDTO(Jornal jornal);

    /**
     * Convierte una lista de Jornal a una lista de JornalResumenDTO.
     * MapStruct usará este método para poblar la lista de jornales en el resumen.
     */
    @Mapping(source = "id", target = "idJornal")
    JornalResumenDTO toJornalResumenDTO(Jornal jornal);

    @Named("toJornalResumenDTOList")
    List<JornalResumenDTO> toJornalResumenDTOList(List<Jornal> jornales);

    /**
     * Mapeo principal para convertir la asignación y su lista de jornales en el
     * DTO de resumen.
     * 
     * @param asignacion La entidad ProfesionalObra que actúa como clave de
     *                   agrupación.
     * @param jornales   La lista de jornales asociados a esa asignación.
     * @return Un DTO que resume la información.
     */
    @Mapping(source = "asignacion.profesional.id", target = "idProfesional")
    @Mapping(source = "asignacion.profesional.nombre", target = "nombreProfesional")
    @Mapping(source = "asignacion.profesional.tipoProfesional", target = "tipoProfesional")
    @Mapping(target = "idObra", ignore = true)
    @Mapping(source = "asignacion.direccionObraCompleta", target = "nombreObra")
    @Mapping(source = "asignacion.rolEnObra", target = "rolEnObra")
    @Mapping(source = "jornales", target = "jornales", qualifiedByName = "toJornalResumenDTOList")
    @Mapping(target = "cantidadJornales", expression = "java(jornales.size())")
    @Mapping(target = "totalHorasTrabajadas", expression = "java(jornales.stream().map(Jornal::getHorasTrabajadas).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))")
    @Mapping(target = "montoTotalJornales", expression = "java(jornales.stream().map(Jornal::getMontoTotal).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))")
    ResumenJornalesProfesionalDTO toResumenDTO(ProfesionalObra asignacion, List<Jornal> jornales);

    /**
     * Método principal que toma una lista plana de jornales y la transforma en una
     * lista de resúmenes agrupados por profesional y obra.
     * 
     * @param jornales La lista de jornales a procesar.
     * @return Una lista de DTOs agrupados.
     */
    default List<ResumenJornalesProfesionalDTO> toResumenDTOListFromJornales(List<Jornal> jornales) {
        if (jornales == null || jornales.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 1. Agrupamos los jornales por su 'asignacion' (la combinación
        // profesional-obra).
        Map<ProfesionalObra, List<Jornal>> jornalesAgrupados = jornales.stream()
                .collect(Collectors.groupingBy(Jornal::getAsignacion));

        // 2. Transformamos cada grupo en un DTO de resumen usando el otro método del
        // mapper.
        return jornalesAgrupados.entrySet().stream()
                .map(entry -> toResumenDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Convierte los datos calculados de estadísticas a su DTO correspondiente.
     * La fecha de consulta se genera automáticamente en el momento del mapeo.
     */
    @Mapping(target = "fechaConsulta", expression = "java(java.time.LocalDate.now())")
    EstadisticasJornalResponseDTO toEstadisticasDTO(
            long totalJornales,
            long jornalesMesActual,
            BigDecimal montoTotalMesActual,
            BigDecimal horasTotalMesActual);

    /**
     * Convierte los datos calculados de un período a su DTO correspondiente.
     */
    @Mapping(target = "periodo", source = "periodo")
    @Mapping(target = "cantidadJornales", source = "cantidadJornales")
    @Mapping(target = "montoTotal", source = "montoTotal")
    @Mapping(target = "horasTotal", source = "horasTotal")
    @Mapping(target = "valorPromedioHora", source = "valorPromedioHora")
    ResumenPeriodoJornalDTO toResumenPeriodoDTO(String periodo,
            long cantidadJornales,
            BigDecimal montoTotal,
            BigDecimal horasTotal,
            BigDecimal valorPromedioHora);
}
