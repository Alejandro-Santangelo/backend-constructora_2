package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.MaterialRequestDTO;
import com.rodrigo.construccion.dto.response.MaterialEstadisticaResponseDTO;
import com.rodrigo.construccion.model.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface IMaterialService {

    List<Material> obtenerTodosActivos();

    Material obtenerPorId(Long id);

    Page<Material> buscarPorTexto(String texto, Pageable pageable);

    List<Material> obtenerPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax);

    Material crear(MaterialRequestDTO material);

    Material actualizar(Long id, MaterialRequestDTO materialActualizado);

    void eliminar(Long id);

    Page<Material> obtenerMaterialesPaginados(Pageable pageable);

    List<Material> obtenerPorUnidadMedida(String unidadMedida);

    MaterialEstadisticaResponseDTO obtenerEstadisticas();

    BigDecimal obtenerPrecioPromedio();

    List<Material> obtenerTodosOrdenadosPorNombre();

    /* Busca un material por nombre (case-insensitive). Si no existe, crea uno nuevo con los datos proporcionados. */
    Material buscarOCrearPorNombre(String nombre, String unidadMedida, BigDecimal precioUnitario);

    /* Busca un material por ID sin lanzar excepción si no existe */
    Material buscarPorIdOpcional(Long id);
}
