package com.rodrigo.construccion.service;

import java.util.List;

import com.rodrigo.construccion.dto.request.ObraRequestDTO;
import com.rodrigo.construccion.dto.response.*;

import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.model.entity.Obra;

public interface IObraService {

    ObraSimpleDTO obtenerPorId(Long id);

    Obra encontrarObraPorIdYEmpresa(Long id, Long idEmpresa);

    Obra findById(Long id);

    Obra buscarPorIdOpcional(Long id);

    List<ObraResponseDTO> obtenerPorCliente(Long clienteId);

    List<ObraSimpleDTO> obtenerPorEstado(EstadoObra estado);

    List<ObraSimpleDTO> obtenerActivas();

    List<ObraResponseDTO> obtenerPorEmpresa(Long empresaId);

    List<ObraResponseDTO> obtenerTodas();

    List<ProfesionalResponseDTO> obtenerProfesionalesAsignados(Long obraId);

    ObraResponseDTO crear(ObraRequestDTO obraRequestDto, Long clienteId);

    ObraResponseDTO actualizar(Long id, ObraRequestDTO obraActualizada);

    void eliminarEnCascada(Long id, Long empresaId);

    ObraResponseDTO cambiarEstado(Long id, EstadoObra nuevoEstado);

    EstadisticasObraDTO obtenerEstadisticas();

    List<ProfesionalResponseDTO> actualizarPorcentajeGananciaTodosAsignados(Long obraId, double porcentaje);

    ProfesionalResponseDTO actualizarPorcentajeGananciaProfesionalAsignado(Long obraId, Long profesionalId,
                                                                           double porcentaje);

    List<String> obtenerEstadosObra();

    boolean existeObra(Long empresaId, Long obraId);

}
