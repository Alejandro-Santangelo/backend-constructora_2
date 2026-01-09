package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.EmpresaRequestDTO;
import com.rodrigo.construccion.dto.response.EmpresaEstadisticasDTO;
import com.rodrigo.construccion.dto.response.EmpresaEstadoResponseDTO;
import com.rodrigo.construccion.dto.response.EmpresaResponseDTO;
import com.rodrigo.construccion.model.entity.Empresa;

import java.util.List;

public interface IEmpresaService {

    EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO empresaRequestDTO);

    List<EmpresaResponseDTO> obtenerEmpresasActivas();

    List<EmpresaResponseDTO> obtenerTodasLasEmpresas();

    EmpresaResponseDTO actualizarEmpresa(Long id, EmpresaRequestDTO empresaRequestDtoModificada);

    void desactivarEmpresa(Long id);

    void activarEmpresa(Long id);

    List<EmpresaResponseDTO> obtenerEmpresasConClientes();

    List<EmpresaEstadisticasDTO> obtenerEstadisticasEmpresas();

    boolean esCuitDisponible(String cuit);

    EmpresaEstadoResponseDTO verificarEstado(Long id);

    Empresa findEmpresaById(Long id);

    EmpresaResponseDTO buscarPorIdentificador(String identificador);

    List<Empresa> findEmpresasByIds(List<Long> ids);

}