package com.rodrigo.construccion.service;

import java.util.List;

import com.rodrigo.construccion.dto.request.AsignarProfesionalRequest;
import com.rodrigo.construccion.dto.request.ProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.model.entity.Profesional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProfesionalService {

    public List<ProfesionalResponseDTO> obtenerTodos();

    public List<Profesional> findAllEntities();

    public Profesional obtenerPorId(Long id);

    public ProfesionalResponseDTO obtenerProfesionalPorId(Long id);

    public ProfesionalResponseDTO crearProfesional(ProfesionalRequestDTO requestDTO);

    public ProfesionalResponseDTO actualizar(Long id, ProfesionalRequestDTO requestDTO);

    public void eliminar(Long id);

    public List<ProfesionalResponseDTO> buscarPorTipo(String tipoProfesional);

    public List<ProfesionalResponseDTO> buscarPorNombre(String nombre);

    public void actualizarValorHoraTodosPorPorcentaje(double porcentaje);

    public void actualizarValorHoraPorIdPorPorcentaje(Long id, double porcentaje);

    public void actualizarPorcentajeGananciaTodos(double porcentaje);

    public void actualizarPorcentajeGananciaPorId(Long id, double porcentaje);

    public void actualizarPorcentajeGananciaVarios(List<Long> ids, double porcentaje);

    public List<String> obtenerTiposProfesionales();

    public Profesional findProfesionalParaAsignacion(AsignarProfesionalRequest request);

    public List<Profesional> buscarActivosPorTipoFlexible(String tipoProfesional);

    public List<Profesional> buscarPorTipoProfesionalActivos(String tipoProfesional);

    public Profesional findFirstActivoByTipo(String tipoProfesional);

    Page<Profesional> findAllWithHonorarios(Pageable pageable, Long empresaId);

}
