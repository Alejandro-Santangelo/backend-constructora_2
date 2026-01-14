package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.EtapaDiariaRequestDTO;
import com.rodrigo.construccion.dto.request.TareaEtapaDiariaDTO;
import com.rodrigo.construccion.dto.response.EtapaDiariaResponseDTO;
import com.rodrigo.construccion.model.entity.EtapaDiaria;
import com.rodrigo.construccion.model.entity.ProfesionalTareaEtapa;
import com.rodrigo.construccion.model.entity.TareaEtapaDiaria;
import com.rodrigo.construccion.repository.ProfesionalTareaEtapaRepository;
import com.rodrigo.construccion.repository.TareaEtapaDiariaRepository;
import com.rodrigo.construccion.repository.EtapaDiariaRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de etapas diarias con tareas
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtapaDiariaService implements IEtapaDiariaService {

    private final EtapaDiariaRepository etapaDiariaRepository;
    private final TareaEtapaDiariaRepository tareaRepository;
    private final ProfesionalTareaEtapaRepository profesionalTareaRepository;
    private final ObraRepository obraRepository;
    private final EntityManager entityManager;

    private static final Set<String> ESTADOS_ETAPA_VALIDOS = new HashSet<>(Arrays.asList(
        "PENDIENTE", "EN_PROCESO", "TERMINADA", "SUSPENDIDA"
    ));

    private static final Set<String> ESTADOS_TAREA_VALIDOS = new HashSet<>(Arrays.asList(
        "PENDIENTE", "EN_PROCESO", "COMPLETADA"
    ));

    @Override
    @Transactional(readOnly = true)
    public List<EtapaDiariaResponseDTO> obtenerPorObra(Long empresaId, Long obraId) {
        log.info("Obteniendo etapas diarias para obra {} y empresa {}", obraId, empresaId);
        
        // Validar que la obra existe y pertenece a la empresa
        validarObraPerteneciaEmpresa(obraId, empresaId);
        
        List<EtapaDiaria> etapas = etapaDiariaRepository.findByObraIdAndEmpresaId(obraId, empresaId);
        
        return etapas.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EtapaDiariaResponseDTO obtenerPorId(Long empresaId, Long id) {
        log.info("Obteniendo etapa diaria {} para empresa {}", id, empresaId);
        
        EtapaDiaria etapaDiaria = etapaDiariaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "Etapa diaria no encontrada con ID: " + id + " o no pertenece a la empresa"));
        
        return mapearEntityAResponse(etapaDiaria);
    }

    @Override
    @Transactional(readOnly = true)
    public EtapaDiariaResponseDTO obtenerPorObraYFecha(Long empresaId, Long obraId, LocalDate fecha) {
        log.info("Obteniendo etapa diaria para obra {}, fecha {} y empresa {}", obraId, fecha, empresaId);
        
        // Validar que la obra existe y pertenece a la empresa
        validarObraPerteneciaEmpresa(obraId, empresaId);
        
        EtapaDiaria etapaDiaria = etapaDiariaRepository.findByObraIdAndFechaAndEmpresaId(obraId, fecha, empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "Etapa diaria no encontrada para la obra " + obraId + " en la fecha " + fecha));
        
        return mapearEntityAResponse(etapaDiaria);
    }

    @Override
    @Transactional
    public EtapaDiariaResponseDTO crear(Long empresaId, EtapaDiariaRequestDTO request) {
        log.info("Creando etapa diaria para obra {} y empresa {} en fecha {}", 
                request.getObraId(), empresaId, request.getFecha());
        
        validarObraPerteneciaEmpresa(request.getObraId(), empresaId);
        
        if (etapaDiariaRepository.existsByObraIdAndFechaAndEmpresaId(
                request.getObraId(), request.getFecha(), empresaId)) {
            throw new RuntimeException(
                "Ya existe una etapa diaria para la obra " + request.getObraId() + 
                " en la fecha " + request.getFecha());
        }
        
        EtapaDiaria etapaDiaria = EtapaDiaria.builder()
                .obraId(request.getObraId())
                .empresaId(empresaId)
                .fecha(request.getFecha())
                .estado(request.getEstado())
                .descripcion(request.getDescripcion())
                .observaciones(request.getObservaciones())
                .tareas(new ArrayList<>())
                .build();
        
        EtapaDiaria guardada = etapaDiariaRepository.save(etapaDiaria);
        
        // Crear tareas si existen
        if (request.getTareas() != null && !request.getTareas().isEmpty()) {
            crearTareas(guardada.getId(), request.getTareas());
        }
        
        log.info("Etapa diaria creada exitosamente con ID: {}", guardada.getId());
        
        return mapearEntityAResponse(etapaDiariaRepository.findById(guardada.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public EtapaDiariaResponseDTO actualizar(Long empresaId, Long id, EtapaDiariaRequestDTO request) {
        log.info("Actualizando etapa diaria {} para empresa {}", id, empresaId);
        
        EtapaDiaria etapaDiaria = etapaDiariaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "Etapa diaria no encontrada con ID: " + id + " o no pertenece a la empresa"));
        
        validarObraPerteneciaEmpresa(request.getObraId(), empresaId);
        
        if (!etapaDiaria.getObraId().equals(request.getObraId()) || 
            !etapaDiaria.getFecha().equals(request.getFecha())) {
            
            if (etapaDiariaRepository.existsByObraIdAndFechaAndEmpresaId(
                    request.getObraId(), request.getFecha(), empresaId)) {
                throw new RuntimeException(
                    "Ya existe una etapa diaria para la obra " + request.getObraId() + 
                    " en la fecha " + request.getFecha());
            }
        }
        
        etapaDiaria.setObraId(request.getObraId());
        etapaDiaria.setFecha(request.getFecha());
        etapaDiaria.setEstado(request.getEstado());
        etapaDiaria.setDescripcion(request.getDescripcion());
        etapaDiaria.setObservaciones(request.getObservaciones());
        
        etapaDiariaRepository.save(etapaDiaria);
        
        // Actualizar tareas (eliminar las que no vienen, actualizar existentes, crear nuevas)
        actualizarTareas(id, request.getTareas());
        
        log.info("Etapa diaria {} actualizada exitosamente", id);
        
        return mapearEntityAResponse(etapaDiariaRepository.findById(id).orElseThrow());
    }

    @Transactional
    protected void crearTareas(Long etapaDiariaId, List<TareaEtapaDiariaDTO> tareasDTO) {
        for (TareaEtapaDiariaDTO tareaDTO : tareasDTO) {
            validarEstadoTarea(tareaDTO.getEstado());
            
            TareaEtapaDiaria tarea = TareaEtapaDiaria.builder()
                    .etapaDiariaId(etapaDiariaId)
                    .descripcion(tareaDTO.getDescripcion())
                    .estado(tareaDTO.getEstado())
                    .profesionales(new ArrayList<>())
                    .build();
            
            TareaEtapaDiaria tareaGuardada = tareaRepository.save(tarea);
            
            // Asignar profesionales
            if (tareaDTO.getProfesionales() != null && !tareaDTO.getProfesionales().isEmpty()) {
                for (Long profesionalId : tareaDTO.getProfesionales()) {
                    ProfesionalTareaEtapa profesional = ProfesionalTareaEtapa.builder()
                            .tareaId(tareaGuardada.getId())
                            .profesionalId(profesionalId)
                            .build();
                    profesionalTareaRepository.save(profesional);
                }
            }
        }
    }

    @Transactional
    protected void actualizarTareas(Long etapaDiariaId, List<TareaEtapaDiariaDTO> tareasDTO) {
        // Obtener tareas existentes
        List<TareaEtapaDiaria> tareasExistentes = tareaRepository.findByEtapaDiariaId(etapaDiariaId);
        
        if (tareasDTO == null || tareasDTO.isEmpty()) {
            // Eliminar todas las tareas
            tareasExistentes.forEach(t -> tareaRepository.delete(t));
            return;
        }
        
        // IDs de tareas que vienen en el request
        Set<Long> idsEnRequest = tareasDTO.stream()
                .map(TareaEtapaDiariaDTO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        // Eliminar tareas que no vienen en el request
        tareasExistentes.stream()
                .filter(t -> !idsEnRequest.contains(t.getId()))
                .forEach(t -> tareaRepository.delete(t));
        
        // Procesar cada tarea del request
        for (TareaEtapaDiariaDTO tareaDTO : tareasDTO) {
            validarEstadoTarea(tareaDTO.getEstado());
            
            if (tareaDTO.getId() != null) {
                // Actualizar existente
                TareaEtapaDiaria tarea = tareaRepository.findById(tareaDTO.getId())
                        .orElseThrow(() -> new RuntimeException("Tarea no encontrada: " + tareaDTO.getId()));
                
                tarea.setDescripcion(tareaDTO.getDescripcion());
                tarea.setEstado(tareaDTO.getEstado());
                tareaRepository.save(tarea);
                
                // Actualizar profesionales - ELIMINAR primero y hacer flush
                profesionalTareaRepository.deleteByTareaId(tarea.getId());
                entityManager.flush(); // Forzar eliminación antes de insertar
                
                if (tareaDTO.getProfesionales() != null) {
                    for (Long profesionalId : tareaDTO.getProfesionales()) {
                        ProfesionalTareaEtapa profesional = ProfesionalTareaEtapa.builder()
                                .tareaId(tarea.getId())
                                .profesionalId(profesionalId)
                                .build();
                        profesionalTareaRepository.save(profesional);
                    }
                }
            } else {
                // Crear nueva
                TareaEtapaDiaria tarea = TareaEtapaDiaria.builder()
                        .etapaDiariaId(etapaDiariaId)
                        .descripcion(tareaDTO.getDescripcion())
                        .estado(tareaDTO.getEstado())
                        .profesionales(new ArrayList<>())
                        .build();
                
                TareaEtapaDiaria tareaGuardada = tareaRepository.save(tarea);
                
                if (tareaDTO.getProfesionales() != null) {
                    for (Long profesionalId : tareaDTO.getProfesionales()) {
                        ProfesionalTareaEtapa profesional = ProfesionalTareaEtapa.builder()
                                .tareaId(tareaGuardada.getId())
                                .profesionalId(profesionalId)
                                .build();
                        profesionalTareaRepository.save(profesional);
                    }
                }
            }
        }
    }

    private void validarEstadoTarea(String estado) {
        if (!ESTADOS_TAREA_VALIDOS.contains(estado)) {
            throw new RuntimeException(
                "Estado de tarea inválido: " + estado + ". Valores permitidos: " + ESTADOS_TAREA_VALIDOS);
        }
    }

    @Override
    @Transactional
    public void eliminar(Long empresaId, Long id) {
        log.info("Eliminando etapa diaria {} para empresa {}", id, empresaId);
        
        // Validar que la etapa diaria existe y pertenece a la empresa
        if (!etapaDiariaRepository.existsByIdAndEmpresaId(id, empresaId)) {
            throw new RuntimeException(
                "Etapa diaria no encontrada con ID: " + id + " o no pertenece a la empresa");
        }
        
        etapaDiariaRepository.deleteById(id);
        log.info("Etapa diaria {} eliminada exitosamente", id);
    }

    /**
     * Valida que la obra existe y pertenece a la empresa
     */
    private void validarObraPerteneciaEmpresa(Long obraId, Long empresaId) {
        boolean existe = obraRepository.findByIdAndEmpresaId(obraId, empresaId).isPresent();
        
        if (!existe) {
            throw new RuntimeException(
                "La obra con ID " + obraId + " no existe o no pertenece a la empresa " + empresaId);
        }
    }

    /**
     * Mapea una entidad EtapaDiaria a su DTO de respuesta
     */
    private EtapaDiariaResponseDTO mapearEntityAResponse(EtapaDiaria entity) {
        // Obtener tareas
        List<TareaEtapaDiaria> tareas = tareaRepository.findByEtapaDiariaId(entity.getId());
        
        List<EtapaDiariaResponseDTO.TareaResponseDTO> tareasDTO = tareas.stream()
                .map(tarea -> {
                    // Obtener profesionales de la tarea
                    List<Long> profesionalesIds = profesionalTareaRepository.findByTareaId(tarea.getId())
                            .stream()
                            .map(ProfesionalTareaEtapa::getProfesionalId)
                            .collect(Collectors.toList());
                    
                    return EtapaDiariaResponseDTO.TareaResponseDTO.builder()
                            .id(tarea.getId())
                            .descripcion(tarea.getDescripcion())
                            .estado(tarea.getEstado())
                            .profesionales(profesionalesIds)
                            .build();
                })
                .collect(Collectors.toList());
        
        return EtapaDiariaResponseDTO.builder()
                .id(entity.getId())
                .obraId(entity.getObraId())
                .empresaId(entity.getEmpresaId())
                .fecha(entity.getFecha())
                .estado(entity.getEstado())
                .descripcion(entity.getDescripcion())
                .observaciones(entity.getObservaciones())
                .fechaCreacion(entity.getCreatedAt())
                .fechaModificacion(entity.getUpdatedAt())
                .tareas(tareasDTO)
                .build();
    }
}
