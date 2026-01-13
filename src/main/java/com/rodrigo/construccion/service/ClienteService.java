package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.mapper.ClienteMapper;
import com.rodrigo.construccion.dto.request.ClienteRequestDTO;
import com.rodrigo.construccion.dto.response.ClienteResponseDTO;
import com.rodrigo.construccion.exception.DuplicateCuitException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Cliente;
import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.repository.ClienteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClienteService implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final IEmpresaService empresaService;
    private final ClienteMapper clienteMapper;

    /* Crear un nuevo cliente asociado a varias empresas */
    @Override
    public ClienteResponseDTO crearCliente(ClienteRequestDTO clienteRequestDTO, List<Long> empresaIds) {
        // 1. Validar y obtener todas las entidades Empresa en una sola consulta
        List<Empresa> empresasEntidades = empresaService.findEmpresasByIds(empresaIds);

        // 2. Validar CUIT único en todas las empresas a asociar
        if (clienteRequestDTO.getCuitCuil() != null) {
            for (Long empresaId : empresaIds) {
                if (clienteRepository.existsByEmpresaIdAndCuitCuil(empresaId, clienteRequestDTO.getCuitCuil())) {
                    throw new DuplicateCuitException("cliente", clienteRequestDTO.getCuitCuil(), empresaId);
                }
            }
        }

        // 3. Crear la entidad Cliente a partir del DTO
        Cliente cliente = clienteMapper.toEntity(clienteRequestDTO);

        // 4. Asociar empresas al cliente y guardar
        cliente.setEmpresas(empresasEntidades);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        // 5. Mapear la entidad guardada a DTO para la respuesta
        return clienteMapper.toResponseDTO(clienteGuardado);
    }

    /* Actualizar cliente y sus empresas asociadas */
    @Override
    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO clienteRequestDTO) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        // Validar CUIT único si ha cambiado
        String nuevoCuit = clienteRequestDTO.getCuitCuil();
        if (nuevoCuit != null && !nuevoCuit.equals(clienteExistente.getCuitCuil())) {
            // Realizamos una búsqueda global por el nuevo CUIT.
            clienteRepository.findByCuitCuil(nuevoCuit).ifPresent(otroCliente -> {
                // Si se encuentra un cliente con ese CUIT y no es el mismo que estamos
                // actualizando, lanzamos la excepción.
                if (!otroCliente.getId().equals(id)) {
                    throw new DuplicateCuitException("cliente", nuevoCuit);
                }
            });
        }

        clienteMapper.updateEntityFromRequestDTO(clienteExistente, clienteRequestDTO);
        Cliente clienteGuardado = clienteRepository.save(clienteExistente);

        return clienteMapper.toResponseDTO(clienteGuardado);
    }

    /* Eliminar cliente (soft delete o validar dependencias) */
    public void eliminarCliente(String identificadorCliente, Long empresaId) {
        Long clienteId = resolveIdentifierToId(identificadorCliente, empresaId);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        clienteRepository.delete(cliente);
    }

    /* Obtener clientes con paginación */
    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> obtenerPorEmpresaConPaginacion(Long empresaId, Pageable pageable) {
        empresaService.findEmpresaById(empresaId);

        Page<Cliente> clientePage = clienteRepository.findByEmpresaId(empresaId, pageable);

        return clienteMapper.toResponseDTOPage(clientePage);
    }

    /* Usado por Obra Service */
    @Override
    public Cliente obtenerPorId(Long idCliente) {
        Cliente clienteEncontrado = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", idCliente));
        return clienteEncontrado;
    }

    /* Obtener por ID de cliente y de empresa */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorIdYEmpresa(Long idCliente, Long empresaId) {
        empresaService.findEmpresaById(empresaId);
        Cliente clienteEncontrado = clienteRepository.findByIdAndEmpresaId(idCliente, empresaId).orElseThrow(() ->
                new ResourceNotFoundException("El cliente no existe o no pertenece a la empresa"));
        return clienteMapper.toResponseDTO(clienteEncontrado);
    }

    /* (Usados por el controlador) */

    /* Obtener todos los clientes de una empresa */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerTodosPorEmpresa(Long empresaId) {
        Empresa empresaEncontrada = empresaService.findEmpresaById(empresaId);
        List<Cliente> clientes = clienteRepository.findByEmpresaId(empresaEncontrada.getId());

        return clienteMapper.toResponseDTOList(clientes);
    }

    /* Obtener todos los clientes del sistema (sin filtrar por empresa) */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerTodos() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clienteMapper.toResponseDTOList(clientes);
    }

    /**
     * Busca un cliente por un identificador universal y lo devuelve como un DTO.
     * Este método encapsula la lógica de búsqueda, validación de empresa y mapeo.
     */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarYMapearPorIdentificador(String identificador, Long empresaId) {
        return buscarPorIdentificador(identificador, empresaId)
                .map(clienteMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "identificador", identificador));
    }

    /*
     * Resuelve un identificador (ID, CUIT o nombre) a un ID numérico de Cliente.
     * Este método centraliza la lógica de resolución para ser usada por el
     * controlador.
     */
    @Override
    @Transactional(readOnly = true)
    public Long resolveIdentifierToId(String identificador, Long empresaId) {
        try {
            return Long.parseLong(identificador);
        } catch (NumberFormatException nfe) {
            // Si no es un número, delegamos la búsqueda al método universal.
            // El empresaId puede ser null, buscarPorIdentificador lo manejará.
            return buscarPorIdentificador(identificador, empresaId)
                    .map(Cliente::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", "identificador", identificador));
        }
    }

    /* BÚSQUEDA UNIVERSAL POR IDENTIFICADOR */
    /* Busca un cliente por ID, CUIT/CUIL o nombre de forma inteligente */
    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorIdentificador(String identificador, Long empresaId) {
        // 1. Si el identificador es numérico, la búsqueda es ÚNICAMENTE por ID y debe
        // ser exacta.
        try {
            Long id = Long.parseLong(identificador);
            // Realiza la búsqueda por ID y retorna el resultado inmediatamente, sea
            // encontrado o no.
            // No continúa con otras búsquedas.
            return (empresaId != null)
                    ? clienteRepository.findByIdAndEmpresaId(id, empresaId)
                    : clienteRepository.findById(id);
        } catch (NumberFormatException e) {
            // Si no es un número, procede a buscar por CUIT o nombre.
        }

        // 2. Intentar buscar por CUIT parcial.
        List<Cliente> clientesPorCuit = (empresaId != null)
                ? clienteRepository.findByEmpresaIdAndCuitCuilContainingIgnoreCase(empresaId, identificador)
                : clienteRepository.findByCuitCuilContainingIgnoreCase(identificador);
        if (!clientesPorCuit.isEmpty()) {
            // Si hay una coincidencia exacta, la priorizamos.
            for (Cliente c : clientesPorCuit) {
                if (c.getCuitCuil().equalsIgnoreCase(identificador)) {
                    return Optional.of(c);
                }
            }
            return Optional.of(clientesPorCuit.get(0)); // Si no, devolvemos la primera coincidencia parcial.
        }

        // 3. Intentar buscar por nombre parcial.
        List<Cliente> clientesPorNombre = (empresaId != null)
                ? clienteRepository
                .findByEmpresaIdAndNombreContainingIgnoreCase(empresaId, identificador, Pageable.unpaged())
                .getContent()
                : clienteRepository.findByNombreContainingIgnoreCase(identificador);
        if (!clientesPorNombre.isEmpty()) {
            return Optional.of(clientesPorNombre.get(0));
        }

        return Optional.empty();
    }

}