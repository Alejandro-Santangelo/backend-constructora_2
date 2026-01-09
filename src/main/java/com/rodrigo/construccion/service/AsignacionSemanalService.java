package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.*;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.exception.BusinessException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.AsignacionProfesionalDia;
import com.rodrigo.construccion.model.entity.AsignacionProfesionalObra;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.repository.AsignacionProfesionalDiaRepository;
import com.rodrigo.construccion.repository.AsignacionProfesionalObraRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.ProfesionalRepository;
import com.rodrigo.construccion.util.DiasHabilesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para gestión de asignaciones semanales de profesionales a obras
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AsignacionSemanalService {

    private final AsignacionProfesionalObraRepository asignacionRepository;
    private final AsignacionProfesionalDiaRepository asignacionDiaRepository;
    private final ObraRepository obraRepository;
    private final ProfesionalRepository profesionalRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Crear asignación semanal (modalidad "total" o "semanal")
     */
    public AsignacionSemanalCreacionResponseDTO crearAsignacionSemanal(
            AsignacionSemanalRequestDTO request, Long empresaId) {

        log.info("Creando asignación semanal para obra {} con modalidad {}", 
                request.getObraId(), request.getModalidad());

        // Validar modalidad
        if (!"total".equals(request.getModalidad()) && !"semanal".equals(request.getModalidad())) {
            throw new BusinessException("Modalidad inválida. Debe ser 'total' o 'semanal'");
        }

        // Validar obra
        Obra obra = obraRepository.findById(request.getObraId())
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + request.getObraId()));

        // Forzar inicialización de la obra para evitar lazy loading issues
        obra.getNombre(); // Toca una propiedad para inicializar el proxy

        if (!obra.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("La obra no pertenece a la empresa actual");
        }

        if (obra.getFechaInicio() == null) {
            throw new BusinessException("La obra debe tener fecha de inicio definida");
        }

        // Procesar según modalidad
        if ("total".equalsIgnoreCase(request.getModalidad())) {
            return procesarModalidadTotal(request, obra, empresaId);
        } else {
            return procesarModalidadSemanal(request, obra, empresaId);
        }
    }

    /**
     * Procesar modalidad "total" (equipo fijo)
     */
    private AsignacionSemanalCreacionResponseDTO procesarModalidadTotal(
            AsignacionSemanalRequestDTO request, Obra obra, Long empresaId) {

        if (request.getProfesionales() == null || request.getProfesionales().isEmpty()) {
            throw new BusinessException("Debe especificar al menos un profesional para modalidad 'total'");
        }

        // Calcular días hábiles
        List<LocalDate> diasHabiles = DiasHabilesUtil.calcularDiasHabilesPorSemanas(
                obra.getFechaInicio(), request.getSemanasObjetivo());

        if (diasHabiles.isEmpty()) {
            throw new BusinessException("No hay días hábiles en el período especificado");
        }

        int totalJornales = 0;
        int totalProfesionales = request.getProfesionales().size();

        // Para cada profesional
        for (ProfesionalAsignadoDTO profDTO : request.getProfesionales()) {
            // Validar profesional
            Profesional profesional = profesionalRepository.findById(profDTO.getProfesionalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Profesional no encontrado con ID: " + profDTO.getProfesionalId()));

            // Crear asignación principal
            AsignacionProfesionalObra asignacion = new AsignacionProfesionalObra();
            asignacion.setObra(obra);
            asignacion.setObraId(obra.getId());
            asignacion.setProfesional(profesional);
            asignacion.setProfesionalId(profesional.getId());
            asignacion.setEmpresaId(empresaId);
            asignacion.setModalidad("total");
            asignacion.setSemanasObjetivo(request.getSemanasObjetivo());
            asignacion.setTipoAsignacion("JORNAL");
            asignacion.setEstado("ACTIVO");
            asignacion.setFechaInicio(obra.getFechaInicio());
            asignacion.setRubroId(0L); // Mock - ajustar según lógica de negocio
            asignacion.setRubroNombre("Asignación Semanal");
            asignacion.setCantidadJornales(diasHabiles.size() * profDTO.getCantidadPorDia());
            asignacion.setJornalesUtilizados(0);

            asignacion = asignacionRepository.save(asignacion);

            // Crear detalle diario para todos los días hábiles
            List<AsignacionProfesionalDia> diasAsignacion = new ArrayList<>();
            for (LocalDate fecha : diasHabiles) {
                AsignacionProfesionalDia dia = new AsignacionProfesionalDia();
                dia.setAsignacion(asignacion);
                dia.setFecha(fecha);
                dia.setCantidad(profDTO.getCantidadPorDia());
                dia.setSemanaIso(DiasHabilesUtil.obtenerSemanaIso(fecha));
                dia.setEmpresaId(empresaId);
                diasAsignacion.add(dia);
            }

            asignacionDiaRepository.saveAll(diasAsignacion);
            totalJornales += asignacion.getCantidadJornales();

            log.info("Asignación creada para profesional {} con {} jornales en {} días", 
                    profesional.getNombre(), asignacion.getCantidadJornales(), diasHabiles.size());
        }

        return AsignacionSemanalCreacionResponseDTO.builder()
                .success(true)
                .message("Asignación creada correctamente")
                .data(AsignacionSemanalCreacionResponseDTO.DatosAsignacionDTO.builder()
                        .totalJornalesAsignados(totalJornales)
                        .diasHabiles(diasHabiles.size())
                        .profesionalesAsignados(totalProfesionales)
                        .build())
                .build();
    }

    /**
     * Procesar modalidad "semanal" (por semana)
     */
    private AsignacionSemanalCreacionResponseDTO procesarModalidadSemanal(
            AsignacionSemanalRequestDTO request, Obra obra, Long empresaId) {

        if (request.getAsignacionesPorSemana() == null || request.getAsignacionesPorSemana().isEmpty()) {
            throw new BusinessException("Debe especificar al menos una semana para modalidad 'semanal'");
        }

        int totalJornales = 0;
        Set<Long> profesionalesUnicos = new HashSet<>();
        int totalDias = 0;

        // Para cada semana
        for (AsignacionPorSemanaDTO semanaDTO : request.getAsignacionesPorSemana()) {
            // Validar que hay cantidades por día
            if (semanaDTO.getCantidadesPorDia() == null || semanaDTO.getCantidadesPorDia().isEmpty()) {
                throw new BusinessException("Debe especificar cantidades por día para la semana " + semanaDTO.getSemanaKey());
            }

            // Para cada profesional en esta semana
            for (ProfesionalBasicoDTO profDTO : semanaDTO.getProfesionales()) {
                // Validar profesional
                Profesional profesional = profesionalRepository.findById(profDTO.getProfesionalId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Profesional no encontrado con ID: " + profDTO.getProfesionalId()));

                profesionalesUnicos.add(profDTO.getProfesionalId());

                // Calcular total de jornales para este profesional en esta semana
                int jornalesSemana = semanaDTO.getCantidadesPorDia().values().stream()
                        .mapToInt(Integer::parseInt)
                        .sum();

                // Crear asignación principal
                AsignacionProfesionalObra asignacion = new AsignacionProfesionalObra();
                asignacion.setObra(obra);
                asignacion.setObraId(obra.getId());
                asignacion.setProfesional(profesional);
                asignacion.setProfesionalId(profesional.getId());
                asignacion.setEmpresaId(empresaId);
                asignacion.setModalidad("semanal");
                asignacion.setSemanasObjetivo(request.getSemanasObjetivo());
                asignacion.setTipoAsignacion("JORNAL");
                asignacion.setEstado("ACTIVO");
                asignacion.setFechaInicio(obra.getFechaInicio());
                asignacion.setRubroId(0L); // Mock - ajustar según lógica de negocio
                asignacion.setRubroNombre("Asignación Semanal - " + semanaDTO.getSemanaKey());
                asignacion.setCantidadJornales(jornalesSemana);
                asignacion.setJornalesUtilizados(0);

                asignacion = asignacionRepository.save(asignacion);

                // Crear detalle diario
                List<AsignacionProfesionalDia> diasAsignacion = new ArrayList<>();
                for (Map.Entry<String, String> entry : semanaDTO.getCantidadesPorDia().entrySet()) {
                    LocalDate fecha = LocalDate.parse(entry.getKey(), DATE_FORMATTER);
                    int cantidad = Integer.parseInt(entry.getValue());

                    // Validar que es día hábil
                    if (!DiasHabilesUtil.esDiaHabil(fecha)) {
                        log.warn("La fecha {} no es día hábil, se incluirá de todas formas", fecha);
                    }

                    AsignacionProfesionalDia dia = new AsignacionProfesionalDia();
                    dia.setAsignacion(asignacion);
                    dia.setFecha(fecha);
                    dia.setCantidad(cantidad);
                    dia.setSemanaIso(semanaDTO.getSemanaKey());
                    dia.setEmpresaId(empresaId);
                    diasAsignacion.add(dia);
                }

                asignacionDiaRepository.saveAll(diasAsignacion);
                totalJornales += jornalesSemana;
                totalDias += diasAsignacion.size();

                log.info("Asignación semanal creada para profesional {} en semana {} con {} jornales", 
                        profesional.getNombre(), semanaDTO.getSemanaKey(), jornalesSemana);
            }
        }

        return AsignacionSemanalCreacionResponseDTO.builder()
                .success(true)
                .message("Asignación semanal creada correctamente")
                .data(AsignacionSemanalCreacionResponseDTO.DatosAsignacionDTO.builder()
                        .totalJornalesAsignados(totalJornales)
                        .diasHabiles(totalDias)
                        .profesionalesAsignados(profesionalesUnicos.size())
                        .build())
                .build();
    }

    /**
     * Obtener asignaciones de una obra
     */
    @Transactional(readOnly = true)
    public List<AsignacionSemanalResponseDTO> obtenerAsignacionesPorObra(Long obraId, Long empresaId) {
        // Validar obra
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + obraId));

        if (!obra.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("La obra no pertenece a la empresa actual");
        }

        // Obtener asignaciones con modalidad definida
        List<AsignacionProfesionalObra> asignaciones = asignacionRepository
                .findByObra_IdAndEmpresaId(obraId, empresaId).stream()
                .filter(a -> a.getModalidad() != null)
                .collect(Collectors.toList());

        // Agrupar por modalidad y profesional
        Map<String, List<AsignacionProfesionalObra>> agrupadas = asignaciones.stream()
                .collect(Collectors.groupingBy(a -> a.getModalidad() + "-" + a.getProfesional().getId()));

        List<AsignacionSemanalResponseDTO> resultado = new ArrayList<>();

        for (List<AsignacionProfesionalObra> grupo : agrupadas.values()) {
            AsignacionProfesionalObra primera = grupo.get(0);
            
            // Obtener detalles diarios
            List<AsignacionProfesionalDia> dias = asignacionDiaRepository.findByObraId(obraId);

            // Agrupar por semana
            Map<String, List<AsignacionProfesionalDia>> porSemana = dias.stream()
                    .filter(d -> grupo.stream().anyMatch(a -> a.getId().equals(d.getAsignacion().getId())))
                    .collect(Collectors.groupingBy(AsignacionProfesionalDia::getSemanaIso));

            List<AsignacionSemanaDetalleDTO> semanas = porSemana.entrySet().stream()
                    .map(entry -> {
                        List<DetalleDiaDTO> detalles = entry.getValue().stream()
                                .map(d -> DetalleDiaDTO.builder()
                                        .fecha(d.getFecha().format(DATE_FORMATTER))
                                        .profesionalId(d.getAsignacion().getProfesional().getId())
                                        .profesionalNombre(d.getAsignacion().getProfesional().getNombre())
                                        .profesionalTipo(d.getAsignacion().getProfesional().getTipoProfesional())
                                        .importeJornal(d.getAsignacion().getImporteJornal())
                                        .cantidad(d.getCantidad())
                                        .build())
                                .collect(Collectors.toList());

                        return AsignacionSemanaDetalleDTO.builder()
                                .semanaKey(entry.getKey())
                                .detallesPorDia(detalles)
                                .build();
                    })
                    .collect(Collectors.toList());

            int totalJornales = grupo.stream()
                    .mapToInt(a -> a.getCantidadJornales() != null ? a.getCantidadJornales() : 0)
                    .sum();

            resultado.add(AsignacionSemanalResponseDTO.builder()
                    .asignacionId(primera.getId())
                    .modalidad(primera.getModalidad())
                    .semanasObjetivo(primera.getSemanasObjetivo())
                    .totalJornalesAsignados(totalJornales)
                    .asignacionesPorSemana(semanas)
                    .build());
        }

        return resultado;
    }

    /**
     * Actualizar asignación semanal
     */
    public AsignacionSemanalCreacionResponseDTO actualizarAsignacion(
            Long asignacionId, AsignacionSemanalRequestDTO request, Long empresaId) {

        log.info("Actualizando asignación {} para obra {}", asignacionId, request.getObraId());

        // Verificar que la asignación existe y pertenece a la empresa
        AsignacionProfesionalObra asignacionExistente = asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacionExistente.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("La asignación no pertenece a la empresa actual");
        }

        // Verificar que la obra del request coincida con la obra de la asignación original
        if (!asignacionExistente.getObra().getId().equals(request.getObraId())) {
            throw new BusinessException("No se puede cambiar la obra de una asignación existente");
        }

        // Eliminar asignación anterior (soft delete)
        asignacionExistente.setEstado("INACTIVO");
        asignacionRepository.save(asignacionExistente);

        // Crear nueva asignación
        AsignacionSemanalCreacionResponseDTO response = crearAsignacionSemanal(request, empresaId);
        response.setMessage("Asignación actualizada correctamente");

        log.info("Asignación {} actualizada exitosamente", asignacionId);
        return response;
    }

    /**
     * Eliminar asignación semanal individual
     */
    public AsignacionSemanalCreacionResponseDTO eliminarAsignacion(Long asignacionId, Long empresaId) {
        log.info("Eliminando asignación {}", asignacionId);

        AsignacionProfesionalObra asignacion = asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("La asignación no pertenece a la empresa actual");
        }

        // Hard delete - eliminación física del registro
        asignacionRepository.delete(asignacion);

        // Preparar response
        AsignacionSemanalCreacionResponseDTO.DatosAsignacionDTO datos = 
            AsignacionSemanalCreacionResponseDTO.DatosAsignacionDTO.builder()
                .asignacionId(asignacionId)
                .totalJornalesAsignados(0)
                .diasHabiles(0)
                .profesionalesAsignados(0)
                .build();

        AsignacionSemanalCreacionResponseDTO response = AsignacionSemanalCreacionResponseDTO.builder()
                .success(true)
                .message("Asignación eliminada correctamente")
                .data(datos)
                .build();

        log.info("Asignación {} eliminada físicamente de la base de datos", asignacionId);
        return response;
    }

    /**
     * Eliminar todas las asignaciones de una obra
     */
    public AsignacionSemanalCreacionResponseDTO eliminarAsignacionesPorObra(Long obraId, Long empresaId) {
        log.info("Eliminando todas las asignaciones de la obra {}", obraId);

        // Verificar que la obra existe y pertenece a la empresa
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + obraId));

        if (!obra.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("La obra no pertenece a la empresa actual");
        }

        // Buscar todas las asignaciones activas de la obra
        List<AsignacionProfesionalObra> asignaciones = asignacionRepository
                .findByObra_IdAndEmpresaIdAndEstado(obraId, empresaId, "ACTIVO");

        if (asignaciones.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron asignaciones activas para la obra " + obraId);
        }

        // Eliminar todas las asignaciones (hard delete - eliminación física)
        int asignacionesEliminadas = 0;
        for (AsignacionProfesionalObra asignacion : asignaciones) {
            asignacionRepository.delete(asignacion);
            asignacionesEliminadas++;
        }

        // Preparar response
        AsignacionSemanalCreacionResponseDTO.DatosAsignacionDTO datos = 
            AsignacionSemanalCreacionResponseDTO.DatosAsignacionDTO.builder()
                .asignacionId(obraId) // Usamos obraId como referencia
                .totalJornalesAsignados(asignacionesEliminadas) // Reutilizamos el campo para el contador
                .diasHabiles(0)
                .profesionalesAsignados(0)
                .build();

        AsignacionSemanalCreacionResponseDTO response = AsignacionSemanalCreacionResponseDTO.builder()
                .success(true)
                .message("Todas las asignaciones de la obra eliminadas correctamente")
                .data(datos)
                .build();

        log.info("Eliminadas {} asignaciones físicamente de la obra {}", asignacionesEliminadas, obraId);
        return response;
    }

    /**
     * Eliminar asignación semanal (método original para compatibilidad)
     */
    public void eliminarAsignacionSemanal(Long asignacionId, Long empresaId) {
        AsignacionProfesionalObra asignacion = asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        if (!asignacion.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("La asignación no pertenece a la empresa actual");
        }

        // Soft delete
        asignacion.setEstado("INACTIVO");
        asignacionRepository.save(asignacion);

        log.info("Asignación {} marcada como INACTIVA", asignacionId);
    }
}
