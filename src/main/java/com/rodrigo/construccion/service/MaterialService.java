package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.response.MaterialEstadisticaResponseDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Material;
import com.rodrigo.construccion.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaterialService {

    private final MaterialRepository materialRepository;

    /* Obtener todos los materiales activos */
    @Transactional(readOnly = true)
    public List<Material> obtenerTodosActivos() {
        return materialRepository.findByActivoTrue();
    }

    /* Obtener material por ID */
    @Transactional(readOnly = true)
    public Material obtenerPorId(Long id) {
        return materialRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));
    }

    /* Buscar materiales por texto */
    @Transactional(readOnly = true)
    public Page<Material> buscarPorTexto(String texto, Pageable pageable) {
        return materialRepository.findByTextoContaining(texto, pageable);
    }

    /* Obtener materiales por rango de precio */
    @Transactional(readOnly = true)
    public List<Material> obtenerPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        return materialRepository.findByActivoTrueAndPrecioBetween(precioMin, precioMax);
    }

    /* Crear nuevo material */
    public Material crear(Material material) {
        // Establecer valores por defecto
        if (material.getActivo() == null) {
            material.setActivo(true);
        }
        return materialRepository.save(material);
    }

    /* Actualizar material */
    public Material actualizar(Long id, Material materialActualizado) {
        Material materialExistente = obtenerPorId(id);

        // Actualizar campos permitidos
        if (materialActualizado.getNombre() != null) {
            materialExistente.setNombre(materialActualizado.getNombre());
        }

        if (materialActualizado.getDescripcion() != null) {
            materialExistente.setDescripcion(materialActualizado.getDescripcion());
        }

        if (materialActualizado.getUnidadMedida() != null) {
            materialExistente.setUnidadMedida(materialActualizado.getUnidadMedida());
        }

        if (materialActualizado.getPrecioUnitario() != null) {
            materialExistente.setPrecioUnitario(materialActualizado.getPrecioUnitario());
        }

        return materialRepository.save(materialExistente);
    }

    /* Eliminar material (desactivar) */
    public void eliminar(Long id) {
        Material material = obtenerPorId(id);
        material.setActivo(false);
        materialRepository.save(material);
    }


    /* MÉTODOS QUE NO ESTÁN SIENDO USADOS POR EL FRONTEND - PARA BORRAR */

    /* Obtener materiales con paginación */
    @Transactional(readOnly = true)
    public Page<Material> obtenerMaterialesPaginados(Pageable pageable) {
        List<Material> materiales = materialRepository.findByActivoTrue();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), materiales.size());

        List<Material> pageContent = materiales.subList(start, end);
        return new PageImpl<>(pageContent, pageable, materiales.size());
    }

    /* Obtener materiales por unidad de medida */
    @Transactional(readOnly = true)
    public List<Material> obtenerPorUnidadMedida(String unidadMedida) {
        return materialRepository.findByActivoTrueAndUnidadMedida(unidadMedida);
    }

    /* Obtener estadísticas generales */
    @Transactional(readOnly = true)
    public MaterialEstadisticaResponseDTO obtenerEstadisticas() {
        long totalMateriales = materialRepository.countByActivoTrue();
        BigDecimal precioPromedio = materialRepository.findPrecioPromedio();

        return new MaterialEstadisticaResponseDTO(
                totalMateriales,
                precioPromedio != null ? precioPromedio : BigDecimal.ZERO
        );
    }

    /* Obtener precio promedio */
    @Transactional(readOnly = true)
    public BigDecimal obtenerPrecioPromedio() {
        BigDecimal precio = materialRepository.findPrecioPromedio();
        return precio != null ? precio : BigDecimal.ZERO;
    }

    /* Obtener todos los materiales ordenados por nombre */
    @Transactional(readOnly = true)
    public List<Material> obtenerTodosOrdenadosPorNombre() {
        return materialRepository.findAllActivosOrdenadosPorNombre();
    }
}