package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.dto.request.HonorarioRequestDTO;
import com.rodrigo.construccion.dto.response.EstadisticasHonorarioResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenPeriodoHonorarioDTO;
import com.rodrigo.construccion.dto.response.HonorarioProfesionalObraResponseDTO;
import com.rodrigo.construccion.dto.response.HonorarioSimpleDTO;
import com.rodrigo.construccion.dto.response.ResumenHonorariosProfesionalDTO;
import com.rodrigo.construccion.model.entity.Honorario;
import com.rodrigo.construccion.model.entity.Profesional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface HonorarioMapper {

    /**
     * Método principal que convierte una lista plana de honorarios en una lista de resúmenes agrupados por profesional.
     * Utiliza un método default para implementar la lógica de agrupación.
     *
     * @param honorarios Lista de entidades Honorario a procesar.
     * @return Una lista de DTOs ResumenHonorariosProfesionalDTO, cada uno representando a un profesional y sus honorarios.
     */
    default List<ResumenHonorariosProfesionalDTO> toResumenHonorariosProfesionalDTOList(List<Honorario> honorarios) {
        if (honorarios == null || honorarios.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Agrupar los honorarios por profesional.
        Map<Profesional, List<Honorario>> honorariosPorProfesional = honorarios.stream()
                .filter(h -> h.getProfesional() != null) // Asegurarse de que el honorario tiene un profesional asociado
                .collect(Collectors.groupingBy(Honorario::getProfesional));

        // 2. Transformar cada grupo (cada entrada del mapa) en un DTO de resumen.
        return honorariosPorProfesional.entrySet().stream()
                .map(entry -> toResumenHonorariosProfesionalDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Método auxiliar para convertir un Profesional y su lista de honorarios en un DTO de resumen.
     * MapStruct utilizará este método para realizar el mapeo.
     */
    @Mapping(source = "profesional.id", target = "idProfesional")
    @Mapping(source = "profesional.nombre", target = "nombreProfesional")
    @Mapping(source = "profesional.tipoProfesional", target = "tipoProfesional")
    @Mapping(source = "profesional.cuit", target = "cuitProfesional")
    @Mapping(source = "honorarios", target = "honorarios", qualifiedByName = "toHonorarioSimpleDTOList") // Mapea la lista de honorarios
    @Mapping(target = "cantidadHonorarios", expression = "java(honorarios.size())") // Calcula la cantidad
    @Mapping(target = "montoTotalPagado", expression = "java(honorarios.stream().map(Honorario::getMonto).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add))") // Calcula el total
    ResumenHonorariosProfesionalDTO toResumenHonorariosProfesionalDTO(Profesional profesional, List<Honorario> honorarios);

    /**
     * Convierte una entidad Honorario a su DTO simple. MapStruct implementará esto.
     */
    @Mapping(source = "id", target = "idHonorario")
    @Mapping(source = "obra.nombre", target = "nombreObra")
    HonorarioSimpleDTO toHonorarioSimpleDTO(Honorario honorario);

    /**
     * Convierte una lista de entidades Honorario a una lista de DTOs simples. MapStruct implementará esto.
     */
    @Named("toHonorarioSimpleDTOList")
    List<HonorarioSimpleDTO> toHonorarioSimpleDTOList(List<Honorario> honorarios);

    /**
     * Convierte una entidad Honorario a un DTO detallado con información del profesional y la obra.
     * MapStruct implementará este método basándose en las anotaciones.
     *
     * @param honorario La entidad Honorario a convertir.
     * @return Un DTO con la información combinada.
     */
    @Mapping(source = "id", target = "idHonorario")
    @Mapping(source = "profesional.nombre", target = "nombreProfesional")
    @Mapping(source = "profesional.tipoProfesional", target = "tipoProfesional")
    @Mapping(source = "profesional.especialidad", target = "especialidadProfesional")
    @Mapping(source = "profesional.activo", target = "activoProfesional")
    @Mapping(source = "profesional.cuit", target = "cuitProfesional")
    @Mapping(source = "profesional.horas", target = "horas")
    @Mapping(source = "profesional.dias", target = "dias")
    @Mapping(source = "profesional.semanas", target = "semanas")
    @Mapping(source = "profesional.meses", target = "meses")
    @Mapping(source = "profesional.honorarioHora", target = "honorarioHora")
    @Mapping(source = "profesional.honorarioDia", target = "honorarioDia")
    @Mapping(source = "profesional.honorarioSemana", target = "honorarioSemana")
    @Mapping(source = "profesional.honorarioMes", target = "honorarioMes")
    @Mapping(source = "profesional.valorHoraDefault", target = "valorHoraDefault")
    @Mapping(source = "profesional.porcentajeGanancia", target = "porcentajeGanancia")
    @Mapping(source = "profesional.importeGanancia", target = "importeGanancia")
    @Mapping(source = "obra.nombre", target = "nombreObra")
    @Mapping(target = "rolEnObra", ignore = true) // El rol está en ProfesionalObra, no en Honorario
    @Mapping(target = "valorHoraAsignado", ignore = true) // El valor/hora está en ProfesionalObra
    HonorarioProfesionalObraResponseDTO toHonorarioProfesionalObraResponseDTO(Honorario honorario);

    /**
     * Convierte un DTO de solicitud a una entidad Honorario.
     * Las relaciones (obra, profesional) se ignoran porque deben ser buscadas y asignadas en el servicio.
     * @param requestDTO El DTO con los datos de entrada.
     * @return Una entidad Honorario lista para ser completada y guardada.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "obra", ignore = true)
    @Mapping(target = "profesional", ignore = true)
    Honorario toEntity(HonorarioRequestDTO requestDTO);

    /**
     * Actualiza una entidad Honorario existente a partir de un DTO de solicitud.
     * Ignora el ID y las relaciones, que deben manejarse en el servicio.
     * @param requestDTO El DTO con los datos de actualización.
     * @param honorario La entidad a actualizar.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "obra", ignore = true)
    @Mapping(target = "profesional", ignore = true)
    void updateEntity(@MappingTarget Honorario honorario, HonorarioRequestDTO requestDTO);

    /**
     * Convierte los datos calculados de estadísticas a su DTO correspondiente.
     * La fecha de consulta se genera automáticamente en el momento del mapeo.
     * @param totalHonorarios Cantidad total de honorarios.
     * @param honorariosMesActual Cantidad de honorarios en el mes actual.
     * @param montoTotalMesActual Suma de montos en el mes actual.
     * @return Un DTO con las estadísticas.
     */
    @Mapping(target = "fechaConsulta", expression = "java(java.time.LocalDate.now())")
    EstadisticasHonorarioResponseDTO toEstadisticasDTO(long totalHonorarios, long honorariosMesActual, BigDecimal montoTotalMesActual);

    /**
     * Convierte los datos calculados de un período a su DTO de resumen.
     * @param periodo Rango de fechas.
     * @param cantidadHonorarios Cantidad de honorarios en el período.
     * @param montoTotal Suma de montos en el período.
     * @param montoPromedio Monto promedio por honorario.
     * @return Un DTO con el resumen del período.
     */
    ResumenPeriodoHonorarioDTO toResumenPeriodoDTO(String periodo, long cantidadHonorarios, BigDecimal montoTotal,
            BigDecimal montoPromedio);
}
