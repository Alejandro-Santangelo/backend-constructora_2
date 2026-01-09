package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.dto.request.ObraRequestDTO;
import com.rodrigo.construccion.dto.response.ObraSimpleDTO;
import com.rodrigo.construccion.dto.response.ObraResponseDTO;
import com.rodrigo.construccion.model.entity.Obra;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mapper para convertir entre entidades Obra y DTOs
 */
@Mapper(componentModel = "spring", uses = ClienteMapper.class, unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ObraMapper {

    /**
     * Convierte de RequestDTO a entidad
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "honorarios", ignore = true) // Ignorar honorarios para evitar ciclos
    @Mapping(source = "idCliente", target = "cliente.id")
    Obra toEntity(ObraRequestDTO obraRequestDTO);

    /**
     * Convierte de entidad a ResponseDTO
     */
    @Mapping(source = "cliente.id", target = "idCliente")
    @Mapping(source = "estadoEnum", target = "estado")
    @Mapping(target = "presupuestos", ignore = true) // Los presupuestos se cargan por separado en el servicio si es necesario
    ObraResponseDTO toResponseDTO(Obra obra);

    /**
     * Convierte lista de entidades a lista de DTOs
     */
    List<ObraResponseDTO> toResponseDTOList(List<Obra> obras);

    /**
     * Convierte Page de entidades a Page de DTOs
     */
    default Page<ObraResponseDTO> toResponseDTOPage(Page<Obra> obraPage) {
        return obraPage.map(this::toResponseDTO);
    }

    /**
     * Actualiza una entidad existente con datos del DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "profesionalesAsignados", ignore = true)
    @Mapping(target = "presupuestosNoCliente", ignore = true)
    @Mapping(target = "cobros", ignore = true)
    @Mapping(target = "pedidosPago", ignore = true)
    @Mapping(target = "costos", ignore = true)
    @Mapping(target = "honorarios", ignore = true)
    void updateEntityFromDto(ObraRequestDTO obraRequestDTO, @MappingTarget Obra obra);

    /**
     * Convierte de entidad a ResponseDTO Simple
     */
    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "estadoEnum", target = "estado")
    ObraSimpleDTO toSimpleDTO(Obra obra);

    /**
     * Convierte lista de entidades a lista de DTOs Simples
     */
    List<ObraSimpleDTO> toSimpleDTOList(List<Obra> obras);

    /**
     * Método de conversión personalizado para el estado.
     * MapStruct usará este método automáticamente cuando necesite convertir
     * un String a un EstadoObra.
     */
    default EstadoObra mapEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        return EstadoObra.fromDisplayName(estado);
    }
}