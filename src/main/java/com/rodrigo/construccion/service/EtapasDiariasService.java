package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.ProfesionalAsignadoTareaDTO;
import com.rodrigo.construccion.dto.ProfesionalDisponibleDTO;
import com.rodrigo.construccion.dto.ResumenDTO;
import com.rodrigo.construccion.dto.request.EtapasDiariasRequestDTO;
import com.rodrigo.construccion.dto.request.ProfesionalTareaRequestDTO;
import com.rodrigo.construccion.dto.request.TareaRequestDTO;
import com.rodrigo.construccion.dto.response.EtapaDiariaCreacionResponseDTO;
import com.rodrigo.construccion.dto.response.EtapasDiariasResponseDTO;
import com.rodrigo.construccion.dto.response.TareaResponseDTO;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service para gestión de etapas diarias y asignación de profesionales a tareas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EtapasDiariasService {

    private final EtapaDiariaRepository etapaDiariaRepository;
    private final TareaEtapaDiariaRepository tareaEtapaDiariaRepository;
    private final ProfesionalTareaDiaRepository profesionalTareaDiaRepository;
    private final AsignacionProfesionalDiaRepository asignacionProfesionalDiaRepository;
    private final ObraRepository obraRepository;

    /**
     * Obtener etapas diarias con profesionales disponibles y tareas
     */
    @Transactional
    public EtapasDiariasResponseDTO obtenerEtapasDiarias(Long obraId, LocalDate fecha, Long empresaId) {
        log.info("Obteniendo etapas diarias para obra {} en fecha {}", obraId, fecha);

        // 1. Validar obra
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada con ID: " + obraId));

        // 2. Obtener profesionales disponibles ese día
        List<ProfesionalDisponibleDTO> profesionalesDisponibles = obtenerProfesionalesDisponibles(obraId, fecha, empresaId);

        // 3. Obtener etapa diaria (si existe)
        EtapaDiaria etapaDiaria = etapaDiariaRepository.findByObraIdAndFechaAndEmpresaId(obraId, fecha, empresaId).orElse(null);

        // 4. Obtener tareas (si hay etapa)
        List<TareaResponseDTO> tareas = new ArrayList<>();
        if (etapaDiaria != null) {
            tareas = obtenerTareasConProfesionales(etapaDiaria.getId(), empresaId);
        }

        // 5. Construir respuesta
        return EtapasDiariasResponseDTO.builder()
                .fecha(fecha)
                .obraId(obraId)
                .obraNombre(obra.getDireccionCompleta())
                .profesionalesDisponibles(profesionalesDisponibles)
                .tareas(tareas)
                .resumen(ResumenDTO.builder()
                        .totalProfesionalesDisponibles(profesionalesDisponibles.size())
                        .totalTareas(tareas.size())
                        .totalAsignaciones(0)
                        .build())
                .build();
    }

    /**
     * Guardar etapas diarias con tareas y profesionales asignados
     */
    @Transactional
    public EtapaDiariaCreacionResponseDTO guardarEtapasDiarias(EtapasDiariasRequestDTO request, Long empresaId) {
        log.info("Guardando etapas diarias para obra {} en fecha {}", request.getObraId(), request.getFecha());

        // 1. Validar obra
        Obra obra = obraRepository.findById(request.getObraId())
                .orElseThrow(() -> new RuntimeException("Obra no encontrada con ID: " + request.getObraId()));

        if (!obra.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para modificar esta obra");
        }

        // 2. Obtener o crear etapa diaria
        EtapaDiaria etapaDiaria = etapaDiariaRepository
                .findByObraIdAndFechaAndEmpresaId(request.getObraId(), request.getFecha(), empresaId)
                .orElseGet(() -> {
                    EtapaDiaria nueva = new EtapaDiaria();
                    nueva.setObraId(request.getObraId());
                    nueva.setFecha(request.getFecha());
                    nueva.setEmpresaId(empresaId);
                    return etapaDiariaRepository.save(nueva);
                });

        // 3. Procesar cada tarea
        List<EtapaDiariaCreacionResponseDTO.TareaCreacionDTO> tareasCreadas = new ArrayList<>();
        int totalAsignaciones = 0;

        for (TareaRequestDTO tareaDTO : request.getTareas()) {
            // Crear o actualizar tarea
            TareaEtapaDiaria tarea = crearOActualizarTarea(etapaDiaria.getId(), tareaDTO);
            
            // Eliminar asignaciones anteriores
            profesionalTareaDiaRepository.deleteByTareaId(tarea.getId());
            
            // Crear nuevas asignaciones
            int profesionalesAsignados = 0;
            for (ProfesionalTareaRequestDTO profDTO : tareaDTO.getProfesionales()) {
                // Validar que el profesional está disponible ese día
                AsignacionProfesionalDia asignacionDia = validarAsignacionDisponible(
                        profDTO.getAsignacionDiaId(),
                        request.getObraId(),
                        request.getFecha(),
                        empresaId
                );

                // Crear asignación profesional-tarea
                ProfesionalTareaDia ptd = new ProfesionalTareaDia();
                ptd.setTarea(tarea);
                ptd.setAsignacionDia(asignacionDia);
                ptd.setHorasAsignadas(profDTO.getHorasAsignadas());
                ptd.setRolEnTarea(profDTO.getRol());
                ptd.setNotas(profDTO.getNotas());
                // estado tiene valor por defecto en entidad
                ptd.setEmpresaId(empresaId);

                profesionalTareaDiaRepository.save(ptd);
                profesionalesAsignados++;
                totalAsignaciones++;
            }

            tareasCreadas.add(EtapaDiariaCreacionResponseDTO.TareaCreacionDTO.builder()
                    .tareaId(tarea.getId())
                    .nombreTarea(tareaDTO.getNombreTarea())
                    .profesionalesAsignados(profesionalesAsignados)
                    .build());
        }

        // 4. Construir respuesta
        return EtapaDiariaCreacionResponseDTO.builder()
                .etapaId(etapaDiaria.getId())
                .tareasCreadas(tareasCreadas.size())
                .asignacionesProfesionales(totalAsignaciones)
                .tareas(tareasCreadas)
                .build();
    }

    /**
     * Actualizar tarea específica
     */
    @Transactional
    public void actualizarTarea(Long tareaId, TareaRequestDTO tareaDTO, Long empresaId) {
        TareaEtapaDiaria tarea = tareaEtapaDiariaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + tareaId));

        // Actualizar campos básicos
        if (tareaDTO.getDescripcion() != null) {
            tarea.setDescripcion(tareaDTO.getDescripcion());
        }
        if (tareaDTO.getNombreTarea() != null) {
            tarea.setDescripcion(tareaDTO.getNombreTarea()); // usar descripcion para nombreTarea
        }
        if (tareaDTO.getHoraInicio() != null) {
            tarea.setHoraInicio(tareaDTO.getHoraInicio());
        }
        if (tareaDTO.getHoraFin() != null) {
            tarea.setHoraFin(tareaDTO.getHoraFin());
        }
        if (tareaDTO.getCategoria() != null) {
            tarea.setCategoria(tareaDTO.getCategoria());
        }
        if (tareaDTO.getPrioridad() != null) {
            tarea.setPrioridad(tareaDTO.getPrioridad());
        }
        if (tareaDTO.getEstado() != null) {
            tarea.setEstado(tareaDTO.getEstado());
        }

        tareaEtapaDiariaRepository.save(tarea);

        // Actualizar profesionales si se especifican
        if (tareaDTO.getProfesionales() != null && !tareaDTO.getProfesionales().isEmpty()) {
            profesionalTareaDiaRepository.deleteByTareaId(tareaId);

            for (ProfesionalTareaRequestDTO profDTO : tareaDTO.getProfesionales()) {
                ProfesionalTareaDia ptd = new ProfesionalTareaDia();
                ptd.setTarea(tarea);
                ptd.setAsignacionDia(asignacionProfesionalDiaRepository.findById(profDTO.getAsignacionDiaId())
                        .orElseThrow(() -> new RuntimeException("Asignación no encontrada")));
                ptd.setHorasAsignadas(profDTO.getHorasAsignadas());
                ptd.setRolEnTarea(profDTO.getRol());
                ptd.setNotas(profDTO.getNotas());
                ptd.setEmpresaId(empresaId);

                profesionalTareaDiaRepository.save(ptd);
            }
        }
    }

    /**
     * Eliminar tarea
     */
    @Transactional
    public void eliminarTarea(Long tareaId, Long empresaId) {
        TareaEtapaDiaria tarea = tareaEtapaDiariaRepository.findById(tareaId)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con ID: " + tareaId));

        // Eliminar asignaciones primero (cascade debería hacerlo automático si está configurado)
        profesionalTareaDiaRepository.deleteByTareaId(tareaId);
        
        // Eliminar tarea
        tareaEtapaDiariaRepository.delete(tarea);
    }

    // ===================== MÉTODOS HELPER =====================

    /**
     * Obtener profesionales disponibles para una obra en una fecha
     */
    private List<ProfesionalDisponibleDTO> obtenerProfesionalesDisponibles(Long obraId, LocalDate fecha, Long empresaId) {
        List<Object[]> resultados = asignacionProfesionalDiaRepository
                .findProfesionalesDisponiblesByObraAndFecha(obraId, fecha, empresaId);

        return resultados.stream()
                .map(row -> ProfesionalDisponibleDTO.builder()
                        .asignacionDiaId(((Number) row[0]).longValue())
                        .profesionalId(((Number) row[1]).longValue())
                        .profesionalNombre((String) row[2])
                        .tipoProfesional((String) row[3])
                        .cantidadJornales(row[4] != null ? ((Number) row[4]).intValue() : 0)
                        .semanaIso((String) row[5])
                        .build())
                .toList();
    }

    /**
     * Obtener tareas con profesionales asignados
     */
    private List<TareaResponseDTO> obtenerTareasConProfesionales(Long etapaDiariaId, Long empresaId) {
        List<TareaEtapaDiaria> tareas = tareaEtapaDiariaRepository.findByEtapaDiariaId(etapaDiariaId);

        return tareas.stream()
                .map(tarea -> {
                    List<ProfesionalTareaDia> profesionalesTarea = profesionalTareaDiaRepository.findByTareaId(tarea.getId());

                    return TareaResponseDTO.builder()
                            .tareaId(tarea.getId())
                            .nombreTarea(tarea.getDescripcion()) // usar descripción como nombreTarea
                            .descripcion(tarea.getDescripcion())
                            .categoria(tarea.getCategoria())
                            .horaInicio(tarea.getHoraInicio())
                            .horaFin(tarea.getHoraFin())
                            .estado(tarea.getEstado())
                            .prioridad(tarea.getPrioridad())
                            .profesionalesAsignados(profesionalesTarea.stream()
                                    .map(ptd -> ProfesionalAsignadoTareaDTO.builder()
                                            .asignacionDiaId(ptd.getAsignacionDia().getId())
                                            .profesionalId(ptd.getAsignacionDia().getAsignacion().getProfesionalId())
                                            .profesionalNombre(ptd.getAsignacionDia().getAsignacion().getProfesional().getNombre())
                                            .horasAsignadas(ptd.getHorasAsignadas() != null ? ptd.getHorasAsignadas() : BigDecimal.ZERO)
                                            .rol(ptd.getRolEnTarea())
                                            .estado(ptd.getEstado())
                                            .notas(ptd.getNotas())
                                            .build())
                                    .toList())
                            .build();
                })
                .toList();
    }

    /**
     * Crear o actualizar tarea
     */
    private TareaEtapaDiaria crearOActualizarTarea(Long etapaDiariaId, TareaRequestDTO tareaDTO) {
        // Buscar si ya existe una tarea con el mismo nombre
        TareaEtapaDiaria tarea = tareaEtapaDiariaRepository.findByEtapaDiariaId(etapaDiariaId).stream()
                .filter(t -> t.getDescripcion() != null && t.getDescripcion().equals(tareaDTO.getDescripcion()))
                .findFirst()
                .orElse(new TareaEtapaDiaria());

        tarea.setEtapaDiariaId(etapaDiariaId);
        tarea.setDescripcion(tareaDTO.getDescripcion() != null ? tareaDTO.getDescripcion() : tareaDTO.getNombreTarea());
        if (tareaDTO.getCategoria() != null) {
            tarea.setCategoria(tareaDTO.getCategoria());
        }
        if (tareaDTO.getHoraInicio() != null) {
            tarea.setHoraInicio(tareaDTO.getHoraInicio());
        }
        if (tareaDTO.getHoraFin() != null) {
            tarea.setHoraFin(tareaDTO.getHoraFin());
        }
        tarea.setPrioridad(tareaDTO.getPrioridad() != null ? tareaDTO.getPrioridad() : "MEDIA");
        tarea.setEstado(tareaDTO.getEstado() != null ? tareaDTO.getEstado() : "PENDIENTE");

        return tareaEtapaDiariaRepository.save(tarea);
    }

    /**
     * Validar que la asignación día existe y pertenece a la obra/fecha correcta
     */
    private AsignacionProfesionalDia validarAsignacionDisponible(
            Long asignacionDiaId,
            Long obraId,
            LocalDate fecha,
            Long empresaId) {

        AsignacionProfesionalDia asignacionDia = asignacionProfesionalDiaRepository.findById(asignacionDiaId)
                .orElseThrow(() -> new RuntimeException("Asignación profesional día no encontrada"));

        if (!asignacionDia.getAsignacion().getObraId().equals(obraId)) {
            throw new RuntimeException("El profesional no está asignado a esta obra");
        }

        if (!asignacionDia.getFecha().equals(fecha)) {
            throw new RuntimeException("El profesional no está disponible en esta fecha");
        }

        if (!asignacionDia.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para asignar este profesional");
        }

        return asignacionDia;
    }
}
