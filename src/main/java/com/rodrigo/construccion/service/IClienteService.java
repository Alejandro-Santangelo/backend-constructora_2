package com.rodrigo.construccion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rodrigo.construccion.dto.request.ClienteRequestDTO;
import com.rodrigo.construccion.dto.response.ClienteResponseDTO;
import com.rodrigo.construccion.model.entity.Cliente;

public interface IClienteService {

    public ClienteResponseDTO crearCliente(ClienteRequestDTO clienteRequestDTO, List<Long> empresaIds);

    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO clienteRequestDTO);

    public void eliminarCliente(String identificador, Long empresaId);

    public Page<ClienteResponseDTO> obtenerPorEmpresaConPaginacion(Long empresaId, Pageable pageable);

    public Cliente obtenerPorId(Long idCliente);

    public ClienteResponseDTO obtenerPorIdYEmpresa(Long idCliente, Long empresaId);

    public List<ClienteResponseDTO> obtenerTodosPorEmpresa(Long empresaId);

    public List<ClienteResponseDTO> obtenerTodos();

    public ClienteResponseDTO buscarYMapearPorIdentificador(String identificador, Long empresaId);

    public Long resolveIdentifierToId(String identificador, Long empresaId);

    public Optional<Cliente> buscarPorIdentificador(String identificador, Long empresaId);

}
