package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.mapper.ProveedorMapper;
import com.rodrigo.construccion.dto.request.ProveedorRequestDTO;
import com.rodrigo.construccion.dto.response.ProveedorEstadisticaResponseDTO;
import com.rodrigo.construccion.dto.response.ProveedorResponseDTO;
import com.rodrigo.construccion.dto.response.RutValidacionResponseDTO;
import com.rodrigo.construccion.exception.DuplicateRutException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.model.entity.Proveedor;
import com.rodrigo.construccion.repository.ProveedorRepository;
// ...existing imports...
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProveedorService implements IProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final IEmpresaService empresaService;
    private final ProveedorMapper proveedorMapper;

    /* Crear un nuevo proveedor */
    public ProveedorResponseDTO crearProveedor(ProveedorRequestDTO dto, Long empresaId) {
        // Mapear DTO a entidad
        Proveedor proveedor = proveedorMapper.toEntity(dto);
        Empresa empresaEncontrada = empresaService.findEmpresaById(empresaId);

        // Verificar si el RUT ya existe (si se proporciona)
        if (proveedor.getRut() != null && !proveedor.getRut().trim().isEmpty()) {
            if (proveedorRepository.existsByRutAndEmpresa_Id(proveedor.getRut(), empresaId)) {
                throw new DuplicateRutException("proveedor", proveedor.getRut(), empresaId);
            }
        }

        // Setear valores por defecto y relación con empresa
        proveedor.setEmpresa(empresaEncontrada);
        proveedor.setActivo(true);
        proveedor.setFechaCreacion(LocalDateTime.now());

        // Guardar y retornar el DTO de respuesta
        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponseDTO(proveedorGuardado);
    }

    /* Obtener proveedores por empresa con paginación */
    @Transactional(readOnly = true)
    public Page<ProveedorResponseDTO> obtenerPorEmpresaConPaginacion(Long empresaId, Pageable pageable) {
        Page<Proveedor> proveedoresPage = proveedorRepository.findByEmpresa_IdOrderByNombre(empresaId, pageable);
        return proveedoresPage.map(proveedorMapper::toResponseDTO);
    }

    /* Obtener proveedor por ID y empresa - usado para el frontend */
    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerPorIdYEmpresaDTO(Long id, Long empresaId) {
        Proveedor proveedorEncontrado = proveedorRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Proveedor no encontrado con ID: " + id + " para la empresa: " + empresaId));
        return proveedorMapper.toResponseDTO(proveedorEncontrado);
    }

    /* Obtener proveedor por ID y empresa usado solamente en este archivo */
    @Override
    @Transactional(readOnly = true)
    public Proveedor obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return proveedorRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Proveedor no encontrado con ID: " + id + " para la empresa: " + empresaId));
    }

    /* Actualizar proveedor */
    public ProveedorResponseDTO actualizarProveedor(Long idProveedor, ProveedorRequestDTO dto, Long empresaId) {
        var proveedorExistente = obtenerPorIdYEmpresa(idProveedor, empresaId);

        // Validar RUT si cambió
        if (dto.getRut() != null && !dto.getRut().equals(proveedorExistente.getRut())) {
            if (proveedorRepository.existsByRutAndEmpresa_IdAndIdNot(dto.getRut(), empresaId, idProveedor)) {
                throw new DuplicateRutException("proveedor", dto.getRut(), empresaId);
            }
        }

        // Actualizar todos los campos del DTO usando el mapper
        proveedorMapper.updateEntityFromDto(dto, proveedorExistente);

        Proveedor proveedorActualizado = proveedorRepository.save(proveedorExistente);
        return proveedorMapper.toResponseDTO(proveedorActualizado);
    }

    /* Eliminar proveedor */
    public void eliminarProveedor(Long id, Long empresaId) {
        var proveedor = obtenerPorIdYEmpresa(id, empresaId);
        proveedorRepository.delete(proveedor);
    }

    /* Buscar proveedores por nombre  */
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> buscarPorNombre(String nombre, Long empresaId) {
        return proveedorRepository.findByNombreContainingIgnoreCaseAndEmpresa_Id(nombre, empresaId)
                .stream()
                .map(proveedorMapper::toResponseDTO)
                .toList();
    }

    /* Obtener proveedores activos */
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> obtenerProveedoresActivos(Long empresaId) {
        List<Proveedor> proveedoresEncontrados = proveedorRepository.findByActivoTrueAndEmpresa_IdOrderByNombre(empresaId);
        return proveedoresEncontrados.stream().map(proveedorMapper::toResponseDTO).toList();
    }

    /* MÉTODOS QUE NO SE ESTÁN USANDO EN EL FRONTEND */

    /* Obtener proveedores por ciudad */
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> obtenerPorCiudad(String ciudad, Long empresaId) {
        List<Proveedor> proveedoresEncontrados = proveedorRepository.findByCiudadIgnoreCaseAndEmpresa_Id(ciudad, empresaId);
        return proveedoresEncontrados.stream().map(proveedorMapper::toResponseDTO).toList();
    }

    /* Obtener proveedores por región */
    @Transactional(readOnly = true)
    public List<ProveedorResponseDTO> obtenerPorRegion(String region, Long empresaId) {
        List<Proveedor> proveedoresEncontrados = proveedorRepository.findByRegionIgnoreCaseAndEmpresa_Id(region, empresaId);
        return proveedoresEncontrados.stream().map(proveedorMapper::toResponseDTO).toList();
    }

    /* Validar si un RUT está disponible  */
    @Transactional(readOnly = true)
    public RutValidacionResponseDTO validarRutDisponible(String rut, Long empresaId) {
        boolean disponible = !proveedorRepository.existsByRutAndEmpresa_Id(rut, empresaId);
        return new RutValidacionResponseDTO(disponible, rut);
    }

    /* Obtener estadísticas de proveedores */
    @Transactional(readOnly = true)
    public ProveedorEstadisticaResponseDTO obtenerEstadisticas(Long empresaId) {
        // Obtener datos del repository
        long totalProveedores = proveedorRepository.countByEmpresa_Id(empresaId);
        long proveedoresActivos = proveedorRepository.countByActivoTrueAndEmpresa_Id(empresaId);
        long proveedoresInactivos = totalProveedores - proveedoresActivos;
        var distribucionCiudad = proveedorRepository.countProveedoresPorCiudad(empresaId);
        var distribucionRegion = proveedorRepository.countProveedoresPorRegion(empresaId);

        // Usar el mapper para construir el DTO
        return proveedorMapper.toEstadisticasDTO(
            totalProveedores,
            proveedoresActivos,
            proveedoresInactivos,
            distribucionCiudad,
            distribucionRegion
        );
    }

    /* Cambiar estado del proveedor */
    public ProveedorResponseDTO cambiarEstado(Long id, boolean activo, Long empresaId) {
        var proveedor = obtenerPorIdYEmpresa(id, empresaId);
        proveedor.setActivo(activo);
        Proveedor proveedorActualizado = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponseDTO(proveedorActualizado);
    }
}