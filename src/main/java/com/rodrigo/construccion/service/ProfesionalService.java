package com.rodrigo.construccion.service;

import com.rodrigo.construccion.config.TenantContext;
import com.rodrigo.construccion.dto.mapper.ProfesionalMapper;
import com.rodrigo.construccion.dto.request.ProfesionalRequestDTO;
import com.rodrigo.construccion.enums.CategoriaProfesional;
import com.rodrigo.construccion.enums.TipoProfesional;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.dto.request.AsignarProfesionalRequest;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.repository.ProfesionalRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfesionalService implements IProfesionalService {

    private final ProfesionalRepository profesionalRepository;
    private final ProfesionalMapper profesionalMapper;
    private final IEmpresaService empresaService;

    /* Crear nuevo profesional desde DTO */
    @Override
    public ProfesionalResponseDTO crearProfesional(ProfesionalRequestDTO requestDTO) {
        // Validar rol personalizado si el tipo es "Otro (personalizado)"
        validarRolPersonalizado(requestDTO.getTipoProfesional(), requestDTO.getRolPersonalizado());

        Profesional profesional = profesionalMapper.toEntity(requestDTO);

        // Setear la empresa: priorizar el del body, si no existe, usar el del contexto
        Long empresaIdFromBody = requestDTO.getEmpresaId();
        Long empresaIdFromContext = TenantContext.getTenantId();
        final Long empresaId = empresaIdFromBody != null ? empresaIdFromBody : empresaIdFromContext;

        if (empresaId != null) {
            Empresa empresa = empresaService.findEmpresaById(empresaId);
            empresa.getId();
            profesional.setEmpresa(empresa);
        }

        // Lógica flexible para tipo de profesional
        TipoProfesional tipoEnum = TipoProfesional.fromDisplayName(requestDTO.getTipoProfesional());
        if (tipoEnum != null) {
            profesional.setTipoProfesional(tipoEnum.getDisplayName());
        } else {
            profesional.setTipoProfesional(requestDTO.getTipoProfesional());
        }

        if (profesional.getActivo() == null) {
            profesional.setActivo(true);
        }

        if (profesional.getValorHoraDefault() == null) {
            profesional.setValorHoraDefault(BigDecimal.ZERO);
        }

        // Validar y setear categoría (EMPLEADO por default)
        validarYSetearCategoria(profesional, requestDTO.getCategoria());

        return profesionalMapper.toResponseDTO(profesionalRepository.save(profesional));
    }

    /**
     * Valida y establece la categoría del profesional.
     * Si no se proporciona, defaultea a EMPLEADO.
     */
    private void validarYSetearCategoria(Profesional profesional, String categoriaString) {
        if (categoriaString != null && !categoriaString.trim().isEmpty()) {
            // Validar que sea una categoría válida
            if (!CategoriaProfesional.esValida(categoriaString)) {
                throw new IllegalArgumentException(
                    "Categoría inválida: " + categoriaString + 
                    ". Valores permitidos: EMPLEADO, INDEPENDIENTE, CONTRATISTA"
                );
            }
            profesional.setCategoria(categoriaString.trim().toUpperCase());
        } else {
            // Default a EMPLEADO
            profesional.setCategoria(CategoriaProfesional.EMPLEADO.getValor());
        }
    }

    /* Actualizar profesional existente desde DTO */
    @Override
    public ProfesionalResponseDTO actualizar(Long id, ProfesionalRequestDTO requestDTO) {
        // Validar rol personalizado si el tipo es "Otro (personalizado)"
        validarRolPersonalizado(requestDTO.getTipoProfesional(), requestDTO.getRolPersonalizado());

        return profesionalRepository.findById(id)
                .map(profesional -> {
                    profesionalMapper.updateEntity(requestDTO, profesional);
                    // Si se actualiza el tipo, usar el enum para validar y estandarizar
                    if (requestDTO.getTipoProfesional() != null && !requestDTO.getTipoProfesional().isBlank()) {
                        TipoProfesional tipoEnum = TipoProfesional.fromDisplayName(requestDTO.getTipoProfesional());
                        if (tipoEnum != null) {
                            // Si el tipo existe, estandarizamos
                            profesional.setTipoProfesional(tipoEnum.getDisplayName());
                        } else {
                            // Si es un tipo nuevo, lo aceptamos como texto
                            profesional.setTipoProfesional(requestDTO.getTipoProfesional());
                        }
                    }

                    if (requestDTO.getCuit() != null) {
                        profesional.setCuit(requestDTO.getCuit());
                    }

                    // Validar y actualizar categoría si viene en el request
                    if (requestDTO.getCategoria() != null) {
                        validarYSetearCategoria(profesional, requestDTO.getCategoria());
                    }

                    Profesional profesionalGuardado = profesionalRepository.save(profesional);

                    return profesionalMapper.toResponseDTO(profesionalGuardado);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con ID: " + id));
    }

    /* Valida que el rol personalizado esté presente cuando el tipo de profesional es "Otro (personalizado) */
    private void validarRolPersonalizado(String tipoProfesional, String rolPersonalizado) {
        if (tipoProfesional != null && tipoProfesional.trim().equalsIgnoreCase("Otro (personalizado)")) {
            if (rolPersonalizado == null || rolPersonalizado.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "El campo 'Rol Personalizado' es obligatorio cuando el tipo de profesional es 'Otro (personalizado)'"
                );
            }
        }
    }

    /* Obtener todos los profesionales */
    @Override
    public List<ProfesionalResponseDTO> obtenerTodos() {
        List<Profesional> profesionales = profesionalRepository.findAll();
        return profesionalMapper.toResponseDTOList(profesionales);
    }

    /* Obtener profesional por ID - Usado tambien en ProfesionalObraService */
    @Override
    public Profesional obtenerPorId(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con ID: " + id));
    }

    /* Obtener profesional por ID - Usado en ProfesionalController */
    @Override
    public ProfesionalResponseDTO obtenerProfesionalPorId(Long id) {
        Profesional profesionalEncontrado = profesionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con ID: " + id));
        return profesionalMapper.toResponseDTO(profesionalEncontrado);
    }

    /* Buscar profesionales por tipo con búsqueda flexible */
    @Override
    public List<ProfesionalResponseDTO> buscarPorTipo(String tipoProfesional) {

        // Normalizar el input del usuario
        String tipoNormalizado = normalizarTipoProfesional(tipoProfesional);

        // Primero intentar búsqueda exacta (ignorando mayúsculas)
        List<Profesional> resultadosExactos = profesionalRepository.findByTipoProfesionalIgnoreCase(tipoNormalizado);

        if (!resultadosExactos.isEmpty())
            return profesionalMapper.toResponseDTOList(resultadosExactos);

        // Si no hay resultados exactos, usar búsqueda flexible
        List<Profesional> resultadosFlexibles = profesionalRepository.buscarPorTipoFlexible(tipoNormalizado);

        return profesionalMapper.toResponseDTOList(resultadosFlexibles);
    }

    /* Normaliza el tipo de profesional para mejorar las búsquedas */
    private String normalizarTipoProfesional(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return tipo;
        }

        String tipoLimpio = tipo.trim();

        // Capitalizar primera letra
        return tipoLimpio.substring(0, 1).toUpperCase() +
                tipoLimpio.substring(1).toLowerCase();
    }

    /* Eliminar profesional */
    @Override
    public void eliminar(Long id) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con ID: " + id));
        profesionalRepository.delete(profesional);
    }

    /* Actualizar valorHoraDefault de todos los profesionales por porcentaje */
    @Override
    public void actualizarValorHoraTodosPorPorcentaje(double porcentaje) {
        List<Profesional> profesionales = profesionalRepository.findAll();
        for (Profesional profesional : profesionales) {
            BigDecimal actual = profesional.getValorHoraDefault();
            BigDecimal nuevo = actual
                    .add(actual.multiply(BigDecimal.valueOf(porcentaje).divide(BigDecimal.valueOf(100))));
            profesional.setValorHoraDefault(nuevo);
        }
        profesionalRepository.saveAll(profesionales);
    }

    /* Actualizar valorHoraDefault de un profesional por ID y por porcentaje */
    @Override
    public void actualizarValorHoraPorIdPorPorcentaje(Long id, double porcentaje) {
        // Buscamos el profesional. Si no existe, el método orElseThrow lanzará la excepción.
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con ID: " + id));

        // Delegamos la lógica de cálculo a la entidad.
        profesional.actualizarValorHoraPorPorcentaje(porcentaje);

        profesionalRepository.save(profesional);
    }

    /* Actualizar porcentajeGanancia de todos los profesionales y calcular importeGanancia
     */
    @Override
    public void actualizarPorcentajeGananciaTodos(double porcentaje) {
        List<Profesional> profesionales = profesionalRepository.findAll();
        BigDecimal porcentajeBD = BigDecimal.valueOf(porcentaje);
        for (Profesional profesional : profesionales) {
            profesional.setPorcentajeGanancia(porcentajeBD);
            BigDecimal importe = profesional.getValorHoraDefault()
                    .multiply(porcentajeBD)
                    .divide(BigDecimal.valueOf(100));
            profesional.setImporteGanancia(importe);
        }
        profesionalRepository.saveAll(profesionales);
    }

    /* Actualizar porcentajeGanancia de un profesional por ID y calcular importeGanancia */
    @Override
    public void actualizarPorcentajeGananciaPorId(Long id, double porcentaje) {
        // 1. Buscamos el profesional. Si no existe, lanzará una excepción clara.
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con ID: " + id));

        // 2. Delegamos la lógica de cálculo a la entidad Profesional.
        profesional.actualizarGanancia(BigDecimal.valueOf(porcentaje));

        // 3. Guardamos los cambios. Gracias a @Transactional, esto podría ser opcional,
        // pero es una buena práctica ser explícito.
        profesionalRepository.save(profesional);
    }

    /**
     * Busca un profesional para ser asignado, priorizando por ID y luego por tipo/nombre.
     * Usado por ProfesionalObraService para encapsular la lógica de búsqueda.
     */
    @Override
    @Transactional(readOnly = true)
    public Profesional findProfesionalParaAsignacion(AsignarProfesionalRequest request) {
        if (request.getProfesionalId() != null) {
            return profesionalRepository.findById(request.getProfesionalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró un profesional con el ID: " + request.getProfesionalId()));
        } else {
            if (request.getTipoProfesional() == null || request.getTipoProfesional().isBlank() ||
                    request.getNombre() == null || request.getNombre().isBlank()) {
                throw new IllegalArgumentException(
                        "Si no se proporciona 'profesionalId', los campos 'Tipo Profesional' y 'nombre' son obligatorios.");
            }
            // Se usa el tipo de profesional directamente, ya que ahora es flexible.

            return profesionalRepository
                    .findByTipoProfesionalAndNombreAndActivoTrue(request.getTipoProfesional(), request.getNombre())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("No existe ningún profesional de tipo '%s' con el nombre '%s'",
                                    request.getTipoProfesional(), request.getNombre())));
        }
    }

    /* Busca profesionales activos por tipo de forma flexible, encapsulando la lógica de variaciones de género. */
    @Override
    @Transactional(readOnly = true)
    public List<Profesional> buscarActivosPorTipoFlexible(String tipoProfesional) {
        return profesionalRepository.findByTipoProfesionalIgnoreCaseAndActivoTrue(tipoProfesional);
    }

    /* Metodo usado en ProfesionalObraService */
    @Override
    public List<Profesional> buscarPorTipoProfesionalActivos(String tipoProfesional) {
        List<Profesional> profesionalesEncontrados = profesionalRepository
                .findByTipoProfesionalAndActivoTrue(tipoProfesional);

        if (profesionalesEncontrados.isEmpty())
            throw new ResourceNotFoundException(
                    String.format("No se encontraron profesionales activos de tipo '%s'", tipoProfesional));

        return profesionalesEncontrados;
    }

    /* Metodo usado en ProfesionalObraService */
    @Override
    public Profesional findFirstActivoByTipo(String tipoProfesional) {
        return profesionalRepository
                .findByTipoProfesionalAndActivoTrue(tipoProfesional)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontraron profesionales activos del tipo '" + tipoProfesional + "'."));
    }

    /* Metodo para devolver entidades directamente a otros servicios */
    @Override
    public List<Profesional> findAllEntities() {
        return profesionalRepository.findAll();
    }

    @Override
    public Page<Profesional> findAllWithHonorarios(Pageable pageable, Long empresaId) {
        return profesionalRepository.findProfesionalesWithHonorarios(pageable, empresaId);
    }

    /* Buscar profesionales por nombre (contiene) */
    @Override
    public List<ProfesionalResponseDTO> buscarPorNombre(String nombre) {
        List<Profesional> profesionalesEncontrados = profesionalRepository.findByNombreContaining(nombre);
        return profesionalMapper.toResponseDTOList(profesionalesEncontrados);
    }

    /* Buscar profesionales por categoría (solo activos) */
    @Override
    @Transactional(readOnly = true)
    public List<ProfesionalResponseDTO> buscarPorCategoria(String categoria) {
        // Validar que la categoría sea válida
        if (!CategoriaProfesional.esValida(categoria)) {
            throw new IllegalArgumentException(
                "Categoría inválida: " + categoria + 
                ". Valores permitidos: EMPLEADO, INDEPENDIENTE, CONTRATISTA"
            );
        }
        
        List<Profesional> profesionales = profesionalRepository.findByCategoriaAndActivoTrue(categoria.toUpperCase());
        return profesionalMapper.toResponseDTOList(profesionales);
    }

    /* Buscar profesionales por categoría y empresaId (solo activos) */
    @Override
    @Transactional(readOnly = true)
    public List<ProfesionalResponseDTO> buscarPorCategoriaYEmpresaId(String categoria, Long empresaId) {
        // Validar que la categoría sea válida
        if (!CategoriaProfesional.esValida(categoria)) {
            throw new IllegalArgumentException(
                "Categoría inválida: " + categoria + 
                ". Valores permitidos: EMPLEADO, INDEPENDIENTE, CONTRATISTA"
            );
        }
        
        List<Profesional> profesionales = profesionalRepository.findByCategoriaAndEmpresaIdAndActivoTrue(
            categoria.toUpperCase(), 
            empresaId
        );
        return profesionalMapper.toResponseDTOList(profesionales);
    }

}