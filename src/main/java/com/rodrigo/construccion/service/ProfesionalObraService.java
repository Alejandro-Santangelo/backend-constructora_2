package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.ActualizarAsignacionRequest;
import com.rodrigo.construccion.enums.RolEnObra;
import com.rodrigo.construccion.dto.request.AsignarProfesionalRequest;
import com.rodrigo.construccion.dto.request.AsignarProfesionalesBatchRequest;
import com.rodrigo.construccion.dto.mapper.ProfesionalMapper;
import com.rodrigo.construccion.dto.request.SolicitudProfesionalesRequest;
import com.rodrigo.construccion.dto.request.SolicitudTipoProfesionalRequest;
import com.rodrigo.construccion.dto.response.AsignacionProfesionalResponse;
import com.rodrigo.construccion.dto.mapper.ProfesionalObraMapper;
import com.rodrigo.construccion.dto.response.DisponibilidadProfesionalResponse;
import com.rodrigo.construccion.dto.response.ListaProfesionalesResponse;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.repository.ProfesionalObraRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Asignaciones Profesional-Obra
 * Maneja la lógica de negocio para asignaciones de profesionales a obras
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfesionalObraService implements IProfesionalObraService {

    private final ProfesionalObraRepository profesionalObraRepository;
    private final IEmpresaService empresaService;
    private final ProfesionalObraMapper profesionalObraMapper;
    private final IProfesionalService profesionalService;
    private final ProfesionalMapper profesionalMapper;
    private final IObraService obraService;

    /**
     * Obtener todas las asignaciones como DTOs - USADO EN CONTROLLER
     */
    @Override
    public List<AsignacionProfesionalResponse> obtenerTodasComoDTO() {
        List<ProfesionalObra> asignaciones = profesionalObraRepository.findAll();
        return asignaciones.stream()
                .map(profesionalObraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener todas las asignaciones filtradas por empresa - USADO EN CONTROLLER
     */
    @Override
    public List<AsignacionProfesionalResponse> obtenerTodasPorEmpresa(Long empresaId) {
        List<ProfesionalObra> asignaciones = profesionalObraRepository.findByEmpresaId(empresaId);
        return asignaciones.stream()
                .map(profesionalObraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Asignar profesional por tipo y retornar DTO - USADO EN CONTROLLER
     */
    @Transactional
    @Override
    public AsignacionProfesionalResponse asignarProfesionalPorTipoComoDTO(AsignarProfesionalRequest request) {
        log.info("🔍 Iniciando asignación de profesional - Empresa: {}, Obra: {}, Tipo: {}, Nombre: {}", 
                request.empresaId, request.getObraId(), request.getTipoProfesional(), request.getNombre());
        
        try {
            // 1. Validar que la empresa y la obra existen
            empresaService.findEmpresaById(request.empresaId);
            Obra obra = obraService.findById(request.getObraId());
            log.info("✅ Empresa y obra validadas correctamente");

            // 2. Buscar al profesional delegando la lógica a ProfesionalService
            Profesional profesional = profesionalService.findProfesionalParaAsignacion(request);
            log.info("✅ Profesional encontrado: {} (ID: {})", profesional.getNombre(), profesional.getId());

        // 3. TODO: ADAPTAR - Validar que el profesional no esté ya asignado usando dirección
        // boolean yaAsignado = profesionalObraRepository
        //         .existsByProfesional_IdAndObra_IdAndActivoTrue(profesional.getId(), obra.getId());
        //
        // if (yaAsignado) {
        //     throw new RuntimeException(
        //             String.format("El profesional '%s' ya está asignado a la obra '%s'",
        //                     profesional.getNombre(), obra.getNombre()));
        // }

        // 4. Crear la nueva entidad de asignación
        ProfesionalObra asignacion = new ProfesionalObra();
        asignacion.setProfesional(profesional);
        // Copiar los 4 campos de dirección desde Obra
        asignacion.setDireccionObraCalle(obra.getDireccionObraCalle());
        asignacion.setDireccionObraAltura(obra.getDireccionObraAltura() != null ? obra.getDireccionObraAltura() : "");
        asignacion.setDireccionObraPiso(obra.getDireccionObraPiso());
        asignacion.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
        asignacion.setEmpresaId(obra.getEmpresaId());
        asignacion.setFechaDesde(request.getFechaDesde() != null ? request.getFechaDesde() : LocalDate.now());
        asignacion.setFechaHasta(request.getFechaHasta());
        // Se valida y estandariza el rol usando el enum.
        if (request.getRolEnObra() != null && !request.getRolEnObra().isBlank()) {
            try {
                asignacion.setRolEnObraEnum(RolEnObra.fromDisplayName(request.getRolEnObra()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Rol '{}' no reconocido, usando ROL_OFICIAL por defecto", request.getRolEnObra());
                asignacion.setRolEnObraEnum(RolEnObra.ROL_OFICIAL);
            }
        }
        asignacion.setValorHoraAsignado(
                request.getValorHoraAsignado() != null ? BigDecimal.valueOf(request.getValorHoraAsignado())
                        : profesional.getValorHoraDefault());
        asignacion.setActivo(request.getActivo() != null ? request.getActivo() : true);

        // 5. Guardar la asignación en la base de datos
        log.info("💾 Guardando asignación en base de datos...");
        ProfesionalObra asignacionGuardada = profesionalObraRepository.save(asignacion);
        log.info("✅ Asignación guardada con ID: {}", asignacionGuardada.getId());

        // 6. Mapear la entidad guardada a un DTO de respuesta y retornarla
        AsignacionProfesionalResponse response = profesionalObraMapper.toResponseDTO(asignacionGuardada);
        log.info("🎯 Asignación completada exitosamente - ID: {}", response.idAsignacion);
        return response;
        
        } catch (Exception e) {
            log.error("❌ Error en asignación de profesional: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Asignar múltiples profesionales a una obra en lote - USADO EN CONTROLLER
     */
    @Transactional
    @Override
    public List<AsignacionProfesionalResponse> asignarMultiplesProfesionales(AsignarProfesionalesBatchRequest request) {
        return request.getProfesionalesIds().stream()
                .map(profesionalId -> {
                    // TODO: ADAPTAR - Verificar si el profesional ya está asignado usando dirección
                    // boolean yaAsignado = profesionalObraRepository
                    //         .existsByProfesional_IdAndObra_IdAndActivoTrue(profesionalId, request.getObraId());
                    // if (yaAsignado) {
                    //     return null; // Si ya está asignado, lo omitimos.
                    // }

                    // 2. Crear la solicitud de asignación individual.
                    AsignarProfesionalRequest asignacionIndividualRequest = new AsignarProfesionalRequest();
                    asignacionIndividualRequest.setEmpresaId(request.getEmpresaId());
                    asignacionIndividualRequest.setObraId(request.getObraId());
                    asignacionIndividualRequest.setProfesionalId(profesionalId);

                    // 3. Reutilizar la lógica de asignación principal.
                    return asignarProfesionalPorTipoComoDTO(asignacionIndividualRequest);
                })
                .filter(java.util.Objects::nonNull) // Filtrar los profesionales que se omitieron (nulos).
                .collect(Collectors.toList());
    }

    @Override
    public List<AsignacionProfesionalResponse> obtenerAsignacionesPorTipo(String tipoProfesional, Long empresaId) {

        empresaService.findEmpresaById(empresaId);
        // Optimización: Se mantiene la lógica de variación de género pero se consolida
        // en una única consulta a la base de datos para mejorar el rendimiento.
        String terminoBase = tipoProfesional.trim();
        String terminoAlterno = "";

        // Lógica mejorada para generar la variación de género en términos compuestos.
        // Ejemplo: "Maestro Mayor" -> "Maestra Mayor"
        String[] palabras = terminoBase.split(" ");
        String ultimaPalabra = palabras[palabras.length - 1];
        String baseSinUltimaPalabra = terminoBase.substring(0, terminoBase.length() - ultimaPalabra.length()).trim();

        if (ultimaPalabra.toLowerCase().endsWith("o")) {
            String ultimaPalabraAlterna = ultimaPalabra.substring(0, ultimaPalabra.length() - 1) + "a";
            terminoAlterno = (baseSinUltimaPalabra + " " + ultimaPalabraAlterna).trim();
        } else if (ultimaPalabra.toLowerCase().endsWith("a")) {
            String ultimaPalabraAlterna = ultimaPalabra.substring(0, ultimaPalabra.length() - 1) + "o";
            terminoAlterno = (baseSinUltimaPalabra + " " + ultimaPalabraAlterna).trim();
        } else {
            terminoAlterno = terminoBase; // Si no termina en 'o' o 'a', no hay variación que generar.
        }

        // TODO: ADAPTAR - Llamar a método que no dependa de relación FK con Obra
        // List<ProfesionalObra> asignaciones = profesionalObraRepository.buscarPorTipoFlexible(terminoBase,
        //         terminoAlterno,
        //         empresaId);
        
        // Por ahora retornar lista vacía
        return new ArrayList<>();

        /* CÓDIGO ORIGINAL COMENTADO:
        if (asignaciones.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format(
                            "No se encontraron asignaciones para el tipo de profesional '%s' en la empresa con ID %d",
                            tipoProfesional, empresaId));
        }

        return profesionalObraMapper.toResponseDTOList(asignaciones);
        */
    }

    /**
     * Obtener disponibilidad de profesionales por tipo
     * Búsqueda flexible: case-insensitive y con variaciones de género - USADO EN
     * CONTROLLER
     */
    @Override
    public List<DisponibilidadProfesionalResponse> obtenerDisponibilidadPorTipo(String tipoProfesional,
            Long empresaId) {
        // 1. Validar que la empresa existe. El filtrado se delega al
        // ProfesionalService.
        empresaService.findEmpresaById(empresaId);

        // 2. Delegar la búsqueda de profesionales al servicio correspondiente.
        // Esto respeta la arquitectura de capas y centraliza la lógica de negocio.
        List<Profesional> profesionales = profesionalService.buscarActivosPorTipoFlexible(tipoProfesional);

        // 3. Usar el mapper para convertir la lista de entidades a DTOs de
        // disponibilidad.
        // Esto elimina el bucle y las consultas N+1.
        return profesionalMapper.toDisponibilidadDtoList(profesionales);
    }

    /**
     * Obtiene una lista de profesionales por tipo con su estado de disponibilidad.
     * Este método está optimizado para evitar el problema N+1. - USADO EN
     * CONTROLLER
     */
    @Override
    public ListaProfesionalesResponse obtenerDisponibilidadProfesionalesPorTipo(String tipoProfesional,
            Long empresaId) {
        // Se valida la empresa para mantener la lógica de negocio, aunque el filtro
        // principal es por tipo.
        empresaService.findEmpresaById(empresaId);

        // 1. Se delega la búsqueda al servicio de profesionales, que ahora es más
        // eficiente.
        List<Profesional> profesionales = profesionalService.buscarActivosPorTipoFlexible(tipoProfesional);

        List<DisponibilidadProfesionalResponse> dtoList = profesionalMapper.toDisponibilidadDtoList(profesionales);

        // Corrección del bug: Se ajusta el tipo de profesional en la respuesta para que
        // sea consistente
        // con los resultados encontrados, en lugar de simplemente repetir el término de
        // búsqueda.
        String tipoProfesionalEncontrado = dtoList.isEmpty() ? tipoProfesional : dtoList.get(0).getTipoProfesional();

        // 3. Se construye el objeto de respuesta final con el formato requerido.
        return new ListaProfesionalesResponse(dtoList.size(), tipoProfesionalEncontrado, dtoList);
    }

    /**
     * Actualizar asignación y retornar DTO - USADO EN CONTROLLER
     */
    @Override
    public AsignacionProfesionalResponse actualizarAsignacionComoDTO(Long asignacionId,
            ActualizarAsignacionRequest request, Long empresaId) {
        ProfesionalObra asignacionActualizada = actualizarAsignacion(asignacionId, request, empresaId);
        // La responsabilidad de traducir la excepción de la base de datos a un mensaje
        // amigable se delega al
        // GlobalExceptionHandler.
        return profesionalObraMapper.toResponseDTO(asignacionActualizada);

    }

    /**
     * Desactiva (marca como inactiva) una asignación delegando a
     * {@link #actualizarAsignacion(Long, ActualizarAsignacionRequest, Long)}.
     * Este método encapsula la intención de negocio "desactivar" y hace más
     * claro el uso desde el controlador.
     */
    @Transactional
    public AsignacionProfesionalResponse desactivarAsignacion(Long asignacionId, Long empresaId) {
        return desactivarAsignacion(asignacionId, empresaId, null);
    }

    @Override
    @Transactional
    public AsignacionProfesionalResponse desactivarAsignacion(Long asignacionId, Long empresaId, Long obraId) {
        if (asignacionId == null)
            throw new IllegalArgumentException("El ID de la asignación es obligatorio");

        ActualizarAsignacionRequest request = new ActualizarAsignacionRequest();
        request.setActivo(false);
        if (obraId != null) {
            request.setObraId(obraId);
        }

        ProfesionalObra asignacionDesactivada = actualizarAsignacion(asignacionId, request, empresaId);

        return profesionalObraMapper.toResponseDTO(asignacionDesactivada);
    }

    /**
     * Actualizar una asignación existente - este metodo lo usa los dos metodos de
     * arriba
     */
    @Transactional
    public ProfesionalObra actualizarAsignacion(Long asignacionId, ActualizarAsignacionRequest request,
            Long empresaId) {
        // --- MEJORA DE SEGURIDAD: Validación de empresaId ---
        if (empresaId == null) {
            throw new IllegalArgumentException("El ID de la empresa es obligatorio para actualizar una asignación.");
        }

        // Buscar la asignación existente (el filtro de Hibernate ya filtra por empresaId)
        ProfesionalObra asignacionExistente = profesionalObraRepository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("Asignación no encontrada con ID: %d para la empresa: %d", asignacionId, empresaId)));

        // --- LÓGICA DE ACTUALIZACIÓN OPTIMIZADA ---

        if (request.getProfesional() != null && !request.getProfesional().trim().isEmpty()) {
            // Se delega la búsqueda del profesional al servicio correspondiente.
            Profesional nuevoProfesional = profesionalService.findFirstActivoByTipo(request.getProfesional());
            asignacionExistente.setProfesional(nuevoProfesional);
        }

        if (request.getObraId() != null) {
            // Valida y copia los 4 campos de dirección desde la nueva obra
            Obra nuevaObra = obraService.findById(request.getObraId());
            asignacionExistente.setDireccionObraCalle(nuevaObra.getDireccionObraCalle());
            asignacionExistente.setDireccionObraAltura(nuevaObra.getDireccionObraAltura());
            asignacionExistente.setDireccionObraPiso(nuevaObra.getDireccionObraPiso());
            asignacionExistente.setDireccionObraDepartamento(nuevaObra.getDireccionObraDepartamento());
        }

        // Actualización de campos simples
        if (request.getFechaDesde() != null) {
            asignacionExistente.setFechaDesde(request.getFechaDesde());
        }

        if (request.getFechaHasta() != null) {
            asignacionExistente.setFechaHasta(request.getFechaHasta());
        }

        if (request.getRolEnObra() != null) {
            if (!request.getRolEnObra().isBlank()) {
                asignacionExistente.setRolEnObraEnum(RolEnObra.fromDisplayName(request.getRolEnObra()));
            }
        }

        if (request.getValorHoraAsignado() != null) {
            asignacionExistente.setValorHoraAsignado(BigDecimal.valueOf(request.getValorHoraAsignado()));
        }

        if (request.getActivo() != null) {
            asignacionExistente.setActivo(request.getActivo());
        }

        // Guardar y forzar la sincronización. Si hay un error de duplicado,
        // la excepción se propagará hacia arriba y será capturada por el método que lo
        // llamó.
        profesionalObraRepository.saveAndFlush(asignacionExistente);

        return asignacionExistente;
    }

    /**
     * ============================================
     * MÉTODOS DE DEPURACIÓN (TEMPORALES) - USADOS EN CONTROLLER Y EN PRESUPUESTOS
     * ============================================
     */

    /**
     * Obtener todos los profesionales para debug
     */
    @Override
    public List<Profesional> obtenerTodosProfesionales() {
        return profesionalService.findAllEntities();
    }

    /**
     * Obtener todas las asignaciones para debug
     */
    @Override
    public List<ProfesionalObra> obtenerTodasLasAsignaciones() {
        return profesionalObraRepository.findAll();
    }

    /**
     * Obtener profesionales asignados a una obra de una empresa
     * TODO: ADAPTAR - Usar búsqueda por dirección
     */
    @Override
    public List<ProfesionalResponseDTO> obtenerProfesionalesPorObraYEmpresa(Long empresaId, Long obraId) {
        empresaService.findEmpresaById(empresaId);
        obraService.findById(obraId);
        // List<ProfesionalObra> asignaciones = profesionalObraRepository.findByEmpresaIdAndObraId(empresaId, obraId);
        // return profesionalObraMapper.toProfesionalResponseDTOList(asignaciones);
        return new ArrayList<>();
    }

    /**
     * Obtener asignación por ID - Se esta usando en JornalService
     */
    @Override
    public ProfesionalObra obtenerPorId(Long id) {
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));
        return profesionalObra;
    }

    /**
     * Buscar asignación específica profesional-obra | Usado en HonorarioService
     * TODO: ADAPTAR - Usar búsqueda por dirección
     */
    @Override
    public ProfesionalObra buscarAsignacionEspecifica(Long profesionalId, Long obraId) {
        // return profesionalObraRepository.findByProfesional_IdAndObra_IdAndActivoTrue(profesionalId, obraId).orElse(null);
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar búsqueda por dirección");
    }

    /*
     * ============================================
     * METODOS QUE NO SE ESTÁN USANDO LITERALMENTE EN NINGÚN LADO
     * ============================================
     */

    /*
     * Obtener todas las asignaciones
     */
    public List<ProfesionalObra> obtenerTodas() {
        return profesionalObraRepository.findAll();
    }

    /**
     * Obtener asignaciones con paginación
     */
    public Page<ProfesionalObra> obtenerTodasPaginadas(Pageable pageable) {
        return profesionalObraRepository.findAll(pageable);
    }

    /**
     * Crear nueva asignación
     */
    public ProfesionalObra crear(ProfesionalObra asignacion) {
        return profesionalObraRepository.save(asignacion);
    }

    /**
     * Actualizar asignación existente
     */
    public ProfesionalObra actualizar(Long id, ProfesionalObra asignacionActualizada) {
        return profesionalObraRepository.findById(id)
                .map(asignacion -> {
                    // Actualizar campos disponibles
                    if (asignacionActualizada.getFechaDesde() != null) {
                        asignacion.setFechaDesde(asignacionActualizada.getFechaDesde());
                    }
                    if (asignacionActualizada.getFechaHasta() != null) {
                        asignacion.setFechaHasta(asignacionActualizada.getFechaHasta());
                    }
                    if (asignacionActualizada.getRolEnObra() != null) {
                        asignacion.setRolEnObra(asignacionActualizada.getRolEnObra());
                    }
                    if (asignacionActualizada.getValorHoraAsignado() != null) {
                        asignacion.setValorHoraAsignado(asignacionActualizada.getValorHoraAsignado());
                    }
                    if (asignacionActualizada.getActivo() != null) {
                        asignacion.setActivo(asignacionActualizada.getActivo());
                    }

                    return profesionalObraRepository.save(asignacion);
                })
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));
    }

    /**
     * Eliminar asignación
     */
    public void eliminar(Long id) {
        if (profesionalObraRepository.existsById(id)) {
            profesionalObraRepository.deleteById(id);
        } else {
            throw new RuntimeException("Asignación no encontrada con ID: " + id);
        }
    }

    /**
     * Asignar profesional por ID y retornar DTO
     */
    public AsignacionProfesionalResponse asignarProfesionalPorIdComoDTO(AsignarProfesionalRequest request) {
        ProfesionalObra asignacion = asignarProfesionalPorId(request);
        return profesionalObraMapper.toResponseDTO(asignacion);
    }

    /**
     * Asignar profesional por ID (implementación básica)
     */
    public ProfesionalObra asignarProfesionalPorId(AsignarProfesionalRequest request) {
        // Validaciones y búsquedas de relaciones
        Profesional profesional = profesionalService.obtenerPorId(request.getProfesionalId());
        Obra obra = obraService.findById(request.getObraId());

        // Usar el mapper para copiar campos simples desde el request
        ProfesionalObra asignacion = profesionalObraMapper.fromRequest(request);

        // Completar relaciones y valores por defecto
        asignacion.setProfesional(profesional);
        // Copiar los 4 campos de dirección desde Obra
        asignacion.setDireccionObraCalle(obra.getDireccionObraCalle());
        asignacion.setDireccionObraAltura(String.valueOf(obra.getDireccionObraAltura()));
        asignacion.setDireccionObraPiso(obra.getDireccionObraPiso());
        asignacion.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
        asignacion.setEmpresaId(obra.getEmpresaId());

        if (asignacion.getFechaDesde() == null) {
            asignacion.setFechaDesde(LocalDate.now());
        }

        if (asignacion.getValorHoraAsignado() == null && profesional.getValorHoraDefault() != null) {
            asignacion.setValorHoraAsignado(profesional.getValorHoraDefault());
        }

        if (asignacion.getActivo() == null) {
            asignacion.setActivo(true);
        }

        return profesionalObraRepository.save(asignacion);
    }

    /**
     * Buscar asignaciones por profesional
     * TODO: ADAPTAR - Este método ya no existe en el repositorio
     */
    public List<ProfesionalObra> buscarPorProfesional(Long profesionalId) {
        // return profesionalObraRepository.findByProfesional_Id(profesionalId);
        throw new UnsupportedOperationException("Método pendiente de adaptación");
    }

    /**
     * Buscar asignaciones activas por profesional
     */
    public List<ProfesionalObra> buscarActivasPorProfesional(Long profesionalId) {
        return profesionalObraRepository.findByProfesional_IdAndActivoTrue(profesionalId);
    }

    /**
     * Buscar asignaciones por obra
     * TODO: ADAPTAR - Este método ya no existe, usar búsqueda por dirección
     */
    public List<ProfesionalObra> buscarPorObra(Long obraId) {
        // return profesionalObraRepository.findByObra_Id(obraId);
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar búsqueda por dirección");
    }

    /**
     * Buscar asignaciones activas por obra
     * TODO: ADAPTAR - Este método ya no existe, usar búsqueda por dirección
     */
    public List<ProfesionalObra> buscarActivasPorObra(Long obraId) {
        // return profesionalObraRepository.findByObra_IdAndActivoTrue(obraId);
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar búsqueda por dirección");
    }

    /**
     * Buscar asignaciones por tipo de profesional
     * TODO: ADAPTAR - Este método ya no existe en el repositorio
     */
    public List<ProfesionalObra> buscarPorTipoProfesional(String tipoProfesional) {
        // return profesionalObraRepository.findByTipoProfesional(tipoProfesional);
        throw new UnsupportedOperationException("Método pendiente de adaptación");
    }

    /**
     * Buscar asignaciones activas por tipo de profesional
     * TODO: ADAPTAR - Este método ya no existe en el repositorio
     */
    public List<ProfesionalObra> buscarActivasPorTipoProfesional(String tipoProfesional) {
        // return profesionalObraRepository.findByTipoProfesionalAndActivoTrue(tipoProfesional);
        throw new UnsupportedOperationException("Método pendiente de adaptación");
    }

    /**
     * Verificar si existe asignación activa
     * TODO: ADAPTAR - Este método ya no existe, usar búsqueda por dirección
     */
    public boolean existeAsignacionActiva(Long profesionalId, Long obraId) {
        // return profesionalObraRepository.existsByProfesional_IdAndObra_IdAndActivoTrue(profesionalId, obraId);
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar búsqueda por dirección");
    }

    /**
     * Buscar asignaciones por rango de fechas
     */
    public List<ProfesionalObra> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return profesionalObraRepository.findAll().stream()
                .filter(asignacion -> {
                    LocalDate desde = asignacion.getFechaDesde();
                    LocalDate hasta = asignacion.getFechaHasta();
                    return desde != null &&
                            !desde.isAfter(fechaFin) &&
                            (hasta == null || !hasta.isBefore(fechaInicio));
                })
                .toList();
    }

    /**
     * Buscar asignaciones por rol
     */
    public List<ProfesionalObra> buscarPorRol(String rol) {
        return profesionalObraRepository.findAll().stream()
                .filter(asignacion -> rol.equals(asignacion.getRolEnObra()))
                .toList();
    }

    /**
     * Obtener asignaciones activas
     */
    public List<ProfesionalObra> obtenerAsignacionesActivas() {
        return profesionalObraRepository.findAll().stream()
                .filter(asignacion -> Boolean.TRUE.equals(asignacion.getActivo()))
                .toList();
    }

    /**
     * Desactivar asignación
     */
    public ProfesionalObra desactivarAsignacion(Long id) {
        return profesionalObraRepository.findById(id)
                .map(asignacion -> {
                    asignacion.setActivo(false);
                    asignacion.setFechaHasta(LocalDate.now());
                    return profesionalObraRepository.save(asignacion);
                })
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));
    }

    /**
     * Obtener estadísticas básicas
     */
    public long contarTotal() {
        return profesionalObraRepository.count();
    }

    public long contarActivas() {
        return obtenerAsignacionesActivas().size();
    }

    /**
     * Procesar solicitud múltiple de profesionales para una obra
     */
    public List<ProfesionalObra> procesarSolicitudProfesionales(SolicitudProfesionalesRequest solicitud) {
        List<ProfesionalObra> asignacionesCreadas = new ArrayList<>();

        Obra obra = obraService.findById(solicitud.obraId);

        // Si no hay tipos de profesionales especificados, retornar lista vacía
        if (solicitud.tiposProfesionales == null || solicitud.tiposProfesionales.isEmpty()) {
            return asignacionesCreadas;
        }

        // Por cada tipo de profesional solicitado
        for (SolicitudTipoProfesionalRequest tipoProfesional : solicitud.tiposProfesionales) {

            // Validar datos mínimos
            if (tipoProfesional.tipoProfesional == null || tipoProfesional.tipoProfesional.trim().isEmpty()) {
                System.out.println("Saltando tipo de profesional sin especificar");
                continue;
            }

            // Establecer valores por defecto
            int cantidadSolicitada = tipoProfesional.cantidad != null ? tipoProfesional.cantidad : 1;

            // Buscar profesionales disponibles de este tipo
            List<Profesional> profesionalesDisponibles = profesionalService.buscarPorTipoProfesionalActivos(
                    tipoProfesional.tipoProfesional.trim());

            if (profesionalesDisponibles.size() < cantidadSolicitada) {
                System.out.println("No hay suficientes profesionales de tipo '" + tipoProfesional.tipoProfesional
                        + "'. Solicitados: " + cantidadSolicitada + ", Disponibles: "
                        + profesionalesDisponibles.size());
            }

            // Asignar la cantidad solicitada (o la máxima disponible)
            int cantidadAAsignar = Math.min(cantidadSolicitada, profesionalesDisponibles.size());

            for (int i = 0; i < cantidadAAsignar; i++) {
                Profesional profesional = profesionalesDisponibles.get(i);

                // TODO: ADAPTAR - Verificar que no esté ya asignado usando dirección
                // boolean yaAsignado = profesionalObraRepository.existsByProfesional_IdAndObra_IdAndActivoTrue(
                //         profesional.getId(), obra.getId());
                //
                // if (!yaAsignado) {

                    ProfesionalObra asignacion = new ProfesionalObra();
                    asignacion.setProfesional(profesional);
                    // Copiar los 4 campos de dirección desde Obra
                    asignacion.setDireccionObraCalle(obra.getDireccionObraCalle());
                    asignacion.setDireccionObraAltura(obra.getDireccionObraAltura());
                    asignacion.setDireccionObraPiso(obra.getDireccionObraPiso());
                    asignacion.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
                    asignacion.setEmpresaId(obra.getEmpresaId());
                    asignacion.setFechaDesde(solicitud.fechaDesde != null ? solicitud.fechaDesde : LocalDate.now());
                    asignacion.setFechaHasta(solicitud.fechaHasta);
                    asignacion.setRolEnObra(tipoProfesional.rolEnObra);
                    asignacion.setValorHoraAsignado(tipoProfesional.valorHoraSugerido);
                    asignacion.setActivo(true);

                    ProfesionalObra asignacionGuardada = profesionalObraRepository.save(asignacion);
                    asignacionesCreadas.add(asignacionGuardada);
                // } else {
                //     System.out.println("Profesional " + profesional.getNombre() + " ya está asignado a la obra "
                //             + obra.getNombre());
                // }
            }
        }

        return asignacionesCreadas;
    }

    /**
     * ============================================
     * MÉTODOS DE GESTIÓN DE CAJA CHICA
     * ============================================
     */

    /**
     * Asignar monto de caja chica a un profesional
     */
    @Transactional
    public ProfesionalObra asignarCajaChica(Long profesionalObraId, BigDecimal monto, Long empresaId) {
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(profesionalObraId)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional obra no encontrado con ID: " + profesionalObraId));

        // Validar empresa
        if (!profesionalObra.getEmpresaId().equals(empresaId)) {
            throw new SecurityException("El profesional no pertenece a la empresa especificada");
        }

        // Asignar monto
        profesionalObra.setMontoAsignado(monto);
        profesionalObra.setSaldoDisponible(monto);

        return profesionalObraRepository.save(profesionalObra);
    }

}