package com.rodrigo.construccion.service;

import java.util.List;

import com.rodrigo.construccion.dto.request.AsignarProfesionalRequest;
import com.rodrigo.construccion.dto.request.ProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.model.entity.Profesional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProfesionalService {

    List<ProfesionalResponseDTO> obtenerTodos();

    List<Profesional> findAllEntities();

    Profesional obtenerPorId(Long id);

    ProfesionalResponseDTO obtenerProfesionalPorId(Long id);

    ProfesionalResponseDTO crearProfesional(ProfesionalRequestDTO requestDTO);

    ProfesionalResponseDTO actualizar(Long id, ProfesionalRequestDTO requestDTO);

    void eliminar(Long id);

    List<ProfesionalResponseDTO> buscarPorTipo(String tipoProfesional);

    List<ProfesionalResponseDTO> buscarPorNombre(String nombre);

    void actualizarValorHoraTodosPorPorcentaje(double porcentaje);

    void actualizarValorHoraPorIdPorPorcentaje(Long id, double porcentaje);

    void actualizarPorcentajeGananciaTodos(double porcentaje);

    void actualizarPorcentajeGananciaPorId(Long id, double porcentaje);

    Profesional findProfesionalParaAsignacion(AsignarProfesionalRequest request);

    List<Profesional> buscarActivosPorTipoFlexible(String tipoProfesional);

    List<Profesional> buscarPorTipoProfesionalActivos(String tipoProfesional);

    Profesional findFirstActivoByTipo(String tipoProfesional);

    Page<Profesional> findAllWithHonorarios(Pageable pageable, Long empresaId);

}
