package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.dto.request.EmpresaRequestDTO;
import com.rodrigo.construccion.dto.response.EmpresaResponseDTO;
import com.rodrigo.construccion.model.entity.Empresa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "activa", ignore = true)
    @Mapping(target = "razonSocial", ignore = true)
    @Mapping(target = "clientes", ignore = true)
    @Mapping(target = "presupuestosNoCliente", ignore = true)
    // usuarios: Relación desactivada, Usuario tiene id_empresa directo
    Empresa toEntity(EmpresaRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "razonSocial", ignore = true)
    @Mapping(target = "clientes", ignore = true)
    @Mapping(target = "presupuestosNoCliente", ignore = true)
    // usuarios: Relación desactivada, Usuario tiene id_empresa directo
    void updateEntityFromRequestDTO(@MappingTarget Empresa empresa, EmpresaRequestDTO requestDTO);

    EmpresaResponseDTO toResponseDTO(Empresa empresa);

    List<EmpresaResponseDTO> toResponseDTOList(List<Empresa> empresas);

    @Mapping(target = "razonSocial", ignore = true)
    @Mapping(target = "cuit", ignore = true)
    @Mapping(target = "direccionFiscal", ignore = true)
    @Mapping(target = "telefono", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "representanteLegal", ignore = true)
    @Mapping(target = "clientes", ignore = true)
    @Mapping(target = "presupuestosNoCliente", ignore = true)
    // usuarios: Relación desactivada, Usuario tiene id_empresa directo
    Empresa toEntityFromResponseDTO(EmpresaResponseDTO dto);
}