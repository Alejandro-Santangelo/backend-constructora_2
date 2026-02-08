package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.dto.request.AsignarProfesionalRequest;
import com.rodrigo.construccion.dto.response.AsignacionProfesionalResponse;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = ProfesionalMapper.class, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ProfesionalObraMapper {

    @Mapping(source = "id", target = "idAsignacion")
    @Mapping(source = "id", target = "profesionalObraId")
    @Mapping(source = "profesional.id", target = "profesionalId")
    @Mapping(source = "profesional.nombre", target = "nombreProfesional")
    @Mapping(source = "profesional.tipoProfesional", target = "tipoProfesional")
    @Mapping(source = "obra.id", target = "obraId")
    @Mapping(source = "obra.nombre", target = "nombreObra")
    @Mapping(source = "obra.estado", target = "estadoObra")
    @Mapping(source = "direccionObraCompleta", target = "direccionObra")
    @Mapping(expression = "java(profesionalObra.getFechaCreacion() != null ? profesionalObra.getFechaCreacion().toLocalDate() : null)", target = "fechaCreacion")
    AsignacionProfesionalResponse toResponseDTO(ProfesionalObra profesionalObra);

    List<AsignacionProfesionalResponse> toResponseDTOList(List<ProfesionalObra> profesionalObras);

    /**
     * Mapea una entidad ProfesionalObra a un ProfesionalResponseDTO,
     * extrayendo la información del profesional anidado.
     * MapStruct usará este método para implementar automáticamente el método de
     * lista.
     */
    @Mapping(source = "profesional", target = ".")
    @Mapping(target = "cantidadObrasAsignadas", ignore = true)
    @Mapping(target = "cantidadHonorarios", ignore = true)
    ProfesionalResponseDTO toProfesionalResponseDTO(ProfesionalObra asignacion);

    List<ProfesionalResponseDTO> toProfesionalResponseDTOList(List<ProfesionalObra> profesionalesAsignados);

    /**
     * Mapea un AsignarProfesionalRequest hacia la entidad ProfesionalObra.
     * Nota: el mapper no resuelve relaciones (profesional, obra); el servicio debe
     * buscar y setear las entidades correspondientes después de la conversión.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profesional", ignore = true)
    @Mapping(target = "direccionObraCalle", ignore = true)
    @Mapping(target = "direccionObraAltura", ignore = true)
    @Mapping(target = "direccionObraPiso", ignore = true)
    @Mapping(target = "direccionObraDepartamento", ignore = true)
    @Mapping(target = "empresaId", ignore = true)
    @Mapping(target = "montoAsignado", ignore = true)
    @Mapping(target = "saldoDisponible", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "modalidad", ignore = true)
    @Mapping(target = "obra", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    @Mapping(target = "rolEnObra", source = "rolEnObra")
    @Mapping(target = "fechaDesde", source = "fechaDesde")
    @Mapping(target = "fechaHasta", source = "fechaHasta")
    @Mapping(target = "valorHoraAsignado", expression = "java(request.getValorHoraAsignado() != null ? java.math.BigDecimal.valueOf(request.getValorHoraAsignado()) : null)")
    @Mapping(target = "activo", source = "activo")
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "jornales", ignore = true)
    @Mapping(target = "rolEnObraEnum", ignore = true)
    ProfesionalObra fromRequest(AsignarProfesionalRequest request);
}