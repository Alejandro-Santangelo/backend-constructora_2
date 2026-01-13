package com.rodrigo.construccion.service;

import java.util.List;

import com.rodrigo.construccion.dto.request.ObraRequestDTO;
import com.rodrigo.construccion.dto.response.*;

import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.model.entity.Obra;

public interface IObraService {

    public ObraSimpleDTO obtenerPorId(Long id);

    public Obra encontrarObraPorIdYEmpresa(Long id, Long idEmpresa);

    public Obra findById(Long id);

    public List<ObraResponseDTO> obtenerPorCliente(Long clienteId);

    public List<ObraSimpleDTO> obtenerPorEstado(EstadoObra estado);

    public List<ObraSimpleDTO> obtenerActivas();

    public List<ObraResponseDTO> obtenerPorEmpresa(Long empresaId);

    public List<ObraResponseDTO> obtenerTodas();

    public List<ProfesionalResponseDTO> obtenerProfesionalesAsignados(Long obraId);

    public ObraResponseDTO crear(ObraRequestDTO obraRequestDto, Long clienteId);

    public ObraResponseDTO actualizar(Long id, ObraRequestDTO obraActualizada);

    public void eliminarEnCascada(Long id, Long empresaId);

    public ObraResponseDTO cambiarEstado(Long id, EstadoObra nuevoEstado);

    public EstadisticasObraDTO obtenerEstadisticas();

    public List<ProfesionalResponseDTO> actualizarPorcentajeGananciaTodosAsignados(Long obraId, double porcentaje);

    public ProfesionalResponseDTO actualizarPorcentajeGananciaProfesionalAsignado(Long obraId, Long profesionalId,
            double porcentaje);

    public List<String> obtenerEstadosObra();

    public boolean existeObra(Long empresaId, Long obraId);

}
