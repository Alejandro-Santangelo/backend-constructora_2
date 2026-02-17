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
        trabajoAdicional.setDiasNecesarios(requestDTO.getDiasNecesarios());
        trabajoAdicional.setFechaInicio(requestDTO.getFechaInicio());
        trabajoAdicional.setDescripcion(requestDTO.getDescripcion());
        trabajoAdicional.setObservaciones(requestDTO.getObservaciones());
        trabajoAdicional.setObraId(requestDTO.getObraId());
        trabajoAdicional.setTrabajoExtraId(requestDTO.getTrabajoExtraId());

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
}
