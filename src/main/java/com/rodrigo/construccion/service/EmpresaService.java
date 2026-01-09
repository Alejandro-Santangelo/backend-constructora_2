package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.mapper.EmpresaMapper;
import com.rodrigo.construccion.dto.request.EmpresaRequestDTO;
import com.rodrigo.construccion.dto.response.EmpresaEstadisticasDTO;
import com.rodrigo.construccion.dto.response.EmpresaEstadoResponseDTO;
import com.rodrigo.construccion.dto.response.EmpresaResponseDTO;
import com.rodrigo.construccion.exception.DuplicateCuitException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/* Servicio para la gestión de Empresas */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmpresaService implements IEmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    /* Crear una nueva empresa */
    @Override
    public EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO empresaRequestDTO) {

        // Validar CUIT único
        if (empresaRepository.existsByCuit(empresaRequestDTO.getCuit()))
            throw new DuplicateCuitException("empresa", empresaRequestDTO.getCuit());

        Empresa empresa = empresaMapper.toEntity(empresaRequestDTO);
        Empresa empresaGuardada = empresaRepository.save(empresa);
        EmpresaResponseDTO responseDTO = empresaMapper.toResponseDTO(empresaGuardada);

        return responseDTO;
    }

    /* Obtener la entidad Empresa por ID (para uso interno de otros servicios) */
    @Override
    @Transactional(readOnly = true)
    public Empresa findEmpresaById(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", id));
    }

    /**
     * Busca una lista de empresas por sus IDs en una sola consulta.
     * Valida que todas las empresas solicitadas existan.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Empresa> findEmpresasByIds(List<Long> ids) {
        List<Empresa> empresas = empresaRepository.findAllById(ids);
        if (empresas.size() != ids.size()) {
            throw new ResourceNotFoundException("Una o más empresas no fueron encontradas.");
        }
        return empresas;
    }

    /* Obtener todas las empresas activas */
    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> obtenerEmpresasActivas() {
        List<Empresa> listaEmpresasActivas = empresaRepository.findByActivaTrue();
        return empresaMapper.toResponseDTOList(listaEmpresasActivas);
    }

    /* Obtener TODAS las empresas (activas e inactivas) */
    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> obtenerTodasLasEmpresas() {
        List<Empresa> todasLasEmpresas = empresaRepository.findAll();
        return empresaMapper.toResponseDTOList(todasLasEmpresas);
    }

    /* Actualizar empresa */
    @Override
    public EmpresaResponseDTO actualizarEmpresa(Long id, EmpresaRequestDTO empresaRequestDtoModificada) {
        Empresa empresaExistente = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));

        String nuevoCuit = (empresaRequestDtoModificada.getCuit() != null)
                ? empresaRequestDtoModificada.getCuit().trim()
                : null;

        if (nuevoCuit != null) {
            empresaRepository.findByCuitAndActivaTrueAndIdNot(nuevoCuit, id).ifPresent(e -> {
                throw new DuplicateCuitException("empresa", nuevoCuit);
            });
        }

        empresaMapper.updateEntityFromRequestDTO(empresaExistente, empresaRequestDtoModificada);

        Empresa empresaGuardada = empresaRepository.save(empresaExistente);

        return empresaMapper.toResponseDTO(empresaGuardada);
    }

    /* Desactivar empresa (soft delete) */
    @Override
    public void desactivarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));

        empresa.setActiva(false);
        empresaRepository.save(empresa);
    }

    /* Activar empresa */
    @Override
    public void activarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con ID: " + id));

        empresa.setActiva(true);
        empresaRepository.save(empresa);
    }

    /* Obtener empresas con clientes */
    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> obtenerEmpresasConClientes() {
        List<Empresa> empresasClientes = empresaRepository.findEmpresasWithClientes();
        return empresaMapper.toResponseDTOList(empresasClientes);
    }

    /* Obtener estadísticas de empresas */
    @Override
    @Transactional(readOnly = true)
    public List<EmpresaEstadisticasDTO> obtenerEstadisticasEmpresas() {
        return empresaRepository.findEmpresaEstadisticas();
    }

    /* Verificar estado de empresa */
    @Override
    @Transactional(readOnly = true)
    public EmpresaEstadoResponseDTO verificarEstado(Long id) {
        return empresaRepository.findEstadoById(id)
                .orElse(new EmpresaEstadoResponseDTO(false, null, null));
    }

    /* Validar disponibilidad de CUIT */
    @Override
    @Transactional(readOnly = true)
    public boolean esCuitDisponible(String cuit) {
        return !empresaRepository.existsByCuit(cuit);
    }

    /* BÚSQUEDA UNIVERSAL: Busca una empresa por ID, CUIT o nombre de forma inteligente */
    @Transactional(readOnly = true)
    public EmpresaResponseDTO buscarPorIdentificador(String identificador) {
        Empresa empresa = null;

        if (identificador == null || identificador.trim().isEmpty()) {
            throw new IllegalArgumentException("El parámetro 'identificador' es obligatorio y no puede estar vacío");
        }

        // 1. Intentar buscar por ID (si es un número)
        try {
            Long id = Long.parseLong(identificador);
            empresa = empresaRepository.findById(id).orElse(null);
            if (empresa != null) {
                return empresaMapper.toResponseDTO(empresa);
            }
        } catch (NumberFormatException e) {
            // Continuar con otras estrategias de búsqueda
        }

        // 2. Intentar buscar por CUIT
        empresa = empresaRepository.findByCuit(identificador).orElse(null);
        if (empresa != null) {
            return empresaMapper.toResponseDTO(empresa);
        }

        // 3. Intentar buscar por nombre exacto (case insensitive)
        empresa = empresaRepository.findByNombreEmpresaIgnoreCase(identificador).orElse(null);
        if (empresa != null) {
            return empresaMapper.toResponseDTO(empresa);
        }

        // 4. Intentar buscar por nombre parcial (case insensitive)
        List<Empresa> empresas = empresaRepository.findByNombreEmpresaContainingIgnoreCase(identificador);
        if (!empresas.isEmpty()) {
            return empresaMapper.toResponseDTO(empresas.get(0));
        }

        throw new ResourceNotFoundException(
                "Empresa no encontrada con identificador: " + identificador);
    }

    /* MÉTODOS NO USADOS EN NINGUN LADO */

    /* Validar que un usuario tiene acceso a una empresa */
    @Transactional(readOnly = true)
    public boolean usuarioTieneAccesoAEmpresa(Long usuarioId, Long empresaId) {
        // Esta lógica se implementará cuando tengamos el servicio de usuarios
        // Por ahora, devolvemos true para permitir el desarrollo
        return true;
    }

}