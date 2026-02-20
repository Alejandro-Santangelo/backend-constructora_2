package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.*;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.exception.TrabajoAdicionalValidationException;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar trabajos adicionales
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrabajoAdicionalService {

    private final TrabajoAdicionalRepository trabajoAdicionalRepository;
    private final TrabajoAdicionalProfesionalRepository trabajoAdicionalProfesionalRepository;
    private final ObraRepository obraRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EntidadFinancieraService entidadFinancieraService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Crear un nuevo trabajo adicional
     */
    @Transactional
    public TrabajoAdicionalResponseDTO crear(TrabajoAdicionalRequestDTO requestDTO) {
        log.info("Creando trabajo adicional: {}", requestDTO.getNombre());

        // Validar constraint: debe tener obra_id O trabajo_extra_id, pero no ambos
        validarConstraintObraOTrabajoExtra(requestDTO.getObraId(), requestDTO.getTrabajoExtraId());

        // Validar que la obra o trabajo extra existe y pertenece a la empresa
        validarObraOTrabajoExtra(requestDTO.getObraId(), requestDTO.getTrabajoExtraId(), requestDTO.getEmpresaId());

        // Validar profesionales registrados (si los hay)
        validarProfesionalesRegistrados(requestDTO.getProfesionales());

        // Crear entidad principal
        TrabajoAdicional trabajoAdicional = TrabajoAdicional.builder()
                .nombre(requestDTO.getNombre())
                .importe(requestDTO.getImporte())
                .importeJornales(requestDTO.getImporteJornales())
                .importeMateriales(requestDTO.getImporteMateriales())
                .importeGastosGenerales(requestDTO.getImporteGastosGenerales())
                .importeHonorarios(requestDTO.getImporteHonorarios())
                .tipoHonorarios(requestDTO.getTipoHonorarios())
                .importeMayoresCostos(requestDTO.getImporteMayoresCostos())
                .tipoMayoresCostos(requestDTO.getTipoMayoresCostos())
                // Honorarios individuales por categoría
                .honorarioJornales(requestDTO.getHonorarioJornales())
                .tipoHonorarioJornales(requestDTO.getTipoHonorarioJornales())
                .honorarioMateriales(requestDTO.getHonorarioMateriales())
                .tipoHonorarioMateriales(requestDTO.getTipoHonorarioMateriales())
                .honorarioGastosGenerales(requestDTO.getHonorarioGastosGenerales())
                .tipoHonorarioGastosGenerales(requestDTO.getTipoHonorarioGastosGenerales())
                .honorarioMayoresCostos(requestDTO.getHonorarioMayoresCostos())
                .tipoHonorarioMayoresCostos(requestDTO.getTipoHonorarioMayoresCostos())
                // Descuentos sobre importes base
                .descuentoJornales(requestDTO.getDescuentoJornales())
                .tipoDescuentoJornales(requestDTO.getTipoDescuentoJornales())
                .descuentoMateriales(requestDTO.getDescuentoMateriales())
                .tipoDescuentoMateriales(requestDTO.getTipoDescuentoMateriales())
                .descuentoGastosGenerales(requestDTO.getDescuentoGastosGenerales())
                .tipoDescuentoGastosGenerales(requestDTO.getTipoDescuentoGastosGenerales())
                .descuentoMayoresCostos(requestDTO.getDescuentoMayoresCostos())
                .tipoDescuentoMayoresCostos(requestDTO.getTipoDescuentoMayoresCostos())
                // Descuentos sobre honorarios
                .descuentoHonorarioJornales(requestDTO.getDescuentoHonorarioJornales())
                .tipoDescuentoHonorarioJornales(requestDTO.getTipoDescuentoHonorarioJornales())
                .descuentoHonorarioMateriales(requestDTO.getDescuentoHonorarioMateriales())
                .tipoDescuentoHonorarioMateriales(requestDTO.getTipoDescuentoHonorarioMateriales())
                .descuentoHonorarioGastosGenerales(requestDTO.getDescuentoHonorarioGastosGenerales())
                .tipoDescuentoHonorarioGastosGenerales(requestDTO.getTipoDescuentoHonorarioGastosGenerales())
                .descuentoHonorarioMayoresCostos(requestDTO.getDescuentoHonorarioMayoresCostos())
                .tipoDescuentoHonorarioMayoresCostos(requestDTO.getTipoDescuentoHonorarioMayoresCostos())
                .diasNecesarios(requestDTO.getDiasNecesarios())
                .fechaInicio(requestDTO.getFechaInicio())
                .descripcion(requestDTO.getDescripcion())
                .observaciones(requestDTO.getObservaciones())
                .obraId(requestDTO.getObraId())
                .trabajoExtraId(requestDTO.getTrabajoExtraId())
                .empresaId(requestDTO.getEmpresaId())
                .estado("PENDIENTE")
                .build();

        // Guardar trabajo adicional primero
        // Compatibilidad: extraer desglose si el frontend lo envió embebido en observaciones
        extraerDesgloseDeObservaciones(trabajoAdicional);
        TrabajoAdicional trabajoAdicionalGuardado = trabajoAdicionalRepository.save(trabajoAdicional);

        // Agregar profesionales si existen
        if (requestDTO.getProfesionales() != null && !requestDTO.getProfesionales().isEmpty()) {
            for (TrabajoAdicionalProfesionalDTO profDTO : requestDTO.getProfesionales()) {
                TrabajoAdicionalProfesional profesional = mapearProfesional(profDTO);
                trabajoAdicionalGuardado.addProfesional(profesional);
            }
            trabajoAdicionalGuardado = trabajoAdicionalRepository.save(trabajoAdicionalGuardado);
        }

        // SINCRONIZACIÓN: registrar en el sistema unificado de entidades financieras
        entidadFinancieraService.sincronizarDesdeTrabajoAdicional(trabajoAdicionalGuardado);

        log.info("Trabajo adicional creado exitosamente con ID: {}", trabajoAdicionalGuardado.getId());
        return mapearAResponseDTO(trabajoAdicionalGuardado);
    }

    /**
     * Obtener todos los trabajos adicionales (con filtros opcionales)
     */
    @Transactional(readOnly = true)
    public List<TrabajoAdicionalResponseDTO> obtenerTodos(Long empresaId, Long obraId, Long trabajoExtraId) {
        log.info("Obteniendo trabajos adicionales - empresaId: {}, obraId: {}, trabajoExtraId: {}", 
                 empresaId, obraId, trabajoExtraId);

        List<TrabajoAdicional> trabajos;

        if (obraId != null) {
            trabajos = trabajoAdicionalRepository.findByEmpresaIdAndObraIdWithProfesionales(empresaId, obraId);
        } else if (trabajoExtraId != null) {
            trabajos = trabajoAdicionalRepository.findByEmpresaIdAndTrabajoExtraIdWithProfesionales(empresaId, trabajoExtraId);
        } else {
            trabajos = trabajoAdicionalRepository.findAllByEmpresaIdWithProfesionales(empresaId);
        }

        return trabajos.stream()
                .map(this::mapearAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un trabajo adicional por ID
     */
    @Transactional(readOnly = true)
    public TrabajoAdicionalResponseDTO obtenerPorId(Long id, Long empresaId) {
        log.info("Obteniendo trabajo adicional por ID: {}", id);

        TrabajoAdicional trabajoAdicional = trabajoAdicionalRepository.findByIdWithProfesionales(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajo adicional no encontrado con ID: " + id));

        // Validar que pertenece a la empresa
        if (!trabajoAdicional.getEmpresaId().equals(empresaId)) {
            throw new TrabajoAdicionalValidationException(
                    "El trabajo adicional no pertenece a la empresa especificada");
        }

        return mapearAResponseDTO(trabajoAdicional);
    }

    /**
     * Actualizar un trabajo adicional existente
     */
    @Transactional
    public TrabajoAdicionalResponseDTO actualizar(Long id, TrabajoAdicionalRequestDTO requestDTO, Long empresaId) {
        log.info("Actualizando trabajo adicional con ID: {}", id);

        // Buscar trabajo adicional existente
        TrabajoAdicional trabajoAdicional = trabajoAdicionalRepository.findByIdWithProfesionales(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajo adicional no encontrado con ID: " + id));

        // Validar que pertenece a la empresa
        if (!trabajoAdicional.getEmpresaId().equals(empresaId)) {
            throw new TrabajoAdicionalValidationException(
                    "El trabajo adicional no pertenece a la empresa especificada");
        }

        // Validar constraint
        validarConstraintObraOTrabajoExtra(requestDTO.getObraId(), requestDTO.getTrabajoExtraId());

        // Validar obra/trabajo extra
        validarObraOTrabajoExtra(requestDTO.getObraId(), requestDTO.getTrabajoExtraId(), requestDTO.getEmpresaId());

        // Validar profesionales registrados
        validarProfesionalesRegistrados(requestDTO.getProfesionales());

        // Actualizar campos principales
        trabajoAdicional.setNombre(requestDTO.getNombre());
        trabajoAdicional.setImporte(requestDTO.getImporte());
        trabajoAdicional.setImporteJornales(requestDTO.getImporteJornales());
        trabajoAdicional.setImporteMateriales(requestDTO.getImporteMateriales());
        trabajoAdicional.setImporteGastosGenerales(requestDTO.getImporteGastosGenerales());
        trabajoAdicional.setImporteHonorarios(requestDTO.getImporteHonorarios());
        trabajoAdicional.setTipoHonorarios(requestDTO.getTipoHonorarios());
        trabajoAdicional.setImporteMayoresCostos(requestDTO.getImporteMayoresCostos());
        trabajoAdicional.setTipoMayoresCostos(requestDTO.getTipoMayoresCostos());
        // Honorarios individuales
        trabajoAdicional.setHonorarioJornales(requestDTO.getHonorarioJornales());
        trabajoAdicional.setTipoHonorarioJornales(requestDTO.getTipoHonorarioJornales());
        trabajoAdicional.setHonorarioMateriales(requestDTO.getHonorarioMateriales());
        trabajoAdicional.setTipoHonorarioMateriales(requestDTO.getTipoHonorarioMateriales());
        trabajoAdicional.setHonorarioGastosGenerales(requestDTO.getHonorarioGastosGenerales());
        trabajoAdicional.setTipoHonorarioGastosGenerales(requestDTO.getTipoHonorarioGastosGenerales());
        trabajoAdicional.setHonorarioMayoresCostos(requestDTO.getHonorarioMayoresCostos());
        trabajoAdicional.setTipoHonorarioMayoresCostos(requestDTO.getTipoHonorarioMayoresCostos());
        // Descuentos sobre importes base
        trabajoAdicional.setDescuentoJornales(requestDTO.getDescuentoJornales());
        trabajoAdicional.setTipoDescuentoJornales(requestDTO.getTipoDescuentoJornales());
        trabajoAdicional.setDescuentoMateriales(requestDTO.getDescuentoMateriales());
        trabajoAdicional.setTipoDescuentoMateriales(requestDTO.getTipoDescuentoMateriales());
        trabajoAdicional.setDescuentoGastosGenerales(requestDTO.getDescuentoGastosGenerales());
        trabajoAdicional.setTipoDescuentoGastosGenerales(requestDTO.getTipoDescuentoGastosGenerales());
        trabajoAdicional.setDescuentoMayoresCostos(requestDTO.getDescuentoMayoresCostos());
        trabajoAdicional.setTipoDescuentoMayoresCostos(requestDTO.getTipoDescuentoMayoresCostos());
        // Descuentos sobre honorarios
        trabajoAdicional.setDescuentoHonorarioJornales(requestDTO.getDescuentoHonorarioJornales());
        trabajoAdicional.setTipoDescuentoHonorarioJornales(requestDTO.getTipoDescuentoHonorarioJornales());
        trabajoAdicional.setDescuentoHonorarioMateriales(requestDTO.getDescuentoHonorarioMateriales());
        trabajoAdicional.setTipoDescuentoHonorarioMateriales(requestDTO.getTipoDescuentoHonorarioMateriales());
        trabajoAdicional.setDescuentoHonorarioGastosGenerales(requestDTO.getDescuentoHonorarioGastosGenerales());
        trabajoAdicional.setTipoDescuentoHonorarioGastosGenerales(requestDTO.getTipoDescuentoHonorarioGastosGenerales());
        trabajoAdicional.setDescuentoHonorarioMayoresCostos(requestDTO.getDescuentoHonorarioMayoresCostos());
        trabajoAdicional.setTipoDescuentoHonorarioMayoresCostos(requestDTO.getTipoDescuentoHonorarioMayoresCostos());
        trabajoAdicional.setDiasNecesarios(requestDTO.getDiasNecesarios());
        trabajoAdicional.setFechaInicio(requestDTO.getFechaInicio());
        trabajoAdicional.setDescripcion(requestDTO.getDescripcion());
        trabajoAdicional.setObservaciones(requestDTO.getObservaciones());
        trabajoAdicional.setObraId(requestDTO.getObraId());
        trabajoAdicional.setTrabajoExtraId(requestDTO.getTrabajoExtraId());

        // Compatibilidad: extraer desglose si el frontend lo envió embebido en observaciones
        extraerDesgloseDeObservaciones(trabajoAdicional);

        // Limpiar profesionales existentes
        trabajoAdicional.clearProfesionales();

        // Agregar nuevos profesionales
        if (requestDTO.getProfesionales() != null && !requestDTO.getProfesionales().isEmpty()) {
            for (TrabajoAdicionalProfesionalDTO profDTO : requestDTO.getProfesionales()) {
                TrabajoAdicionalProfesional profesional = mapearProfesional(profDTO);
                trabajoAdicional.addProfesional(profesional);
            }
        }

        TrabajoAdicional trabajoActualizado = trabajoAdicionalRepository.save(trabajoAdicional);

        log.info("Trabajo adicional actualizado exitosamente con ID: {}", id);
        return mapearAResponseDTO(trabajoActualizado);
    }

    /**
     * Eliminar un trabajo adicional
     */
    @Transactional
    public void eliminar(Long id, Long empresaId) {
        log.info("Eliminando trabajo adicional con ID: {}", id);

        // Verificar que existe y pertenece a la empresa
        TrabajoAdicional trabajoAdicional = trabajoAdicionalRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajo adicional no encontrado con ID: " + id));

        trabajoAdicionalRepository.delete(trabajoAdicional);
        log.info("Trabajo adicional eliminado exitosamente con ID: {}", id);
    }

    /**
     * Actualizar el estado de un trabajo adicional
     */
    @Transactional
    public TrabajoAdicionalResponseDTO actualizarEstado(Long id, String nuevoEstado, Long empresaId) {
        log.info("Actualizando estado de trabajo adicional {} a {}", id, nuevoEstado);

        TrabajoAdicional trabajoAdicional = trabajoAdicionalRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajo adicional no encontrado con ID: " + id));

        trabajoAdicional.setEstado(nuevoEstado);
        TrabajoAdicional trabajoActualizado = trabajoAdicionalRepository.save(trabajoAdicional);

        log.info("Estado actualizado exitosamente para trabajo adicional ID: {}", id);
        return mapearAResponseDTO(trabajoActualizado);
    }

    // ===================== MÉTODOS PRIVADOS DE VALIDACIÓN =====================

    /**
     * Validar que obraId esté presente (siempre obligatorio)
     * trabajoExtraId es opcional
     */
    private void validarConstraintObraOTrabajoExtra(Long obraId, Long trabajoExtraId) {
        if (obraId == null) {
            throw new TrabajoAdicionalValidationException(
                    "El ID de la obra es obligatorio. Todo trabajo adicional debe pertenecer a una obra");
        }
        
        // trabajoExtraId es opcional, no requiere validación aquí
        log.debug("Constraint validado - obraId: {}, trabajoExtraId: {}", obraId, trabajoExtraId);
    }

    /**
     * Validar que obra existe y que si hay trabajoExtraId, pertenezca a esa obra
     */
    private void validarObraOTrabajoExtra(Long obraId, Long trabajoExtraId, Long empresaId) {
        // Validar que la obra existe
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + obraId));

        log.debug("Obra validada: {} para empresa: {}", obraId, empresaId);

        // Si tiene trabajoExtraId, validar que existe y pertenece a la obra
        if (trabajoExtraId != null) {
            // Los trabajos extra son OBRAS con esObraTrabajoExtra = true
            Obra trabajoExtra = obraRepository.findById(trabajoExtraId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Trabajo extra no encontrado con ID: " + trabajoExtraId));

            // Validar que es realmente un trabajo extra
            if (!Boolean.TRUE.equals(trabajoExtra.getEsObraTrabajoExtra())) {
                throw new TrabajoAdicionalValidationException(
                        String.format("La obra con ID %d no es un trabajo extra", trabajoExtraId));
            }

            // VALIDACIÓN CRÍTICA: El trabajo extra debe pertenecer a la obra indicada
            if (!trabajoExtra.getObraOrigenId().equals(obraId)) {
                throw new TrabajoAdicionalValidationException(
                        String.format("El trabajo extra (ID: %d) no pertenece a la obra (ID: %d). " +
                                "El trabajo extra pertenece a la obra ID: %d", 
                                trabajoExtraId, obraId, trabajoExtra.getObraOrigenId()));
            }
            
            log.debug("Trabajo extra {} validado correctamente para obra {}", trabajoExtraId, obraId);
        }
    }

    private void validarProfesionalesRegistrados(List<TrabajoAdicionalProfesionalDTO> profesionales) {
        if (profesionales == null || profesionales.isEmpty()) {
            return;
        }

        for (TrabajoAdicionalProfesionalDTO profDTO : profesionales) {
            if (Boolean.TRUE.equals(profDTO.getEsRegistrado()) && profDTO.getProfesionalId() != null) {
                // Verificar que el profesional existe en la BD
                boolean existe = profesionalRepository.existsById(profDTO.getProfesionalId());
                if (!existe) {
                    throw new ResourceNotFoundException(
                            "Profesional no encontrado con ID: " + profDTO.getProfesionalId());
                }
            }
        }
    }

    // ===================== MÉTODOS DE MAPEO =====================

    private TrabajoAdicionalProfesional mapearProfesional(TrabajoAdicionalProfesionalDTO dto) {
        return TrabajoAdicionalProfesional.builder()
                .profesionalId(dto.getProfesionalId())
                .nombre(dto.getNombre())
                .tipoProfesional(dto.getTipoProfesional())
                .honorarioDia(dto.getHonorarioDia())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .esRegistrado(dto.getEsRegistrado())
                .build();
    }

    private TrabajoAdicionalResponseDTO mapearAResponseDTO(TrabajoAdicional trabajoAdicional) {
        List<TrabajoAdicionalProfesionalDTO> profesionalesDTO = trabajoAdicional.getProfesionales().stream()
                .map(this::mapearProfesionalADTO)
                .collect(Collectors.toList());

        return TrabajoAdicionalResponseDTO.builder()
                .id(trabajoAdicional.getId())
                .nombre(trabajoAdicional.getNombre())
                .importe(trabajoAdicional.getImporte())
                .importeJornales(trabajoAdicional.getImporteJornales())
                .importeMateriales(trabajoAdicional.getImporteMateriales())
                .importeGastosGenerales(trabajoAdicional.getImporteGastosGenerales())
                .importeHonorarios(trabajoAdicional.getImporteHonorarios())
                .tipoHonorarios(trabajoAdicional.getTipoHonorarios())
                .importeMayoresCostos(trabajoAdicional.getImporteMayoresCostos())
                .tipoMayoresCostos(trabajoAdicional.getTipoMayoresCostos())
                // Honorarios individuales
                .honorarioJornales(trabajoAdicional.getHonorarioJornales())
                .tipoHonorarioJornales(trabajoAdicional.getTipoHonorarioJornales())
                .honorarioMateriales(trabajoAdicional.getHonorarioMateriales())
                .tipoHonorarioMateriales(trabajoAdicional.getTipoHonorarioMateriales())
                .honorarioGastosGenerales(trabajoAdicional.getHonorarioGastosGenerales())
                .tipoHonorarioGastosGenerales(trabajoAdicional.getTipoHonorarioGastosGenerales())
                .honorarioMayoresCostos(trabajoAdicional.getHonorarioMayoresCostos())
                .tipoHonorarioMayoresCostos(trabajoAdicional.getTipoHonorarioMayoresCostos())
                // Descuentos sobre importes base
                .descuentoJornales(trabajoAdicional.getDescuentoJornales())
                .tipoDescuentoJornales(trabajoAdicional.getTipoDescuentoJornales())
                .descuentoMateriales(trabajoAdicional.getDescuentoMateriales())
                .tipoDescuentoMateriales(trabajoAdicional.getTipoDescuentoMateriales())
                .descuentoGastosGenerales(trabajoAdicional.getDescuentoGastosGenerales())
                .tipoDescuentoGastosGenerales(trabajoAdicional.getTipoDescuentoGastosGenerales())
                .descuentoMayoresCostos(trabajoAdicional.getDescuentoMayoresCostos())
                .tipoDescuentoMayoresCostos(trabajoAdicional.getTipoDescuentoMayoresCostos())
                // Descuentos sobre honorarios
                .descuentoHonorarioJornales(trabajoAdicional.getDescuentoHonorarioJornales())
                .tipoDescuentoHonorarioJornales(trabajoAdicional.getTipoDescuentoHonorarioJornales())
                .descuentoHonorarioMateriales(trabajoAdicional.getDescuentoHonorarioMateriales())
                .tipoDescuentoHonorarioMateriales(trabajoAdicional.getTipoDescuentoHonorarioMateriales())
                .descuentoHonorarioGastosGenerales(trabajoAdicional.getDescuentoHonorarioGastosGenerales())
                .tipoDescuentoHonorarioGastosGenerales(trabajoAdicional.getTipoDescuentoHonorarioGastosGenerales())
                .descuentoHonorarioMayoresCostos(trabajoAdicional.getDescuentoHonorarioMayoresCostos())
                .tipoDescuentoHonorarioMayoresCostos(trabajoAdicional.getTipoDescuentoHonorarioMayoresCostos())
                .diasNecesarios(trabajoAdicional.getDiasNecesarios())
                .fechaInicio(trabajoAdicional.getFechaInicio())
                .descripcion(trabajoAdicional.getDescripcion())
                .observaciones(trabajoAdicional.getObservaciones())
                .obraId(trabajoAdicional.getObraId())
                .trabajoExtraId(trabajoAdicional.getTrabajoExtraId())
                .empresaId(trabajoAdicional.getEmpresaId())
                .estado(trabajoAdicional.getEstado())
                .fechaCreacion(trabajoAdicional.getFechaCreacion() != null 
                        ? trabajoAdicional.getFechaCreacion().format(DATE_TIME_FORMATTER) : null)
                .fechaActualizacion(trabajoAdicional.getFechaActualizacion() != null 
                        ? trabajoAdicional.getFechaActualizacion().format(DATE_TIME_FORMATTER) : null)
                .profesionales(profesionalesDTO)
                .build();
    }

    private TrabajoAdicionalProfesionalDTO mapearProfesionalADTO(TrabajoAdicionalProfesional profesional) {
        return TrabajoAdicionalProfesionalDTO.builder()
                .id(profesional.getId())
                .profesionalId(profesional.getProfesionalId())
                .nombre(profesional.getNombre())
                .tipoProfesional(profesional.getTipoProfesional())
                .honorarioDia(profesional.getHonorarioDia())
                .telefono(profesional.getTelefono())
                .email(profesional.getEmail())
                .esRegistrado(profesional.getEsRegistrado())
                .fechaAsignacion(profesional.getFechaAsignacion() != null 
                        ? profesional.getFechaAsignacion().format(DATE_TIME_FORMATTER) : null)
                .build();
    }

    // =========================================================================
    // HELPERS: Compatibilidad con frontend que envía desglose en observaciones
    // =========================================================================

    /**
     * Si observaciones contiene un bloque con el desglose JSON enviado por el frontend,
     * extrae los valores y los persiste en las columnas relacionales,
     * limpiando el texto de observaciones.
     * Soporta etiquetas: [DESGLOSE_TRABAJO]...[/DESGLOSE_TRABAJO]
     *                 y: [DESGLOSE_OBRA]...[/DESGLOSE_OBRA]
     */
    private void extraerDesgloseDeObservaciones(TrabajoAdicional t) {
        String obs = t.getObservaciones();
        if (obs == null) return;

        String tagInicio = null;
        String tagFin    = null;
        if (obs.contains("[DESGLOSE_TRABAJO]")) {
            tagInicio = "[DESGLOSE_TRABAJO]";   tagFin = "[/DESGLOSE_TRABAJO]";
        } else if (obs.contains("[DESGLOSE_OBRA]")) {
            tagInicio = "[DESGLOSE_OBRA]";       tagFin = "[/DESGLOSE_OBRA]";
        }
        if (tagInicio == null) return;

        try {
            int inicio = obs.indexOf(tagInicio) + tagInicio.length();
            int fin    = obs.indexOf(tagFin);
            if (fin <= inicio) return;

            String json = obs.substring(inicio, fin).trim();

            if (t.getImporteJornales() == null)
                extractBigDecimal(json, "jornales").ifPresent(t::setImporteJornales);
            if (t.getImporteMateriales() == null)
                extractBigDecimal(json, "materiales").ifPresent(t::setImporteMateriales);
            if (t.getImporteHonorarios() == null)
                extractBigDecimal(json, "honorarios").ifPresent(t::setImporteHonorarios);
            if (t.getTipoHonorarios() == null)
                extractString(json, "tipoHonorarios").ifPresent(t::setTipoHonorarios);
            if (t.getImporteMayoresCostos() == null)
                extractBigDecimal(json, "mayoresCostos").ifPresent(t::setImporteMayoresCostos);
            if (t.getTipoMayoresCostos() == null)
                extractString(json, "tipoMayoresCostos").ifPresent(t::setTipoMayoresCostos);

            // Limpiar el bloque del campo observaciones
            String bloque   = tagInicio + obs.substring(inicio, fin) + tagFin;
            String obsLimpia = obs.replace(bloque, "").trim();
            t.setObservaciones(obsLimpia.isBlank() ? null : obsLimpia);

            log.info("Desglose extraído de observaciones para trabajo adicional '{}'", t.getNombre());
        } catch (Exception e) {
            log.warn("No se pudo extraer desglose de observaciones en trabajo adicional: {}", e.getMessage());
        }
    }

    private java.util.Optional<BigDecimal> extractBigDecimal(String json, String key) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + key + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            try { return java.util.Optional.of(new BigDecimal(m.group(1))); }
            catch (Exception ignored) {}
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<String> extractString(String json, String key) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) return java.util.Optional.of(m.group(1));
        return java.util.Optional.empty();
    }

    // === MÉTODOS PARA SISTEMA DE BORRADORES ===

    /**
     * Crea un trabajo adicional en estado BORRADOR.
     * Permite ir guardando los datos del formulario por etapas.
     * @param requestDTO Datos parciales o completos del trabajo adicional
     * @return Trabajo adicional creado en estado BORRADOR
     */
    @Transactional
    public TrabajoAdicionalResponseDTO crearBorrador(TrabajoAdicionalRequestDTO requestDTO) {
        log.info("🔧 Creando trabajo adicional como BORRADOR...");
        
        // Validaciones básicas (menos estrictas que la creación normal)
        validarConstraintObraOTrabajoExtra(requestDTO.getObraId(), requestDTO.getTrabajoExtraId());
        validarObraOTrabajoExtra(requestDTO.getObraId(), requestDTO.getTrabajoExtraId(), requestDTO.getEmpresaId());

        // Crear entidad principal con estado BORRADOR
        TrabajoAdicional trabajoAdicional = TrabajoAdicional.builder()
                .nombre(requestDTO.getNombre() != null ? requestDTO.getNombre() : "Borrador - " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM HH:mm")))
                .importe(requestDTO.getImporte())
                .importeJornales(requestDTO.getImporteJornales())
                .importeMateriales(requestDTO.getImporteMateriales())
                .importeGastosGenerales(requestDTO.getImporteGastosGenerales())
                .importeHonorarios(requestDTO.getImporteHonorarios())
                .tipoHonorarios(requestDTO.getTipoHonorarios())
                .importeMayoresCostos(requestDTO.getImporteMayoresCostos())
                .tipoMayoresCostos(requestDTO.getTipoMayoresCostos())
                // Honorarios individuales
                .honorarioJornales(requestDTO.getHonorarioJornales())
                .tipoHonorarioJornales(requestDTO.getTipoHonorarioJornales())
                .honorarioMateriales(requestDTO.getHonorarioMateriales())
                .tipoHonorarioMateriales(requestDTO.getTipoHonorarioMateriales())
                .honorarioGastosGenerales(requestDTO.getHonorarioGastosGenerales())
                .tipoHonorarioGastosGenerales(requestDTO.getTipoHonorarioGastosGenerales())
                .honorarioMayoresCostos(requestDTO.getHonorarioMayoresCostos())
                .tipoHonorarioMayoresCostos(requestDTO.getTipoHonorarioMayoresCostos())
                // Descuentos sobre importes base
                .descuentoJornales(requestDTO.getDescuentoJornales())
                .tipoDescuentoJornales(requestDTO.getTipoDescuentoJornales())
                .descuentoMateriales(requestDTO.getDescuentoMateriales())
                .tipoDescuentoMateriales(requestDTO.getTipoDescuentoMateriales())
                .descuentoGastosGenerales(requestDTO.getDescuentoGastosGenerales())
                .tipoDescuentoGastosGenerales(requestDTO.getTipoDescuentoGastosGenerales())
                .descuentoMayoresCostos(requestDTO.getDescuentoMayoresCostos())
                .tipoDescuentoMayoresCostos(requestDTO.getTipoDescuentoMayoresCostos())
                // Descuentos sobre honorarios
                .descuentoHonorarioJornales(requestDTO.getDescuentoHonorarioJornales())
                .tipoDescuentoHonorarioJornales(requestDTO.getTipoDescuentoHonorarioJornales())
                .descuentoHonorarioMateriales(requestDTO.getDescuentoHonorarioMateriales())
                .tipoDescuentoHonorarioMateriales(requestDTO.getTipoDescuentoHonorarioMateriales())
                .descuentoHonorarioGastosGenerales(requestDTO.getDescuentoHonorarioGastosGenerales())
                .tipoDescuentoHonorarioGastosGenerales(requestDTO.getTipoDescuentoHonorarioGastosGenerales())
                .descuentoHonorarioMayoresCostos(requestDTO.getDescuentoHonorarioMayoresCostos())
                .tipoDescuentoHonorarioMayoresCostos(requestDTO.getTipoDescuentoHonorarioMayoresCostos())
                // Información básica
                .descripcion(requestDTO.getDescripcion())
                .fechaInicio(requestDTO.getFechaInicio())
                .obraId(requestDTO.getObraId())
                .trabajoExtraId(requestDTO.getTrabajoExtraId())
                .empresaId(requestDTO.getEmpresaId())
                .estado(TrabajoAdicional.ESTADO_BORRADOR) // Específicamente como borrador
                .build();

        // Guardar inmediatamente para obtener ID
        TrabajoAdicional borradorGuardado = trabajoAdicionalRepository.save(trabajoAdicional);
        log.info("✅ Trabajo adicional borrador creado con ID: {} en estado: {}", 
                 borradorGuardado.getId(), borradorGuardado.getEstado());

        // Sincronizar con entidades financieras si aplica
        try {
            entidadFinancieraService.sincronizarDesdeTrabajoAdicional(borradorGuardado);
        } catch (Exception e) {
            log.warn("⚠️ Error al sincronizar borrador con entidades financieras: {}", e.getMessage());
        }

        return mapearAResponseDTO(borradorGuardado);
    }

    /**
     * Actualiza un borrador de trabajo adicional.
     * Solo permite actualización si está en estado BORRADOR.
     * @param id ID del trabajo adicional borrador
     * @param requestDTO Nuevos datos del trabajo adicional
     * @return Trabajo adicional borrador actualizado
     */
    @Transactional
    public TrabajoAdicionalResponseDTO actualizarBorrador(Long id, TrabajoAdicionalRequestDTO requestDTO) {
        log.info("🔧 Actualizando borrador de trabajo adicional ID: {}", id);
        
        TrabajoAdicional trabajoExistente = trabajoAdicionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajo adicional borrador no encontrado con ID: " + id));

        // Verificar que sea realmente un borrador
        if (!trabajoExistente.esBorrador()) {
            throw new IllegalStateException("Solo se pueden actualizar trabajos adicionales en estado BORRADOR. Estado actual: " + trabajoExistente.getEstado());
        }

        // Actualizar todos los campos del formulario
        actualizarCamposBorrador(trabajoExistente, requestDTO);

        // Mantener estado BORRADOR
        trabajoExistente.setEstado(TrabajoAdicional.ESTADO_BORRADOR);

        TrabajoAdicional trabajoActualizado = trabajoAdicionalRepository.save(trabajoExistente);
        log.info("✅ Borrador actualizado exitosamente. Campos persistidos.");

        return mapearAResponseDTO(trabajoActualizado);
    }

    /**
     * Convierte un borrador en trabajo adicional activo.
     * Cambia del estado BORRADOR a PENDIENTE.
     * @param id ID del trabajo adicional borrador
     * @return Trabajo adicional transformado a estado activo
     */
    @Transactional
    public TrabajoAdicionalResponseDTO confirmarBorrador(Long id) {
        log.info("🔧 Confirmando borrador de trabajo adicional ID: {} -> trabajo activo", id);
        
        TrabajoAdicional trabajoBorrador = trabajoAdicionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trabajo adicional borrador no encontrado con ID: " + id));

        // Verificar que sea borrador
        if (!trabajoBorrador.esBorrador()) {
            throw new IllegalStateException("Solo se pueden confirmar trabajos adicionales en estado BORRADOR. Estado actual: " + trabajoBorrador.getEstado());
        }

        // Validar que tiene los datos mínimos requeridos
        validarDatosMinimosParaConfirmacion(trabajoBorrador);

        // Cambiar a estado activo
        trabajoBorrador.setEstado(TrabajoAdicional.ESTADO_PENDIENTE);

        TrabajoAdicional trabajoConfirmado = trabajoAdicionalRepository.save(trabajoBorrador);
        log.info("✅ Trabajo adicional confirmado. Estado cambiado de BORRADOR a {}", trabajoConfirmado.getEstado());

        // Re-sincronizar con entidades financieras como trabajo activo
        try {
            entidadFinancieraService.sincronizarDesdeTrabajoAdicional(trabajoConfirmado);
        } catch (Exception e) {
            log.warn("⚠️ Error al re-sincronizar trabajo adicional confirmado: {}", e.getMessage());
        }

        return mapearAResponseDTO(trabajoConfirmado);
    }

    /**
     * Obtiene todos los borradores de trabajos adicionales por empresa.
     * @param empresaId ID de la empresa
     * @param obraId ID de la obra (opcional)
     * @param trabajoExtraId ID del trabajo extra (opcional)
     * @return Lista de trabajos adicionales en estado BORRADOR
     */
    public List<TrabajoAdicionalResponseDTO> obtenerBorradores(Long empresaId, Long obraId, Long trabajoExtraId) {
        log.info("📋 Obteniendo borradores de trabajos adicionales para empresa ID: {}", empresaId);
        
        List<TrabajoAdicional> borradores;
        
        if (obraId != null) {
            borradores = trabajoAdicionalRepository.findBorradoresByEmpresaIdAndObraId(empresaId, obraId);
        } else if (trabajoExtraId != null) {
            borradores = trabajoAdicionalRepository.findBorradoresByEmpresaIdAndTrabajoExtraId(empresaId, trabajoExtraId);
        } else {
            borradores = trabajoAdicionalRepository.findBorradoresByEmpresaId(empresaId);
        }
        
        return borradores.stream()
                .map(this::mapearAResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Método auxiliar para validar datos mínimos antes de confirmar.
     */
    private void validarDatosMinimosParaConfirmacion(TrabajoAdicional trabajo) {
        if (trabajo.getNombre() == null || trabajo.getNombre().trim().isEmpty()) {
            throw new IllegalStateException("El nombre del trabajo adicional es obligatorio para confirmar el borrador");
        }
        
        if (trabajo.getImporte() == null) {
            throw new IllegalStateException("El importe total es obligatorio para confirmar el borrador");
        }

        // Validar que al menos tenga un importe base
        boolean tieneImporteBase = (trabajo.getImporteJornales() != null && trabajo.getImporteJornales().compareTo(BigDecimal.ZERO) > 0) ||
                                   (trabajo.getImporteMateriales() != null && trabajo.getImporteMateriales().compareTo(BigDecimal.ZERO) > 0) ||
                                   (trabajo.getImporteGastosGenerales() != null && trabajo.getImporteGastosGenerales().compareTo(BigDecimal.ZERO) > 0);
        
        if (!tieneImporteBase) {
            throw new IllegalStateException("Debe especificar al menos un importe base (jornales, materiales o gastos generales) para confirmar el borrador");
        }

        // Agregar más validaciones según reglas de negocio
    }

    /**
     * Método auxiliar para actualizar campos de trabajo adicional.
     */
    private void actualizarCamposBorrador(TrabajoAdicional trabajoExistente, TrabajoAdicionalRequestDTO nuevosData) {
        // Información básica
        if (nuevosData.getNombre() != null) {
            trabajoExistente.setNombre(nuevosData.getNombre());
        }
        if (nuevosData.getDescripcion() != null) {
            trabajoExistente.setDescripcion(nuevosData.getDescripcion());
        }
        if (nuevosData.getFechaInicio() != null) {
            trabajoExistente.setFechaInicio(nuevosData.getFechaInicio());
        }

        // Importes base
        if (nuevosData.getImporte() != null) {
            trabajoExistente.setImporte(nuevosData.getImporte());
        }
        if (nuevosData.getImporteJornales() != null) {
            trabajoExistente.setImporteJornales(nuevosData.getImporteJornales());
        }
        if (nuevosData.getImporteMateriales() != null) {
            trabajoExistente.setImporteMateriales(nuevosData.getImporteMateriales());
        }
        if (nuevosData.getImporteGastosGenerales() != null) {
            trabajoExistente.setImporteGastosGenerales(nuevosData.getImporteGastosGenerales());
        }
        if (nuevosData.getImporteHonorarios() != null) {
            trabajoExistente.setImporteHonorarios(nuevosData.getImporteHonorarios());
        }
        if (nuevosData.getTipoHonorarios() != null) {
            trabajoExistente.setTipoHonorarios(nuevosData.getTipoHonorarios());
        }
        if (nuevosData.getImporteMayoresCostos() != null) {
            trabajoExistente.setImporteMayoresCostos(nuevosData.getImporteMayoresCostos());
        }
        if (nuevosData.getTipoMayoresCostos() != null) {
            trabajoExistente.setTipoMayoresCostos(nuevosData.getTipoMayoresCostos());
        }

        // Honorarios individuales por categoría
        if (nuevosData.getHonorarioJornales() != null) {
            trabajoExistente.setHonorarioJornales(nuevosData.getHonorarioJornales());
        }
        if (nuevosData.getTipoHonorarioJornales() != null) {
            trabajoExistente.setTipoHonorarioJornales(nuevosData.getTipoHonorarioJornales());
        }
        if (nuevosData.getHonorarioMateriales() != null) {
            trabajoExistente.setHonorarioMateriales(nuevosData.getHonorarioMateriales());
        }
        if (nuevosData.getTipoHonorarioMateriales() != null) {
            trabajoExistente.setTipoHonorarioMateriales(nuevosData.getTipoHonorarioMateriales());
        }
        if (nuevosData.getHonorarioGastosGenerales() != null) {
            trabajoExistente.setHonorarioGastosGenerales(nuevosData.getHonorarioGastosGenerales());
        }
        if (nuevosData.getTipoHonorarioGastosGenerales() != null) {
            trabajoExistente.setTipoHonorarioGastosGenerales(nuevosData.getTipoHonorarioGastosGenerales());
        }
        if (nuevosData.getHonorarioMayoresCostos() != null) {
            trabajoExistente.setHonorarioMayoresCostos(nuevosData.getHonorarioMayoresCostos());
        }
        if (nuevosData.getTipoHonorarioMayoresCostos() != null) {
            trabajoExistente.setTipoHonorarioMayoresCostos(nuevosData.getTipoHonorarioMayoresCostos());
        }

        // Descuentos sobre importes base
        if (nuevosData.getDescuentoJornales() != null) {
            trabajoExistente.setDescuentoJornales(nuevosData.getDescuentoJornales());
        }
        if (nuevosData.getTipoDescuentoJornales() != null) {
            trabajoExistente.setTipoDescuentoJornales(nuevosData.getTipoDescuentoJornales());
        }
        if (nuevosData.getDescuentoMateriales() != null) {
            trabajoExistente.setDescuentoMateriales(nuevosData.getDescuentoMateriales());
        }
        if (nuevosData.getTipoDescuentoMateriales() != null) {
            trabajoExistente.setTipoDescuentoMateriales(nuevosData.getTipoDescuentoMateriales());
        }
        if (nuevosData.getDescuentoGastosGenerales() != null) {
            trabajoExistente.setDescuentoGastosGenerales(nuevosData.getDescuentoGastosGenerales());
        }
        if (nuevosData.getTipoDescuentoGastosGenerales() != null) {
            trabajoExistente.setTipoDescuentoGastosGenerales(nuevosData.getTipoDescuentoGastosGenerales());
        }
        if (nuevosData.getDescuentoMayoresCostos() != null) {
            trabajoExistente.setDescuentoMayoresCostos(nuevosData.getDescuentoMayoresCostos());
        }
        if (nuevosData.getTipoDescuentoMayoresCostos() != null) {
            trabajoExistente.setTipoDescuentoMayoresCostos(nuevosData.getTipoDescuentoMayoresCostos());
        }

        // Descuentos sobre honorarios
        if (nuevosData.getDescuentoHonorarioJornales() != null) {
            trabajoExistente.setDescuentoHonorarioJornales(nuevosData.getDescuentoHonorarioJornales());
        }
        if (nuevosData.getTipoDescuentoHonorarioJornales() != null) {
            trabajoExistente.setTipoDescuentoHonorarioJornales(nuevosData.getTipoDescuentoHonorarioJornales());
        }
        if (nuevosData.getDescuentoHonorarioMateriales() != null) {
            trabajoExistente.setDescuentoHonorarioMateriales(nuevosData.getDescuentoHonorarioMateriales());
        }
        if (nuevosData.getTipoDescuentoHonorarioMateriales() != null) {
            trabajoExistente.setTipoDescuentoHonorarioMateriales(nuevosData.getTipoDescuentoHonorarioMateriales());
        }
        if (nuevosData.getDescuentoHonorarioGastosGenerales() != null) {
            trabajoExistente.setDescuentoHonorarioGastosGenerales(nuevosData.getDescuentoHonorarioGastosGenerales());
        }
        if (nuevosData.getTipoDescuentoHonorarioGastosGenerales() != null) {
            trabajoExistente.setTipoDescuentoHonorarioGastosGenerales(nuevosData.getTipoDescuentoHonorarioGastosGenerales());
        }
        if (nuevosData.getDescuentoHonorarioMayoresCostos() != null) {
            trabajoExistente.setDescuentoHonorarioMayoresCostos(nuevosData.getDescuentoHonorarioMayoresCostos());
        }
        if (nuevosData.getTipoDescuentoHonorarioMayoresCostos() != null) {
            trabajoExistente.setTipoDescuentoHonorarioMayoresCostos(nuevosData.getTipoDescuentoHonorarioMayoresCostos());
        }
    }
}
