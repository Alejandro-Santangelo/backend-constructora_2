package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.dto.request.ClienteRequestDTO;
import com.rodrigo.construccion.dto.response.ClienteResponseDTO;
import com.rodrigo.construccion.dto.response.EmpresaResponseDTO;
import com.rodrigo.construccion.model.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import java.util.*;

@Mapper(componentModel = "spring", uses = {EmpresaMapper.class})
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "empresas", ignore = true)
    @Mapping(target = "obras", ignore = true)
    Cliente toEntity(ClienteRequestDTO requestDTO);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "empresas", ignore = true)
    @Mapping(target = "obras", ignore = true)
    void updateEntityFromRequestDTO(@MappingTarget Cliente cliente, ClienteRequestDTO requestDTO);
    
    @Mapping(source = "id", target = "id_cliente")
    ClienteResponseDTO toResponseDTO(Cliente cliente);
    
    List<ClienteResponseDTO> toResponseDTOList(List<Cliente> clientes);
    
    default Page<ClienteResponseDTO> toResponseDTOPage(Page<Cliente> clientePage) {
        return clientePage.map(this::toResponseDTO);
    }
    
    @Named("toResponseDTOWithFilteredEmpresas")
    default ClienteResponseDTO toResponseDTOWithFilteredEmpresas(Cliente cliente, Long empresaId) {
        ClienteResponseDTO dto = toResponseDTO(cliente);
        if (dto == null || dto.getEmpresas() == null || empresaId == null) {
            return dto;
        }
        List<EmpresaResponseDTO> empresaFiltrada = dto.getEmpresas().stream()
                .filter(e -> empresaId.equals(e.getId()))
                .toList();
        dto.setEmpresas(empresaFiltrada);
        return dto;
    }
}