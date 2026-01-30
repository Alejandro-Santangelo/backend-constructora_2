package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.dto.request.PedidoPagoRequestDTO;
import com.rodrigo.construccion.dto.response.PedidoPagoResponseDTO;
import com.rodrigo.construccion.dto.response.ProveedorPedidoPagoResponseDTO;
import com.rodrigo.construccion.model.entity.PedidoPago;
import com.rodrigo.construccion.model.entity.Proveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para convertir entre entidades PedidoPago y DTOs
 * Reutiliza ObraMapper y EmpresaMapper para las conversiones anidadas
 */
@Mapper(componentModel = "spring",
        uses = {ObraMapper.class, EmpresaMapper.class},
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface PedidoPagoMapper {

    /**
     * Convierte de entidad PedidoPago a PedidoPagoResponseDTO
     * MapStruct automáticamente usa ObraMapper.toSimpleDTO() y EmpresaMapper.toResponseDTO()
     * para las propiedades anidadas obra y empresa
     */
    PedidoPagoResponseDTO toResponseDTO(PedidoPago pedidoPago);

    /**
     * Convierte de PedidoPagoRequestDTO a entidad PedidoPago
     * Los campos de auditoría, relaciones y estado se setean en el servicio
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "obra", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaAprobacion", ignore = true)
    @Mapping(target = "fechaAutorizacion", ignore = true)
    @Mapping(target = "fechaPago", ignore = true)
    @Mapping(target = "motivoRechazo", ignore = true)
    @Mapping(target = "usuarioAprobadorId", ignore = true)
    @Mapping(target = "usuarioAutorizadorId", ignore = true)
    @Mapping(target = "usuarioPagadorId", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    PedidoPago toEntity(PedidoPagoRequestDTO dto);

    /**
     * Convierte un Proveedor a ProveedorPedidoPagoResponseDTO
     * Este método es necesario porque no existe un ProveedorMapper para este DTO específico
     */
    ProveedorPedidoPagoResponseDTO toProveedorDTO(Proveedor proveedor);
}
