package com.rodrigo.construccion.dto.mapper;

import com.rodrigo.construccion.dto.request.ProveedorRequestDTO;
import com.rodrigo.construccion.dto.response.ProveedorEstadisticaResponseDTO;
import com.rodrigo.construccion.dto.response.ProveedorResponseDTO;
import com.rodrigo.construccion.model.entity.Proveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ProveedorMapper {

    /**
     * Convierte un ProveedorRequestDTO a la entidad Proveedor
     * Los campos empresaId, activo, fechaCreacion y fechaModificacion se setean en el Service
     */
    Proveedor toEntity(ProveedorRequestDTO dto);

    /**
     * Convierte la entidad Proveedor a ProveedorResponseDTO
     * Mapea el nombre de la empresa desde la relación con Empresa
     */
    @Mapping(source = "empresa.nombreEmpresa", target = "nombreEmpresa")
    ProveedorResponseDTO toResponseDTO(Proveedor proveedor);

    /**
     * Actualiza una entidad Proveedor existente con los datos del DTO
     * Ignora los campos que no deben ser actualizados por el DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "fechaUltimaCompra", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    void updateEntityFromDto(ProveedorRequestDTO dto, @MappingTarget Proveedor proveedor);

    /**
     * Construye el DTO de estadísticas de proveedores
     */
    default ProveedorEstadisticaResponseDTO toEstadisticasDTO(
            long total,
            long activos,
            long inactivos,
            List<Map<String, Object>> distribucionCiudad,
            List<Map<String, Object>> distribucionRegion) {

        double porcentajeActivos = total > 0 ? (activos * 100.0 / total) : 0.0;

        return new ProveedorEstadisticaResponseDTO(
            total,
            activos,
            inactivos,
            porcentajeActivos,
            distribucionCiudad,
            distribucionRegion
        );
    }
}
